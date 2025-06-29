package coroutines.flow.observeappointmentsusecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAppointmentsUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    fun observeAppointments(): Flow<List<Appointment>> =
        appointmentRepository
            .observeAppointments()
            .filterIsInstance<AppointmentUpdate>()
            .map { it.appointments }
            .distinctUntilChanged()
            .retry { it is ApiException && it.code in 500..599 }
}

interface AppointmentRepository {
    fun observeAppointments(): Flow<AppointmentEvent>
}

sealed class AppointmentEvent
data class AppointmentUpdate(
    val appointments: List<Appointment>
) : AppointmentEvent()

data object AppointmentConfirmed : AppointmentEvent()
data class Appointment(val title: String, val time: Instant)
data class ApiException(val code: Int) : Throwable()

class FakeAppointmentRepository(
    private val flow: Flow<AppointmentEvent>
) : AppointmentRepository {
    override fun observeAppointments() = flow
}

class ObserveAppointmentsUseCaseTest {
    @Test
    fun ` should receive only appointment lists from appointment updates`() = runTest {
        val a1 = Appointment("Title1", Instant.parse("2025-06-01T01:00:00Z"))
        val a2 = Appointment("Title2", Instant.parse("2025-06-02T01:00:00Z"))
        val a3 = Appointment("Title3", Instant.parse("2025-06-03T01:00:00Z"))
        val repo = FakeAppointmentRepository(
            flowOf(
                AppointmentConfirmed,
                AppointmentUpdate(listOf(a1)),
                AppointmentUpdate(listOf(a2)),
                AppointmentUpdate(listOf(a3)),
                AppointmentConfirmed

            )
        )
        val useCase = ObserveAppointmentsUseCase(repo)

        val result = useCase.observeAppointments().toList()

        assertEquals(
            listOf(
                listOf(a1),
                listOf(a2),
                listOf(a3)
            ), result
        )
    }

    @Test
    fun `should not receive non-distinct values`() = runTest {
        val a1 = Appointment("Title1", Instant.parse("2025-06-01T01:00:00Z"))
        val a2 = Appointment("Title2", Instant.parse("2025-06-02T01:00:00Z"))

        val repo = FakeAppointmentRepository(
            flow {
                delay(1000)
                emit(AppointmentUpdate(listOf(a1)))
                emit(AppointmentUpdate(listOf(a1)))
                delay(1000)
                emit(AppointmentUpdate(listOf(a1)))
                emit(AppointmentUpdate(listOf(a2)))
                delay(1000)
                emit(AppointmentUpdate(listOf(a2)))
                emit(AppointmentUpdate(listOf(a1)))
            }
        )
        val useCase = ObserveAppointmentsUseCase(repo)

        val result = useCase.observeAppointments()
            .map { currentTime to it }
            .toList()

        assertEquals(
            listOf(
                1000L to listOf(a1),
                2000L to listOf(a2),
                3000L to listOf(a1)
            ), result
        )
    }

    @Test
    fun ` should retry exceptions of type ApiException with code 5XX`() = runTest {
        var retryFlag = false
        val exception = object : Exception() {}
        val a1 = Appointment("Title1", Instant.parse("2025-06-01T01:00:00Z"))
        val repo = FakeAppointmentRepository(
            flow {
                emit(AppointmentUpdate(listOf(a1)))
                if (!retryFlag) {
                    retryFlag = true
                    throw ApiException(500)
                } else {
                    throw exception
                }
            }
        )
        val useCase = ObserveAppointmentsUseCase(repo)

        val result = useCase.observeAppointments()
            .catch<Any> { emit(it) }
            .toList()

        assertTrue(retryFlag)
        assertEquals(
            listOf(
                listOf(a1),
                listOf(a1),
                exception,
            ), result
        )
    }
}