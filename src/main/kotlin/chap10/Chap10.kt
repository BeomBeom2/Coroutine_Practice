package org.example.chap10

import kotlinx.coroutines.*
import org.example.util.getElapsedTime

fun Ex_10_10_2() = runBlocking<Unit> {
    launch {
        while(true) {
            println("자식 코루틴에서 작업 실행 중")
             yield()
        }
    }

    launch {
        while(true) {
            println("부모 코루틴에서 작업 실행 중")
            yield()
        }
    }
    /*
     * 루틴에서 작업 실행 중
     * 부모 코루틴에서 작업 실행 중
     * 자식 코루틴에서 작업 실행 중
     * 부모 코루틴에서 작업 실행 중
     * ...
     */
}

//delay 사용시 코루틴은 사용하던 스레드를 양보하고 설정된 시간 동안 코루틴을 일시 중단
//코루틴 스레드 양보의 강력함은 코루틴이 여러 개 실행되는 환경에서 드러난다.
fun Ex_10_10_4() = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    repeat(10) { repeatTime ->
        launch {
            delay(1000L) //Thread.sleep(1000L)을 사용하는 경우 스레드를 블로킹 시키므로 대략 10초가 걸린다.
            println("[${getElapsedTime(startTime)}] 코루틴${repeatTime} 실행 완료")
        }
    }

    /*
     * [지난 시간: 1020ms] 코루틴0 실행 완료
     * [지난 시간: 1031ms] 코루틴1 실행 완료
     * [지난 시간: 1031ms] 코루틴2 실행 완료
     * [지난 시간: 1031ms] 코루틴3 실행 완료
     * [지난 시간: 1031ms] 코루틴4 실행 완료
     * [지난 시간: 1031ms] 코루틴5 실행 완료
     * [지난 시간: 1031ms] 코루틴6 실행 완료
     * [지난 시간: 1031ms] 코루틴7 실행 완료
     * [지난 시간: 1031ms] 코루틴8 실행 완료
     * [지난 시간: 1031ms] 코루틴9 실행 완료
     */
}


fun Ex_10_10_6() = runBlocking<Unit> {
    val job = launch {
        println("1. launch 코루틴 작업이 시작되었습니다.")
        delay(1000L)
        println("2. launch 코루틴 작업이 완료되었습니다.")
    }

    println("3. runBlocking 코루틴이 곧 일시 중단 되고 메인 스레드가 양보됩니다.")
    job.join()

    println("4. runBlocking 이 메인 스레드에 분배돼 작업이 다시 재개됩니다.")
    //runBlocking 블럭내에 launch 블럭이 곧바로 실행되어서 1번이 먼저 실행 될 것 같지만,
    //runBlocking 코루틴과 launch 코루틴은 단일 스레드인 메인 스레드에서 실행되기 때문에,
    //하나의 코루틴이 스레드를 양보하지 않으면 다른 코루틴이 실행되지 못한다.
    /*
     * 3. runBlocking 코루틴이 곧 일시 중단 되고 메인 스레드가 양보됩니다.
     * 1. launch 코루틴 작업이 시작되었습니다.
     * 2. launch 코루틴 작업이 완료되었습니다.
     * 4. runBlocking 이 메인 스레드에 분배돼 작업이 다시 재개됩니다.
     */
}