import kotlinx.serialization.Serializable
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import kotlin.math.absoluteValue


@Serializable
data class Operator(
    val identifier: String,//Name, such as "Him188"
    val contact: String?,  //Contact, such as "https://gayhub.com/Ryoii"
)

@Serializable
data class Statement(
    val timestamp: Long,
    val amount: Long,      //The amount of money, example: ¥39.39 = 3939L
    val operator: Operator,
    val attributes: Map<StatementAttribute,String>,//attributes of this statement
):Comparable<Statement>{
    //natural order: old to new
    override fun compareTo(other: Statement): Int {
        return (other.timestamp - this.timestamp).toInt()
    }
}

@Serializable
enum class StatementAttribute(
){
    REMARK,
    PROVE_LINK,
    PROVE_NAME
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


fun Long.interpretAsMoney():String{
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

private class CashFlowStatementBuilder:CashFlowStatement{
    override var currentCash = 0L
    override val statement = StringBuilder()
}

const val DATE_FORMAT = "yyyy/MM/dd"
const val HEAD_TEMPLATE = """
### Net Cash(Current Cash): ¥{currentCash}

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

private fun CashFlowStatementBuilder.compute(statement: Statement){
    currentCash+=statement.amount
    with(this.statement){
        append("| ")
        val simpleDateFormat = SimpleDateFormat(DATE_FORMAT)
        append(simpleDateFormat.format(statement.timestamp))
        append(" | ")
        append(statement.operator.toTag())
        append(" | ")
        append(statement.amount.interpretAsMoney())
        append(" | ")
        append(statement.attributes[StatementAttribute.REMARK]?:"none")

        val prove = statement.attributes[StatementAttribute.PROVE_NAME]
        if(prove != null){
            append("<a href=\"")
            append(statement.attributes[StatementAttribute.PROVE_LINK]?:"#")
            append("\">(")
            append(prove)
            append(")</a>")
        }
        append(" | ")
        append(currentCash.interpretAsMoney())
        append(" | ")
        appendLine()
    }
}


fun List<Statement>.computeCashFlow(initialCurrentCash:Long = 0L):CashFlowStatement{
    return CashFlowStatementBuilder().also{builder ->
        builder.currentCash = initialCurrentCash
        this.sorted().forEach {
            builder.compute(it)
        }
        builder.statement.insert(0,HEAD_TEMPLATE.replace("{currentCash}",builder.currentCash.interpretAsMoney()))
    }
}


fun main(){
    val item = listOf (
        Statement(
            System.currentTimeMillis(), -19300, Operator("Him188", "https://github.com/him188"), mapOf(
                StatementAttribute.REMARK to "打游戏",
                StatementAttribute.PROVE_NAME to "截图",
                StatementAttribute.PROVE_LINK to "https://a/b.jpg"
            )
        ),
        Statement(
            System.currentTimeMillis() - 1000000000000, -19300, Operator("Him288", "https://github.com/him188"), mapOf(
                StatementAttribute.REMARK to "打游戏",
            )
        )
    )

    println(item.computeCashFlow().statement)
}


