package org.example

import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun main() = runBlocking<Unit>(context = CoroutineName("Main")){
    //Ex_4_1_2()

    Ex_4_3_1()
    // Main.kt의 edit Configuration에서 VM options부분에 -Dkotlinx.coroutines.debug을 설정해야 한다.
    println("[${Thread.currentThread().name}] 실행")

    launch(context = CoroutineName("Cor1")) {
        println("[${Thread.currentThread().name}] 실행")
    }

    launch(context = CoroutineName("Cor2")) {
        println("[${Thread.currentThread().name}] 실행")
    }

    //단일 스레드 디스패처 = 작업 대기열 + 스레드가 하나 들어가는 스레드 풀
    //스레드의 이름은 name 인자로 넘긴 값이 된다.
    // 사실, newSingleThreadContext 도 newFixedThreadPoolContext 와 같은 함수이다. newSingleThreadContext 은 다음과 같이 만들어진다.
    // public fun newSingleThreadContext(name : String) : CloseableCoroutineDispatcher = newFixedThreadPoolContext(1, name)
    val dispatcher: CoroutineDispatcher = newSingleThreadContext(name = "SingleThread")
    launch(context = dispatcher) {
        println("[${Thread.currentThread().name}] 실행")
    }

    val multiThreadDispatcher : CoroutineDispatcher = newFixedThreadPoolContext(nThreads = 2, name = "MultiThread")
    launch(context = multiThreadDispatcher) {
        println("[${Thread.currentThread().name}] 실행 1")
    }

    launch(context = multiThreadDispatcher) {
        println("[${Thread.currentThread().name}] 실행 2")
        launch {
            println("[${Thread.currentThread().name}] 실행 3")
        }
    }


    //Dispatchers.Default에서 무겁고 오랜 작업을 사용하는 작업을 launch하면 모든 스레드를 사용할 수가 있다.
    //이는 상황에 따라 효율적이지 않을 수 있어서 일부의 스레드만 사용할 수 있도록 limitedParalllelism() 메서드를 제공한다.
    //limitedParalllelism으로 만들어내는 스레드 풀은 기본적으로 공유되는 스레드 풀에서 따로 빼는 것이 아닌 전체 공유 스레드 내에서 IO와 Default를 제외한 나머지에서 스레드 풀을 구성한다.
    launch(Dispatchers.Default.limitedParallelism(2)) {
        repeat(10) {
            launch {
                println("[${Thread.currentThread().name}] 실행")
            }
        }
    }
    //Dispatchers.Default와 Dispatchers.IO는 애플리케이션 레벨의 공유 스레드 풀을 제공한다.
    //이 둘은 모두 같은 공유 스레드 풀을 사용하기 때문에 DefaultDispatcher-worker-1, ..으로 네이밍된다.
}


@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun Ex_4_1_2() = runBlocking<Unit> {
    val updateTokenJob = launch(Dispatchers.IO) {
        println("[${Thread.currentThread().name}] 토큰 업데이트 시작")
        delay(100L)
        println("[${Thread.currentThread().name}] 토큰 업데이트 완료")
    }

    updateTokenJob.join()

    val networkCallJob = launch(Dispatchers.IO) {}
    println("[${Thread.currentThread().name}] 네트워크 요청")
}

fun Ex_4_3_1() = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    val immediateJob : Job = launch {
        println("[${Thread.currentThread().name}] 즉시 실행")
    }

    val lazyJob : Job = launch(start = CoroutineStart.LAZY) {
        println("[${getElapsedTime(startTime)}] 지연 실행")
    }
    delay(1000L)
    lazyJob.start()
}

fun getElapsedTime(startTime: Long) : String = "지난 시간: ${System.currentTimeMillis() - startTime}ms"

