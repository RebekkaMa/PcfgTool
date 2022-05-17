package model

data class Backtrace(val rule: Rule,val probability: Double, val chain: Pair<Backtrace, Backtrace?>?) {

    fun getParseTreeAsString(): String {
        fun getPartOfTree(bt : Backtrace): String {
            return when {
                bt.chain == null -> "(" + bt.rule.lhs + " " + bt.rule.rhs.first() + ")"
                bt.chain.second == null -> "(" + bt.rule.lhs + " " + getPartOfTree(bt.chain.first) + ")"
                else -> "(" + bt.rule.lhs + " " + getPartOfTree(bt.chain.first) + " " + getPartOfTree(bt.chain.second!!) + ")"
            }
        }
        return getPartOfTree(this)
    }
}
