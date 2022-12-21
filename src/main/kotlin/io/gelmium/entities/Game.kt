package io.gelmium.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

data class PlayerState(val id: String) {

}
data class GameState(val id: String) {

}

enum class Status {
    IDLE, AWAITING_MSG, DISCONNECTED, ERROR
}


enum class AwaitMsgType {
    LOGIN, FIND_MATCH
}

data class UserConnectionState(val username: String, val password: String) {
    var uid: Int = 0
    var status: Status = Status.IDLE
    var awaitMsgType: AwaitMsgType? = null
    var sessionToken: String = ""
    var groupId: Int = 0
    var gameState: GameState? = null
}


@Serializable
@JsonClassDiscriminator("")
sealed class BaseRequestDataPayload(val api:String, val c:String )

@Serializable
class GameReq<T>(val reqData: T, val path: String)

@Serializable
class LoginPayload(val account: String, val pwd: String):BaseRequestDataPayload("user", "login")
// {"path":"login","reqData":{"api":"user","c":"login","account":"h.u.n.gtr.an222017@gmail.com","pwd":"..."}}

@Serializable
class GetBattleInfoPayload(val sessionToken: String) : BaseRequestDataPayload("battle", "getBattleInfo")
//{"path":"logic","reqData":{"api":"battle","c":"getBattleInfo","sessionToken":"0874716bc39dd7d9f7521b8ae8c0eba0"}}

@Serializable
class SingInStatuePayload(val sessionToken: String) : BaseRequestDataPayload("singin", "singInStatue")
//{"path":"logic","reqData":{"api":"singin","c":"singInStatue","sessionToken":"0874716bc39dd7d9f7521b8ae8c0eba0"}}

//{"path":"logic","reqData":{"api":"role","c":"syncRole","sessionToken":"a08a5baad82ebea5c6030a1d05d2e37b"}}
fun buildSyncRolePayload(sessionToken: String): JsonElement {
    return buildJsonObject {
        put("path", "logic")
        putJsonObject("reqData") {
            put("api", "role")
            put("c", "syncRole")
            put("sessionToken", sessionToken)
        }
    }
}

// Open Ranked Match interface
@Serializable
class GetRankInfoPayload(val sessionToken: String) : BaseRequestDataPayload("tianti", "info")
//^ {"path":"logic","reqData":{"api":"tianti","c":"info","sessionToken":"90f986eff41bbd4c5ad763ca8ebcbe28"}}


@Serializable
class GetRankRewardPayload(val sessionToken: String) : BaseRequestDataPayload("tianti", "getReward")
//{"path":"logic","reqData":{"api":"tianti","c":"getReward","sessionToken":"d5d994a9b30eb5ccc4f8dbc4c0f14897"}}


//find match
@Serializable
class FindRankMatchPayload(val sessionToken: String, val groupId: Int) : BaseRequestDataPayload("tianti", "match")
// {"path":"logic","reqData":{"api":"tianti","c":"match","group_id":1657093233,"sessionToken":"90f986eff41bbd4c5ad763ca8ebcbe28"}}
fun buildFindRankMatchPayload(sessionToken: String, groupId: Int): JsonElement {
    return buildJsonObject {
        put("path", "logic")
        putJsonObject("reqData") {
            put("api", "tianti")
            put("c", "match")
            put("group_id", groupId)
            put("sessionToken", sessionToken)
        }
    }
}

// after found match
@Serializable
class SyncLoadRadioPayload(val sessionToken: String, val radio: Int) : BaseRequestDataPayload("battle", "syncLoadRadio")
//^ {"path":"logic","reqData":{"api":"battle","c":"syncLoadRadio","radio":1,"sessionToken":"90f986eff41bbd4c5ad763ca8ebcbe28"}}
fun buildSyncLoadRadioPayload(sessionToken: String, radio: Int = 1): JsonElement {
    return buildJsonObject {
        put("path", "logic")
        putJsonObject("reqData") {
            put("api", "battle")
            put("c", "syncLoadRadio")
            put("radio", radio)
            put("sessionToken", sessionToken)
        }
    }
}

