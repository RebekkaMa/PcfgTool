package model

data class Backtrace(val bin: Pair<Rule, Double>, val chain: Pair<Backtrace, Backtrace?>?) {

    fun getParseTreeAsString(): String {
        fun getPartOfTree(bt : Backtrace): String {
            return when {
                bt.chain == null -> "(" + bt.bin.first.lhs + " " + bt.bin.first.rhs.first() + ")"
                bt.chain.second == null -> "(" + bt.bin.first.lhs + " " + getPartOfTree(bt.chain.first) + ")"
                else -> "(" + bt.bin.first.lhs + " " + getPartOfTree(bt.chain.first) + " " + getPartOfTree(bt.chain.second!!) + ")"
            }
        }
        return getPartOfTree(this)
    }
}
