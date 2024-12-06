package org.example.chap7

import kotlinx.coroutines.*
import org.example.util.getElapsedTime
import org.example.util.printJobState
import kotlin.coroutines.CoroutineContext

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


fun Ex_7_7_10() = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    val parentJob = launch {
        launch {
            delay(1000L)
            println("[${getElapsedTime(startTime)}] 자식 코루틴 실행 완료")
        }
        println("[${getElapsedTime(startTime)}] 부모 코루틴의 마지막 실행 코드")
    }
    parentJob.invokeOnCompletion { // 부모 코루틴이 종료될 시 호출되는 콜백 등록
        println("[${getElapsedTime(startTime)}] 부모 코루틴 실행 완료")
    }
    delay(500L)
    printJobState(parentJob) // 실행 중과 실행 완료 중의 상태는 동일.

    /*
     * [지난 시간: 13ms] 부모 코루틴의 마지막 실행 코드
     * Job State
     * isActive >> true
     * isCancelled >> false
     * isCompleted >> false
     * [지난 시간: 1025ms] 자식 코루틴 실행 완료
     * [지난 시간: 1026ms] 부모 코루틴 실행 완료
     */
}

class CustomCoroutineScope : CoroutineScope {
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    override val coroutineContext: CoroutineContext
        get() = Job() + newSingleThreadContext("CustomScopeThread")
}
fun Ex_7_7_12() {
    //CustomCoroutineScope 인터페이스 구현을 통한 생성
    val coroutineScope = CustomCoroutineScope() //CustomCoroutineScope 인스턴스화
    coroutineScope.launch {
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행 완료")
    }
    Thread.sleep(1000L)

    //CoroutineScope 함수를 사용하는 방법
    val coroutineScope1 = CoroutineScope(Dispatchers.IO)
    coroutineScope1.launch {
        delay(100L)
        println("[${Thread.currentThread().name}] 코루틴 실행 완료")
    }
    Thread.sleep(1000L)

    //[CustomScopeThread @coroutine#2] 코루틴 실행 완료
    //[DefaultDispatcher-worker-1 @coroutine#3] 코루틴 실행 완료
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun Ex_7_7_13() {
    val newScope = CoroutineScope(CoroutineName("MyCoroutine") + Dispatchers.IO)
    newScope.launch(CoroutineName("LaunchCoroutine")) {
        println(this.coroutineContext[CoroutineName])
        println(this.coroutineContext[CoroutineDispatcher])
        val launchJob = this.coroutineContext[Job]
        val newScopeJob = newScope.coroutineContext[Job]
        println("launchJob?.parent === newScopeJob 이다. (${launchJob?.parent === newScopeJob})")
    }
    Thread.sleep(1000L)

    //CoroutineName(LaunchCoroutine)
    //Dispatchers.IO
    //launchJob?.parent === newScopeJob 이다. (true)
}