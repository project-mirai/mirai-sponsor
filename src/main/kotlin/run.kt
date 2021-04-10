import java.io.File

//demo
fun main(){
    val transactions = loadTransactions()

    transactions.add(buildTransaction {
        now()
        cost(64800)//-¥648.00
        operator = Operator("Him188","https://github.com/him188")
        remark = "打游戏"
    })

    transactions.save()
    transactions.computeCashFlow().saveTo(File(FILE_BASE + "test.md"))
}


class TransactionBuilder{

    var timeStamp:Long? = null

    fun now(){
        timeStamp = System.currentTimeMillis()
    }

    fun yesterday(){
        timeStamp = System.currentTimeMillis() - 24*60*60*1000
    }

    var amount:Long? = null

    fun income(amount:Long){
        this.amount = amount
    }

    fun outcome(amount:Long){
        this.amount = -amount
    }

    fun cost(amount:Long){
        this.amount = -amount
    }

    var operator: Operator? = null

    var remark:String? = null

    var proof:Proof? = null

    data class Proof(val caption:String, val link:String)


    fun build():Transaction{
        return Transaction(
            timestamp = timeStamp?: error("Timestamp can't be null"),
            operator = operator?: error("Operator can't be null"),
            amount = amount?: error("Amount can't be null"),
            attributes = mutableMapOf<TransactionAttribute,String>().apply{
                if(remark != null){
                    put(TransactionAttribute.REMARK,remark!!)
                }
                if(proof != null){
                    put(TransactionAttribute.PROOF_LINK,proof!!.link)
                    put(TransactionAttribute.PROOF_CAPTION,proof!!.caption)
                }
            }
        )
    }
}


fun buildTransaction(block: TransactionBuilder.() -> Unit):Transaction = TransactionBuilder().apply(block).build()
