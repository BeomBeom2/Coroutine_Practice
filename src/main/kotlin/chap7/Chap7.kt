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

@OptIn(ExperimentalCoroutinesApi::class)
fun Ex_7_7_5() = runBlocking<Unit> { //부모 코루틴(runBlocking 코루틴) 생성
    val parentJob = coroutineContext[Job] //부모 코루틴의 CoroutineContext로부터 부모 코루틴의 Job 추출
    launch {
        val childJob = coroutineContext[Job] //자식 코루틴의 CoroutineContext으로부터 자식 코루틴의 Job 추출
        println("1. 부모 코루틴과 자식 코루틴의 Job은 같다. (${parentJob === childJob})")
        println("2. 자식 코루틴의 Job이 가지고 있는 parent는 부모 코루틴의 Job이다. (${childJob?.parent === parentJob})")
        println("3. 부모 코루틴의 Job은 자식 코루틴의 Job에 대한 참조를 가진다. (${parentJob?.children?.contains(childJob)})")
    }

    /*
     * 1. 부모 코루틴과 자식 코루틴의 Job은 같다. (false)
     * 2. 자식 코루틴의 Job이 가지고 있는 parent는 부모 코루틴의 Job이다. (true)
     * 3. 부모 코루틴의 Job은 자식 코루틴의 Job에 대한 참조를 가진다. (true)
     */
}

//부모 코루틴은 자식 코루틴이 모두 실행 완료 돼야 완료할 수 있다.
//특정 코루틴에 취소가 요청되면 취소는 자식 코루틴 방향으로만 전파된다.

fun Ex_7_7_9() = runBlocking<Unit> {
    val infiniteJob = launch {
        while(true) {
            delay(1000L)
        }
    }
    //invokeOnCompletion 콜백은 코루틴은 실행 완료 됐을 뿐만아니라, 취소 완료된 경우에도 동작한다.
    infiniteJob.invokeOnCompletion {
        println("invokeOnCompletion 콜백 실행됨")
    }
    infiniteJob.cancel()
}