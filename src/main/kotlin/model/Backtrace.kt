package model

data class Backtrace(val lhs : Int, val chain: Pair<Backtrace, Backtrace?>?) {

    fun getParseTreeAsString(sentence : List<String>, lexicon: Map<Int, String>): String {

        var i = -1
        fun getPartOfTree(bt : Backtrace): String {
            return when {
                bt.chain == null ->{ i++; "(" + lexicon[bt.lhs] + " " + sentence[i] + ")" }
                bt.chain.second == null -> "(" + lexicon[bt.lhs] + " " + getPartOfTree(bt.chain.first) + ")"
                else -> "(" + lexicon[bt.lhs]  + " " + getPartOfTree(bt.chain.first) + " " + getPartOfTree(bt.chain.second!!) + ")"
            }
        }
        return getPartOfTree(this)
    }
}