import kotlin.math.cos

//demo
//fun main(){
//    val transactions = loadTransactions()
//
//    transactions.add(buildTransaction {
//        now()
//        cost(64800)//-¥648.00
//        operator = Operator("Him188","https://github.com/him188")
//        remark = "打游戏"
//    })
//
//    transactions.save()
//    transactions.computeCashFlow().saveTo(File(FILE_BASE + "test.md"))
//}


fun main() {
    add_trans()
    build()
}

val ALLOWED_PRINT_ENV = arrayOf("MIRAI_COST", "MIRAI_OP_NAME", "MIRAI_OP_LINK", "REMARK")
fun add_trans() {
    val env_map = System.getenv()
    val cost = env_map["MIRAI_COST"]
    val op_name = env_map["MIRAI_OP_NAME"]
    val op_link = env_map["MIRAI_OP_LINK"]
    val env_remark = env_map["REMARK"]
    if (cost == null || op_name == null || env_remark == null) {
        println("参数不足，跳过添加新记录")
        env_map.asSequence().filter { i -> i.key in ALLOWED_PRINT_ENV }.forEach { i ->
            println("${i.key} : ${i.value}")
        }
        return
    }
    val transactions = loadTransactions()
    val count =( cost.toFloat() * 100 ).toLong() // 钱很少应该不会有多少误差
    transactions.add(buildTransaction {
        now()
        income(count)
        operator = if (op_link.isNullOrEmpty()) {
            Operator(op_name, null)
        } else {
            Operator(op_name, op_link)
        }
        remark = env_remark
    })
    transactions.save()
}

fun build() {
    val transactions = loadTransactions()
    transactions.computeCashFlow().save()
}

class TransactionBuilder {

    var timeStamp: Long? = null

    fun now() {
        timeStamp = System.currentTimeMillis()
    }

    fun yesterday() {
        timeStamp = System.currentTimeMillis() - 24 * 60 * 60 * 1000
    }

    fun dayOf(year: Int, month: Int, day: Int) {
        timeStamp = simpleDateFormat().parse("${year}/${month}/${day}").time
    }

    var amount: Long? = null

    fun income(amount: Long) {
        this.amount = amount
    }

    fun sponsor(amount: Long) {
        this.amount = amount
        this.remark = "赞助"
    }

    fun outcome(amount: Long) {
        this.amount = -amount
    }

    fun cost(amount: Long) {
        this.amount = -amount
    }

    var operator: Operator? = null

    var remark: String? = null

    var proof: Proof? = null

    data class Proof(val caption: String, val link: String)


    fun build(): Transaction {
        return Transaction(
            timestamp = timeStamp ?: error("Timestamp can't be null"),
            operator = operator ?: error("Operator can't be null"),
            amount = amount ?: error("Amount can't be null"),
            attributes = mutableMapOf<TransactionAttribute, String>().apply {
                if (remark != null) {
                    put(TransactionAttribute.REMARK, remark!!)
                }
                if (proof != null) {
                    put(TransactionAttribute.PROOF_LINK, proof!!.link)
                    put(TransactionAttribute.PROOF_CAPTION, proof!!.caption)
                }
            }
        )
    }
}


fun buildTransaction(block: TransactionBuilder.() -> Unit): Transaction = TransactionBuilder().apply(block).build()
