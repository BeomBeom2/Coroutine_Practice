package org.example.chap5

import kotlinx.coroutines.*
import org.example.getElapsedTime

fun Ex_5_1_1() = runBlocking<Unit> {
    val networkDeferred: Deferred<String> = async(Dispatchers.IO) {
        delay(1000L)
        return@async "Dummy Response"
    }
    //결괏값이 반환될 때까지 runBlokcing 일시 중단
    //코루틴이 실행완료될 때까지 호출부의 코루틴을 일시중단한다는 점에서 join과 유사하다.
    val result = networkDeferred.await()
    println(result)
}

fun Ex_5_5_1() = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    val participantDeferred1: Deferred<Array<String>> = async(Dispatchers.IO) {
        //2. 플랫폼1에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async arrayOf("A", "B")
    }
    //3. 결과가 수신될 떄까지 대기
    val participants1 = participantDeferred1.await()

    val participantDeferred2: Deferred<Array<String>> = async(Dispatchers.IO) {
        //4. 플랫폼2에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async arrayOf("C")
    }
    //5. 결과가 수신될 때까지 대기
    val participants2 = participantDeferred2.await()

    //6. 지난 시간 표시 및 등록된 사람 목록을 병합해 출력
    println("[${getElapsedTime(startTime)}] 등록된 사람 목록 : ${listOf(*participants1, *participants2)}")
}

fun Ex_5_5_2() = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    val participantDeferred1: Deferred<Array<String>> = async(Dispatchers.IO) {
        //2. 플랫폼1에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async arrayOf("A", "B")
    }


    val participantDeferred2: Deferred<Array<String>> = async(Dispatchers.IO) {
        //4. 플랫폼2에서 등록한 관람객 목록을 가져오는 코루틴
        delay(1000L)
        return@async arrayOf("C")
    }
    //3. 모든 결과가 수신될 때까지 대기
    val participants1 = participantDeferred1.await()
    val participants2 = participantDeferred2.await()

    //4. 지난 시간 표시 및 등록된 사람 목록을 병합해 출력
    println("[${getElapsedTime(startTime)}] 등록된 사람 목록 : ${listOf(*participants1, *participants2)}")
}