import kotlinx.serialization.Serializable
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue


@Serializable
data class Operator(
    val identifier: String,//Name, such as "Him188"
    val contact: String?,  //Contact, such as "https://gayhub.com/Ryoii"
)

@Serializable
data class Transaction(
    val timestamp: Long,
    val amount: Long,      //The amount of money, example: ¥39.39 = 3939L
    val operator: Operator,
    val attributes: Map<TransactionAttribute,String>,//attributes of this statement
)

@Serializable
enum class TransactionAttribute(
) {
    REMARK,
    PROOF_LINK,
    PROOF_CAPTION
}


fun Operator.toTag():String{
    //<a href="https://github.com/mzdluo123">RainChan</a>

    return buildString {
        append("<a")
        if(contact == null){
            append(" anonymous")
        }else{
            append(" href=\"")
            append(contact)
            append("\"")
        }
        append(">")
        append(identifier)
        append("</a>")
    }
}

/**
 * Convert Money in Long to Human Readable Language
 */
fun Long.formatMoney():String{
    val long = this
    return buildString {
        //add correspond colors later ;)
        if(long >= 0L){
            append("+")
        }else{
            append("-")
        }
        val stringify = long.absoluteValue.toString()
        when(stringify.length){
            0 -> {
                throw IllegalArgumentException()
            }
            1 -> {
                append("0.0")
                append(stringify)
            }
            2 -> {
                append("0.")
                append(stringify)
            }
            else -> {
                append(stringify.substring(0 .. stringify.length - 3))
                append(".")
                append(stringify.substring(stringify.length - 2))
            }
        }
    }
}


interface CashFlowStatement{
    val currentCash:Long
    val statement:CharSequence
}

internal class CashFlowStatementBuilder:CashFlowStatement{
    override var currentCash = 0L
    override val statement = StringBuilder()
}

const val DATE_FORMAT = "yyyy/MM/dd"
fun simpleDateFormat():SimpleDateFormat =  SimpleDateFormat(DATE_FORMAT).apply {
    timeZone = TimeZone.getTimeZone("Asia/Shanghai")
}


const val HEAD_TEMPLATE = """
### Net Cash(Current Cash): ¥{currentCash}
### Last Update: {lastUpdate} (UTC+8)

 <ul>
  <li>
    所有花销操作需提供实名操作(负责)人
  </li>
  <li>
  赞助栏中操作人即为赞助人, 如没有链接则为匿名赞助
  </li>
</ul>

---

|  日期      | 操作人          |  资金流(¥)   | 详情 |  Net Cash(¥) |
| :-----    | :----           | ----: |:---- |----: |
"""

internal fun CashFlowStatementBuilder.compute(transaction: Transaction){
    currentCash+=transaction.amount
    with(this.statement){
        append("| ")
        val simpleDateFormat = simpleDateFormat()
        append(simpleDateFormat.format(transaction.timestamp))
        append(" | ")
        append(transaction.operator.toTag())
        append(" | ")
        append(transaction.amount.formatMoney())
        append(" | ")
        append(transaction.attributes[TransactionAttribute.REMARK]?:"none")

        val prove = transaction.attributes[TransactionAttribute.PROOF_CAPTION]
        if(prove != null){
            append("<a href=\"")
            append(transaction.attributes[TransactionAttribute.PROOF_LINK]?:"#")
            append("\">(")
            append(prove)
            append(")</a>")
        }
        append(" | ")
        append(currentCash.formatMoney())
        append(" | ")
        appendLine()
    }
}

fun List<Transaction>.computeCashFlow(initialCurrentCash:Long = 0L):CashFlowStatement{
    return CashFlowStatementBuilder().also{builder ->
        builder.currentCash = initialCurrentCash
        this.sortedBy { it.timestamp }.forEach {
            builder.compute(it)
        }

        val lastUpdate = simpleDateFormat().format(System.currentTimeMillis())

        builder.statement.insert(0,HEAD_TEMPLATE
            .replace("{currentCash}",builder.currentCash.formatMoney())
            .replace("{lastUpdate}",lastUpdate))
    }
}





