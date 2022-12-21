package io.gelmium

import io.ktor.websocket.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.*
import io.gelmium.entities.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun main() {
//    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
//        .start(wait = true)

    setupConnectionForAccount("", "")

}

//fun Application.module() {
//    configureRouting()
//}


val format = Json { ignoreUnknownKeys = true }
const val DEFAULT_DELAY = 1000L

fun setupConnectionForAccount(username: String, password: String = "") {
    val client = HttpClient {
        install(WebSockets)
    }
    runBlocking {
        client.wss(method = HttpMethod.Get, host = "game.era7.io", port = 443, path = "/gs") {
//        client.webSocket(method = HttpMethod.Get, host = "localhost", port = 8000, path = "/") {
            val connectionState = UserConnectionState(username, password)
            val messageOutputRoutine = launch { outputMessages(connectionState) }
            val userInputRoutine = launch { inputMessages(connectionState) }

            userInputRoutine.join() // Wait for completion; either "exit" or error
            messageOutputRoutine.cancelAndJoin()
        }
    }
    client.close()
    println("Connection for account: $username is closed. Goodbye!")
}


suspend fun DefaultClientWebSocketSession.processResponseMessage(
    responseMessage: String,
    userState: UserConnectionState
) {
    println("[${userState.username}] v: $responseMessage")
    if (userState.status == Status.AWAITING_MSG) {
        if (userState.awaitMsgType == AwaitMsgType.LOGIN) {
            // decode login message
            val response = format.decodeFromString<LoginResponse>(responseMessage)
            if (response.state == 0) {
                userState.uid = response.uid
                userState.sessionToken = response.sessionToken
                userState.groupId = response.loginData.groups.values.first().group_id
                userState.status = Status.IDLE
                userState.awaitMsgType = null
                var payload = Json.encodeToString(GameReq(GetBattleInfoPayload(userState.sessionToken), "logic"))
                println("^ $payload")
                send(payload)
                delay(DEFAULT_DELAY)
                payload = Json.encodeToString(GameReq(SingInStatuePayload(userState.sessionToken), "logic"))
                println("^ $payload")
                send(payload)
                delay(DEFAULT_DELAY)
                payload = Json.encodeToString(GameReq(GetRankInfoPayload(userState.sessionToken), "logic"))
                println("^ $payload")
                send(payload)
                println("Login Success with sessionToken=${userState.sessionToken}")
            } else {
                println("Login failed with response: $response")
            }
        }
    }
}

suspend fun DefaultClientWebSocketSession.outputMessages(userState: UserConnectionState) {
    try {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            processResponseMessage(message.readText(), userState)
//            println("v: ${message.readText()}")
        }
    } catch (e: Exception) {
        println("Error while receiving: " + e.localizedMessage)
    }
}

suspend fun DefaultClientWebSocketSession.processInputMessage(inputMessage: String, userState: UserConnectionState) {
    when (inputMessage) {
        "login" -> {
//            val payload = buildLoginPayload(userState.username, userState.password).toString()
            val payload = Json.encodeToString(GameReq(LoginPayload(userState.username, userState.password),"login"))
            println("^ $payload")
            send(payload)
            userState.status = Status.AWAITING_MSG
            userState.awaitMsgType = AwaitMsgType.LOGIN
        }

        "sync" -> {
            val payload = buildSyncRolePayload(userState.sessionToken).toString()
            println("^ $payload")
            send(payload)
        }

        "rank" -> {
            var payload = Json.encodeToString(GameReq(GetRankRewardPayload(userState.sessionToken),"logic"))
            println("^ $payload")
            send(payload)
            delay(DEFAULT_DELAY)
            payload = Json.encodeToString(GameReq(GetRankInfoPayload(userState.sessionToken), "logic"))
            println("^ $payload")
            send(payload)
        }

        else -> send(inputMessage)
    }
}

suspend fun DefaultClientWebSocketSession.inputMessages(userState: UserConnectionState) {
    while (true) {
        delay(DEFAULT_DELAY * 3)
        print("[${userState.username}]: ")
        val inputMessage = readLine() ?: ""
        if (inputMessage.equals("exit", true)) return
        try {
            processInputMessage(inputMessage, userState)
        } catch (e: Exception) {
            println("Error while sending: " + e.localizedMessage)
            return
        }
    }
}