package org.example.chap9

import kotlinx.coroutines.*
import org.example.util.getElapsedTime

//일시 중단 함수(delay)는 코루틴이 아니다.
//만약 일시 중단 함수를 코루틴처럼 사용하고 싶다면 일시 중단 함수를 코루틴 빌더로 감싸야 한다.

suspend fun delayAndPrintHelloWorld() {
    delay(1000L)
    println("Hello World")
}

suspend fun searchFromDB(keyword: String) : Array<String> {
    delay(1000L)
    return arrayOf("[DB]${keyword}1", "[DB]${keyword}2")
}

suspend fun searchFromServer(keyword: String) : Array<String> {
    delay(1000L)
    return arrayOf("[Server]${keyword}1", "[Server]${keyword}2")
}

suspend fun searchByKeyword(keyword: String) : Array<String> {
    val dbResults = searchFromDB(keyword)
    val serverResults = searchFromServer(keyword)
    return arrayOf(*dbResults, *serverResults)
}

//searchByKeyword 일시 중단 함수 내부에서 다음과 같이 coroutineScope를 supervisorScope로 변경하면 내부 async 블록에서 예외가 발생해도,
//부모 코루틴으로 예외가 전파되지 않는다.
//참고로 Deferred 객체는 await 함수 호출 시 추가로 예외를 노출하므로 try catch 문을 통해 감까 예외 발생 시 빈 경과라 반환되도록 한다.
suspend fun searchByKeyword1(keyword: String): Array<String> =
    supervisorScope {
        val dbResultsDefferred = async {
            throw Exception("dbResultsDeferred 에서 예외가 발생했습니다")
            searchFromDB(keyword)
        }
        val serverResultsDeferred = async {
            searchFromServer(keyword)
        }
        val dbResults = try {
            serverResultsDeferred.await()
        } catch (e: Exception) {
            arrayOf()
        }
        val serverReults = try {
            serverResultsDeferred.await()
        } catch (e: Exception) {
            arrayOf()
        }
        return@supervisorScope arrayOf(*dbResults, *serverReults)
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

fun Ex_9_9_13() = runBlocking<Unit> {
    println("[결과] ${searchByKeyword("keyword").toList()}")

    //[결과] [[DB]keyword1, [DB]keyword2, [Server]keyword1, [Server]keyword2]
}

