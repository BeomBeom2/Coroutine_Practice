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

fun Ex_5_5_5() = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    val participantDeferred1 : Deferred<Array<String>> = async(Dispatchers.IO) {
        delay(1000L)
        arrayOf("A", "B")
    }

    val participantDeferred2 : Deferred<Array<String>> = async(Dispatchers.IO) {
        delay(1000L)
        arrayOf("C")
    }

    //기다려야 하는 값들이 많은 경우 awaitAll()을 이용하여 대기
    //val results : List<Array<String>> = awaitAll(participantDeferred1, participantDeferred2)

    //Collection<Deferred<T>>에 대해 확장함수를 이용해 awaitAll()메소드 사용.
    val results : List<Array<String>> = listOf(participantDeferred1, participantDeferred2).awaitAll()

    println("[${getElapsedTime(startTime)}] 참여자 목록 : ${listOf(*results[0], *results[1])}")
}

//코루틴 라이브러리에서 제공하는 withContext를 사용하면 async-await 작업을 대체할 수 있다.
fun Ex_5_5_8() = runBlocking<Unit> {
    //async-await을 사용하는 예제를 간단히 살펴보자.
    val networkDeferred : Deferred<String> = async(Dispatchers.IO) {
        delay(1000L)
        return@async "Dummy Response"
    }
    val res1 = networkDeferred.await() //끝날 때 까지 대기

    println(res1)

    val result : String = withContext(Dispatchers.IO) {
        delay(1000L)
        return@withContext "Dummy Response"
    }
    println(result)
}

//withContext 함수는 async-await를 사용하는 것 같지만 실제적으로는 다르게 동작한다.
// withContext 함수는 실행 중이던 코루틴을 유지한 채로, 실행 환경만 변경해 작업을 처리한다.
// 여기서 실행 환경이란, 주로 스레드 풀, 디스패처 종류, 그리고 코루틴의 속성을 말할 수 있다.
fun Ex_5_5_9() = runBlocking<Unit> {
    println("[${Thread.currentThread().name}] runBlocking 실행")
    withContext(Dispatchers.IO) {
        println("[${Thread.currentThread().name}] withContext 실행")
    }
    withContext(Dispatchers.Default) {
        println("[${Thread.currentThread().name}] withContext 실행")
    }
    //Dispatchers.IO와 Default는 같은 공유 스레드 풀(=DefaultDispatcher-worker-1)을 사용한다.

    /*
     * [main @coroutine#2] runBlocking 실행
     * [DefaultDispatcher-worker-1 @coroutine#2] withContext 실행
     * [DefaultDispatcher-worker-1 @coroutine#2] withContext 실행
     */
}

fun Ex_5_5_10() = runBlocking<Unit> {
    println("[${Thread.currentThread().name}] runBlocking 실행")
    async(Dispatchers.IO) {
        println("[${Thread.currentThread().name}] async 실행")
    }.await()

    async(Dispatchers.Default) {
        println("[${Thread.currentThread().name}] async 실행")
    }.await()
    // async-await은 새로운 코루틴을 만든다.

    /*
     * [main @coroutine#2] runBlocking 실행
     * [DefaultDispatcher-worker-1 @coroutine#3] async 실행
     * [DefaultDispatcher-worker-1 @coroutine#4] async 실행
     */

    //withContext 를 호출시 코루틴은 유지된 채, 실행 스레드만 변경되며 동기적으로 실행.
    //async-await 은 새로운 코루틴을 만들지만 await 함수를 통해 순차 처리가 되어 동기적으로 실행.
}