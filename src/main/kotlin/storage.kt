import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File

val FILE_BASE = System.getProperty("user.dir") + "/"

val CASH_FLOW_JSON = FILE_BASE + "CashFlow.json"
val CASH_FLOW_MD = FILE_BASE + "CashFlowStatement.md"


fun loadTransactions():MutableList<Transaction> = File(CASH_FLOW_JSON).deserializeList()

fun saveTransactions(transaction: List<Transaction>) = File(CASH_FLOW_JSON).writeData(transaction)

fun List<Transaction>.save() = saveTransactions(this)

fun CashFlowStatement.save() = saveTo(File(CASH_FLOW_MD))

fun CashFlowStatement.saveTo(target:File) = target.writeText(this.statement.toString())

val Json = Json {
    this.ignoreUnknownKeys = true
    this.isLenient = true
    this.encodeDefaults = true
}

inline fun <reified T : Any> File.deserialize(defaultCreator:() -> T): T{
    if(!this.exists()){
        return defaultCreator()
    }
    val text = this.readText()
    if(text.isEmpty()){
        return defaultCreator()
    }
    return Json.decodeFromString(text)
}

inline fun <reified T:Any> File.deserializeList():MutableList<T>{
    return deserialize{ mutableListOf() }
}

inline fun <reified T:Any> File.writeData(data: T){
    this.writeText(data.serialize(Json))
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> T.serialize(format: StringFormat, serializer: KSerializer<T> = format.serializersModule.serializer()): String {
    return format.encodeToString(serializer, this)
}

