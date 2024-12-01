package org.example

import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun main() = runBlocking<Unit>(context = CoroutineName("Main")){
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
    val dispatcher: CoroutineDispatcher = newSingleThreadContext(name = "SingleThread")


    val multiThreadDispatcher : CoroutineDispatcher = newFixedThreadPoolContext(nThreads = 2, name = "MultiThread")

    // 사실, newSingleThreadContext 도 newFixedThreadPoolContext 와 같은 함수이다. newSingleThreadContext 은 다음과 같이 만들어진다.
    // public fun newSingleThreadContext(name : String) : CloseableCoroutineDispatcher = newFixedThreadPoolContext(1, name)
}