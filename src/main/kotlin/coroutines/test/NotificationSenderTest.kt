package coroutines.test.notificationsendertest

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import kotlin.test.assertEquals

class NotificationSender(
    private val client: NotificationClient,
    private val exceptionCollector: ExceptionCollector,
    dispatcher: CoroutineDispatcher,
) {
    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            exceptionCollector.collectException(throwable)
        }
    val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + dispatcher + exceptionHandler
    )

    fun sendNotifications(notifications: List<Notification>) {
        notifications.forEach { notification ->
            scope.launch {
                client.send(notification)
            }
        }
    }

    fun cancel() {
        scope.coroutineContext.cancelChildren()
    }
}

data class Notification(val id: String)

interface NotificationClient {
    suspend fun send(notification: Notification)
}

interface ExceptionCollector {
    fun collectException(throwable: Throwable)
}

class NotificationSenderTest {

    @Test
    fun `should send notifications concurrently`() {
        val testDispatcher = StandardTestDispatcher()
        val client = FakeNotificationClient(
            delayTime = 100
        )
        val collector = FakeExceptionCollector()
        val sender = NotificationSender(
            client = client,
            exceptionCollector = collector,
            dispatcher = testDispatcher
        )
        val notifications = listOf(
            Notification("A"),
            Notification("B"),
            Notification("C")
        )
        sender.sendNotifications(notifications)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(notifications, client.sent)
        assertEquals(100, testDispatcher.scheduler.currentTime)

    }

    @Test
    fun `should cancel all coroutines when cancel is called`() {
        val testDispatcher = StandardTestDispatcher()
        val client = FakeNotificationClient(
            delayTime = 100
        )
        val collector = FakeExceptionCollector()
        val sender = NotificationSender(
            client = client,
            exceptionCollector = collector,
            dispatcher = testDispatcher
        )
        val notifications = listOf(
            Notification("A"),
            Notification("B"),
            Notification("C")
        )
        sender.sendNotifications(notifications)
        testDispatcher.scheduler.advanceTimeBy(50)
        sender.cancel()
        val childlen = sender.scope.coroutineContext.job.children
        assert(childlen.all { it.isCancelled })
    }

    @Test
    fun `should not cancel other sending processes when one of them fails`() {
        val testDispatcher = StandardTestDispatcher()
        val client = FakeNotificationClient(
            delayTime = 100,
            failEvery = 2
        )
        val collector = FakeExceptionCollector()
        val sender = NotificationSender(
            client = client,
            exceptionCollector = collector,
            dispatcher = testDispatcher
        )
        val notifications = listOf(
            Notification("A"),
            Notification("B"),
            Notification("C")
        )
        sender.sendNotifications(notifications)
        testDispatcher.scheduler.advanceUntilIdle()
        val childlen = sender.scope.coroutineContext.job.children
        childlen.forEachIndexed { index, job ->
            if (index == 1) {
                assertEquals(true, job.isCancelled)
                assertEquals(true, job.isCompleted)
            } else {
                assertEquals(false, job.isCancelled)
                assertEquals(true, job.isCompleted)
            }
        }
        assertEquals(2, client.sent.size)
    }

    @Test
    fun `should collect exceptions from all coroutines`() {
        val testDispatcher = StandardTestDispatcher()
        val client = FakeNotificationClient(
            delayTime = 100,
            failEvery = 2
        )
        val collector = FakeExceptionCollector()
        val sender = NotificationSender(
            client = client,
            exceptionCollector = collector,
            dispatcher = testDispatcher
        )
        val notifications = listOf(
            Notification("A"),
            Notification("B"),
            Notification("C")
        )
        sender.sendNotifications(notifications)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, collector.collected.size)
    }
}

class FakeNotificationClient(
    val delayTime: Long = 0L,
    val failEvery: Int = Int.MAX_VALUE
) : NotificationClient {
    var sent = emptyList<Notification>()
    var counter = 0
    var usedThreads = emptyList<String>()

    override suspend fun send(notification: Notification) {
        if (delayTime > 0) delay(delayTime)
        usedThreads += Thread.currentThread().name
        counter++
        if (counter % failEvery == 0) {
            throw FakeFailure(notification)
        }
        sent += notification
    }
}

class FakeFailure(val notification: Notification) : Throwable("Planned fail for notification ${notification.id}")

class FakeExceptionCollector : ExceptionCollector {
    var collected = emptyList<Throwable>()

    override fun collectException(throwable: Throwable) = synchronized(this) {
        collected += throwable
    }
}
