package coroutines.dispatcher.experiments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

//val dispatcher = Dispatchers.IO.limitedParallelism(1)
//val dispatcher = Dispatchers.Default
//val dispatcher = Dispatchers.IO
val dispatcher = Dispatchers.IO.limitedParallelism(100)

//val operation = ::cpu1
//val operation = ::blocking
val operation = ::suspending

fun cpu1() {
    var i = Int.MAX_VALUE
    while (i > 0) i -= if (i % 2 == 0) 1 else 2
}

fun blocking() {
    Thread.sleep(1000)
}

suspend fun suspending() {
    delay(1000)
}

suspend fun main() = measureTimeMillis {
    coroutineScope {
        repeat(100) {
            launch(dispatcher) {
                operation()
                println("Done $it")
            }
        }
    }
}.let { println("Took $it") }

// Dispatcher          | CPU-intensive | Blocking    | Suspending
// 1 thread            | Took 23888    | Took 100432 | Took 1028
// Dispatchers.Default | Took 4425     | Took 13077  | Took 1037
// Dispatchers.IO      | Took 4345     | Took 2033   | Took 1035
// 100 threads         | Took 4074     | Took 1030   | Took 1043

// CPUは、PCのコア数による。1threadは1コアしか使わないので遅い。他はPCのすべてのコアを使うので同じくらい。論理的にはDefaultが一番速いはず。
// Blockingは、Threadを占有するため、利用可能なThread数が多いほど時間が短くなる。ただしメモリ量などは増える。
// Suspendingは、一時停止するのでThreadを占有しないため、Thread数にかかわらず1秒で終わる