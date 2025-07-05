package com.learn.cmp

import androidx.annotation.Keep
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun LnstowCmpTest() {
//    Text("lnstow cmp", Modifier.padding(200.dp))
    TestJson()
}


@Composable
fun TestJson() {
    val jsStr by remember { mutableStateOf("""{"ksk1":32,"ksk2":-34,"testC":["1","2"]}""") }
    var resStr by remember { mutableStateOf("json result") }

    Column {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "gson to json no keep", Modifier.clickable {
                kotlin.runCatching {
                    val obj = jsStr.ksFromJson<MutableMap<String, JsonElement?>>()
                    obj["ksk2"]= JsonPrimitive(-321)
                    obj["testD"] = JsonPrimitive("1")
                    resStr = obj.ksToJson()
                }.onFailure {
                    resStr = it.message ?: "json error1"
                }
            }
        )
//        Spacer(Modifier.height(8.dp))
//        Text(
//            text = "gson to json keep", Modifier.clickable {
//                kotlin.runCatching {
//                    val obj = jsStr.fromJson<AAAKeep>()
//                    obj.ksk2 = -321
//                    resStr = obj.toJson()
//                }.onFailure {
//                    resStr = it.message ?: "json error2"
//                }
//            }
//        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "ks to json", Modifier.clickable {
                kotlin.runCatching {
                    val obj = jsStr.ksFromJson<AAAKS>()
                    obj.ksk2 = -321
                    resStr = obj.ksToJson()
                }.onFailure {
                    resStr = it.message ?: "json error3"
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        Text(text = "  $resStr")
    }

}

@Serializable
class AAAKS(
    val ksk1: Int = -2,
    var ksk2: Int = -2,
)

@Keep
class AAAKeep(
    val ksk1: Int = -2,
    var ksk2: Int = -2,
)

class AAANoKeep(
    val ksk1: Int = -2,
    var ksk2: Int = -2,
)

val ksJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

inline fun <reified T> T.ksToJson(): String = ksJson.encodeToString<T>(this)
inline fun <reified T> String.ksFromJson(): T = ksJson.decodeFromString<T>(this)

typealias JsKeep = Serializable
typealias JsIgnore = Transient
typealias JsToName = SerialName

@OptIn(ExperimentalSerializationApi::class)
typealias JsFromName = JsonNames