// begin match: replace card
@Serializable
class CheckReplaceCardStatePayload(val sessionToken: String) : BaseRequestDataPayload("battle", "checkReplaceCardState")
//{"path":"logic","reqData":{"api":"battle","c":"checkReplaceCardState","sessionToken":"90f986eff41bbd4c5ad763ca8ebcbe28"}}
fun buildCheckReplaceCardStatePayload(sessionToken: String): JsonElement {
    return buildJsonObject {
        put("path", "logic")
        putJsonObject("reqData") {
            put("api", "battle")
            put("c", "checkReplaceCardState")
            put("sessionToken", sessionToken)
        }
    }
}

@Serializable
class ReplaceCardPayload(val sessionToken: String, val index: List<Int> = listOf()) : BaseRequestDataPayload("battle", "replaceCard")
//{"path":"logic","reqData":{"api":"battle","c":"replaceCard","index":[],"sessionToken":"90f986eff41bbd4c5ad763ca8ebcbe28"}}
fun buildReplaceCardPayload(sessionToken: String, index: List<Int> = listOf()): JsonElement {
    return buildJsonObject {
        put("path", "logic")
        putJsonObject("reqData") {
            put("api", "battle")
            put("c", "replaceCard")
            putJsonArray("index") { index.forEach { add(it) } }
            put("sessionToken", sessionToken)
        }
    }
}

@Serializable
class UpCardPayload(val sessionToken: String, val heroList: List<List<Int>>, val burnCard: Int, val burnCardIndex:Int, val magicList: List<List<Int>> = listOf()) : BaseRequestDataPayload("battle", "upCard")
//{"path":"logic","reqData":{"api":"battle","c":"upCard","heroList":[[20711000071,0,0,1],[20711000021,0,1,0]],"magicList":[],"sessionToken":"90f986eff41bbd4c5ad763ca8ebcbe28"}}
//{"path":"logic","reqData":{"api":"battle","c":"upCard","heroList":[[20311000011,0,2,0]],"magicList":[],"burnCard":20311000031,"burnCardIndex":1,"sessionToken":"90f986eff41bbd4c5ad763ca8ebcbe28"}}


@Serializable
class FightPayload(val sessionToken: String) : BaseRequestDataPayload("battle", "fight")
//^ {"path":"logic","reqData":{"api":"battle","c":"fight","sessionToken":"90f986eff41bbd4c5ad763ca8ebcbe28"}}

// login Response

@Serializable
data class Group(val group_id: Int) {
    val uid: Int = 0
    val name: String = ""
}

@Serializable
data class LoginData(val address: String) {
    val loginCount: Int = 0
    val groups: HashMap<Int, Group> = hashMapOf()
}

@Serializable
data class LoginResponse(val state: Int) {
    val uid: Int = 0
    val sessionToken: String = ""
    val loginData: LoginData = LoginData("")
    val st: Int = 0
}

// common response
@Serializable
data class CommonResponse(val state: Int) {
    val st: Int = 0
}


// match response

@Serializable
data class BattleGround(val id: String) {
    val type: Int = 0
    val round: Int = 0
    val lost: Int = 0
    val phase: Int = 0
    val ct: Int = 0
    val record: Int = 0
}

@Serializable
data class PlayerHeroData(val entity_id: String) {
    val card_id: Int = 0
    val hp: Int = 0
    val atk: Int = 0
    val up_round: Int = 0
    val ab: Int = 0
    val sab: Int = 0
    val type: Int = 0
    val state: Int = 0
}

@Serializable
data class PlayerInitialMatchData(val uid: Int) {
    val battle_id: String = ""
    val round: Int = 0
    val ai: Int = 0
    val pos: Int = 0
    val mp: Int = 0
    val burnCard: Int = 0
    val max_add_mp: Int = 0
    val name: String = ""
    val master_card: Int = 0
    val national_flag: Int = 0
    val card: List<Int> = listOf()
    val card_queue: List<Int> = listOf()
    val magic: List<Int> = listOf()
    val race: List<Int> = listOf()
    val card_mp: List<Int> = listOf()
    val dc_cards: List<Int> = listOf()
    val hero: PlayerHeroData = PlayerHeroData("")
}

@Serializable
data class FoundMatchResponse(val cmd: String) {
    val ai: Int = 0
    val battleGround: BattleGround? = null
    val player0: PlayerInitialMatchData? = null
    val player1: PlayerInitialMatchData? = null
}


