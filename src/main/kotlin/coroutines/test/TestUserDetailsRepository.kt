package coroutines.test.testuserdetailsrepository

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class UserDetailsRepository(
    private val client: UserDataClient,
    private val userDatabase: UserDetailsDatabase,
    private val backgroundScope: CoroutineScope,
) {
    suspend fun getUserDetails(): UserDetails = coroutineScope {
        val stored = userDatabase.load()
        if (stored != null) {
            return@coroutineScope stored
        }
        val name = async { client.getName() }
        val friends = async { client.getFriends() }
        val profile = async { client.getProfile() }
        val details = UserDetails(
            name = name.await(),
            friends = friends.await(),
            profile = profile.await(),
        )
        backgroundScope.launch { userDatabase.save(details) }
        details
    }
}

interface UserDataClient {
    suspend fun getName(): String
    suspend fun getFriends(): List<Friend>
    suspend fun getProfile(): Profile
}

interface UserDetailsDatabase {
    suspend fun load(): UserDetails?
    suspend fun save(user: UserDetails)
}

data class UserDetails(
    val name: String,
    val friends: List<Friend>,
    val profile: Profile
)

data class Friend(val id: String)
data class Profile(val description: String)

class UserDetailsRepositoryTest {

    @Test
    fun `should fetch details asynchronously`() = runTest {
        // given
        val client = object : UserDataClient {
            override suspend fun getName(): String {
                delay(100)
                return "Ben"
            }

            override suspend fun getFriends(): List<Friend> {
                delay(200)
                return listOf(Friend("friend-id-1"))
            }

            override suspend fun getProfile(): Profile {
                delay(300)
                return Profile("Example description")
            }
        }
        var savedDetails: UserDetails? = null
        val database = object : UserDetailsDatabase {
            override suspend fun load(): UserDetails? {
                delay(400)
                return savedDetails
            }

            override suspend fun save(user: UserDetails) {
                delay(500)
                savedDetails = user
            }
        }

        val repo: UserDetailsRepository = UserDetailsRepository(
            client = client,
            userDatabase = database,
            backgroundScope = backgroundScope
        )

        // when
        val details = repo.getUserDetails()

        // then data are fetched asynchronously
        val expectedDetails = UserDetails(
            name = "Ben",
            friends = listOf(Friend("friend-id-1")),
            profile = Profile("Example description")
        )
        assertEquals(expectedDetails, details)
        assertEquals(700, currentTime)
        assertEquals(null, savedDetails)

        // when all children are finished
        backgroundScope.coroutineContext.job.children
            .forEach { it.join() }

        // then data are saved to the database
        assertEquals(1200, currentTime)
        assertEquals(details, savedDetails)

        // when getting details again
        val details2 = repo.getUserDetails()

        // then data are loaded from the database
        assertEquals(details, details2)
        assertEquals(1600, currentTime)
    }


}
