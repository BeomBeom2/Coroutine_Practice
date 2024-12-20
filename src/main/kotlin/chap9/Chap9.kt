package org.example.chap9

import kotlinx.coroutines.*
import org.example.util.getElapsedTime

//일시 중단 함수(delay)는 코루틴이 아니다.
//만약 일시 중단 함수를 코루틴처럼 사용하고 싶다면 일시 중단 함수를 코루틴 빌더로 감싸야 한다.

suspend fun delayAndPrintHelloWorld() {
    delay(1000L)
    println("Hello World")
}

fun Ex_9_9_7() = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    launch {
        delayAndPrintHelloWorld()
    }
    launch {
        delayAndPrintHelloWorld()
    }
    println(getElapsedTime(startTime))

    /*
     * 지난 시간: 6ms
     * Hello World
     * Hello World
     */

    //delayAndPrintHelloWorld 함수의 호출로 1초간 스레드 사용 권한을 양보한다.
    // 자유로워진 스레드는 다른 코루틴인 runBlocking 코루틴에 의해 사용될 수 있으므로 곧바로 마지막 줄의 getElapsedTime 이 실행된다.
}

//일시 중단 함수의 호출 가능 지점은 다음 두 가지다. 1. 코루틴 내부, 2. 일시 중단 함수
//일시 중단 함수에서 순차적이 아닌 독립적으로 실행하려면 async 블록을 사용하여 작성하면된다.
