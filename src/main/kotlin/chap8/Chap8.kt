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



//SupervisorJob을 사용할 때 실수.
//launch에서 Job객체를 파라미터로 전달하면 이를 Parent로 갖는 새로운 Job객체를 만든다.
//이에 대한 자식을 만들고 예외를 발생시키면 의도하지 않게 예외 전파를 해서 SupervisorJob의 특성을 사용할 수 없다.
fun Ex_8_8_7() = runBlocking<Unit> {
    launch(CoroutineName("Parent Cor") + SupervisorJob()) {
        launch(CoroutineName("Cor1")) {
            launch(CoroutineName("Cor3")) {
                throw Exception("예외 발생")
            }
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        launch(CoroutineName("Cor2")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
    delay(1000L)
}

//supervisorScope의 특징은 supervisorJob을 부모로하는 Job을 생성한다는 것이다.
// 따라서 구조화할 필요가 없으며 자식이 모두 완료되면 자동 완료를 하기 때문에 명시적으로 complete()를 사용할 필요가 없다.
fun Ex_8_8_8() = runBlocking<Unit> {
    // - 구조
    // - runBlocking Job
    //      └─ supervisorJob
    //           ├─ Cor1 Job ─ Cor3 Job
    //           └─ Cor2 Job
    supervisorScope {
        launch(CoroutineName("Cor1")) {
            launch(CoroutineName("Cor3")) {
                throw Exception("예외 발생")
            }
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
        launch(CoroutineName("Cor2")) {
            delay(100L)
            println("[${Thread.currentThread().name}] 코루틴 실행")
        }
    }
    /*
     * Exception in thread "main @Cor1#3" java.lang.Exception: 예외 발생
     * ...
     * [main @Cor2#4] 코루틴 실행
     */
}

// exceptionHandler은 상속이 가능하다.
fun Ex_8_8_9() = runBlocking<Unit> {
    // - 구조
    // - runBlocking Job
    // - CoroutineScope Job
    //      └─ Cor1 Job
    val exceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        println("[예외 발생] $throwable")
    }
    CoroutineScope(exceptionHandler).launch(CoroutineName("Cor1")) {
        throw Exception("Cor1에서 예외 발생")
    }
    delay(1000L)

    /*
     * [예외 발생] java.lang.Exception: Cor1에서 예외 발생
     * Process finished with exit code 0
     */
}


// Cor1이 예외를 발생시켰지만 runBlocking Job에게 예외를 전파했기 때문에,
// Handler 객체는 예외를 처리했다고 생각하여 동작하지 않는다.
// 결론적으로, 구조화된 코루틴상에 여러 CoroutineExceptionHandler 객체가 설정되어도
// 마지막 예외를 전파받는 위치에 설정된 coroutineCExceptionHandler 객체만 예외를 처리한다.
fun Ex_8_8_10() = runBlocking<Unit> {
    // - 구조
    // - runBlocking Job
    //      └─ Cor1 Job

    val exceptionHandler = CoroutineExceptionHandler { _, throwable->
        println("[예외 발생] $throwable")
    }

    launch(CoroutineName("Cor1") + exceptionHandler) {
        throw Exception("Cor1에서 예외가 발생")
    }
    delay(1000L)

    /*
     * Exception in thread "main" java.lang.Exception: Cor1에서 예외가 발생
     * ...
     */
}
