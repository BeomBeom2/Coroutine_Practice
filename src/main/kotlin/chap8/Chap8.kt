package org.example.chap8

import kotlinx.coroutines.*


//코루틴의 예외 전파를 제한하기 위한 첫 번째 방법은, 구조화를 깨는 것.
//두 번째 방법은, 자식 코루틴으로부터 예외를 전파받지 않는 Job 객체인 SupervisorJob을 사용하는 것.
//세 번째 방법은, SupervisorJob()의 대안으로 supervisorScope 함수를 사용하는 것이다.

fun Ex_8_8_4() = runBlocking<Unit> {
    val supervisorJob = SupervisorJob()
    launch(CoroutineName("Coroutine1") + supervisorJob) {
        launch(CoroutineName("coroutine3")) {
            throw Exception("예외 발생")
        }
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행")
    }
    launch(CoroutineName("Coroutine2") + supervisorJob) {
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행")
    }
    delay(1000L)

    /*
     * Exception in thread "main @Coroutine1#3" java.lang.Exception: 예외 발생
     * ...
     * [main @Coroutine2#4] 코루틴 실행
     */
}


//위의 예제는 SupervisorJob의 parent가 runBlocking이 아니라서 구조화가 깨져있다.
//SupervisorJob은 자식들의 상태와 독립적으로 존재하며,
//"모든 자식 코루틴이 완료되었는지"를 스스로 판단하지 않아서 완료 상태를 명시적으로 처리해야한다.
fun Ex_8_8_5() = runBlocking<Unit> {
    // - 구조
    // - runBlocking Job
    //      └─ supervisorJob
    //           ├─ Cor1 Job ─ Cor3 Job
    //           └─ Cor2 Job
    val supervisorJob = SupervisorJob(parent = this.coroutineContext[Job])
    launch(CoroutineName("Cor1") + supervisorJob) {
        launch(CoroutineName("cor3")) {
            throw Exception("예외 발생")
        }
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행")
    }
    launch(CoroutineName("Cor2") + supervisorJob) {
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행")
    }
    supervisorJob.complete()

    /*
     * Exception in thread "main @Cor1#3" java.lang.Exception: 예외 발생
     * ...
     * [main @Cor2#4] 코루틴 실행
     */
}

fun Ex_8_8_6() = runBlocking<Unit> {
    // - 구조
    // - runBlocking Job
    // - supervisorJob
    //      ├─ Cor1 Job ─ Cor3 Job
    //      └─ Cor2 Job

    val coroutineScope = CoroutineScope(SupervisorJob())
    coroutineScope.apply{
        launch(CoroutineName("Cor1")) {
            launch(CoroutineName("cor3")) {
                throw Exception("예외 발생")
            }
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        launch(CoroutineName("Cor2")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        delay(1000L)
    }
    /*
     * Exception in thread "DefaultDispatcher-worker-2 @Cor1#3" java.lang.Exception: 예외 발생
     * ...
     * [DefaultDispatcher-worker-2 @Cor2#4] 코루틴 실행
     */
}
