package org.example.chap7

import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun Ex_7_7_2() = runBlocking<Unit> {
    val coroutineContext = newSingleThreadContext("my Thread") +
            CoroutineName("CoroutineA")
    launch(coroutineContext) {
        println("[${Thread.currentThread().name}] 부모 코루틴")
        launch {
            println("[${Thread.currentThread().name}] 자식 코루틴")
        }
    }

    /*
     * [my Thread @CoroutineA#3] 부모 코루틴
     * [my Thread @CoroutineA#4] 자식 코루틴
     */
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun Ex_7_7_3() = runBlocking<Unit> {
    val coroutineContext = newSingleThreadContext("my Thread") +
            CoroutineName("ParentCoroutine")
    launch(coroutineContext) {
        println("[${Thread.currentThread().name}] 부모 코루틴 ")
        launch(CoroutineName("ChildCoroutine")) {
            println("[${Thread.currentThread().name}] 자식 코루틴 ")
        }
    }

    /*
     * [my Thread @CoroutineA#3] 부모 코루틴
     * [my Thread @ChildCoroutine#4] 자식 코루틴
     */
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun Ex_7_7_4() = runBlocking<Unit> {
    val runBlockingJob = coroutineContext[Job] // 부모 코루틴의 CoroutineContext로부터 부모 코루틴의 Job 추출
    launch {
        val launchJob = coroutineContext[Job] // 자식 코투린의 CoroutinContext로부터 자식 코루틴의 Job 추출

        println("runBlocking으로 생성된 Job과 launch로 생성된 Job은 동일하다.(${runBlockingJob === launchJob})")
    }

    //runBlocking으로 생성된 Job과 launch로 생성된 Job은 동일하다.(false)
}
