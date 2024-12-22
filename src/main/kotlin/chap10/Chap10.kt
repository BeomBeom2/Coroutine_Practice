package org.example.chap10

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
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