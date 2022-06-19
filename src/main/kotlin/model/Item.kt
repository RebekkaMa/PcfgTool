package model

class Item(val i: Int, val nt: Int, val j: Int, var wt: Double, var bt: List<Item>?) : Comparable<Item>{

    fun getParseTreeAsString(sentence : List<String>, lexicon: Map<Int, String>): String {
        var i = -1
        fun getPartOfTree(item : Item): String {
            return when {
                item.bt == null ->{ i++; "(" + lexicon[item.nt] + " " + sentence[i] + ")" }
                item.bt!!.size == 1 -> "(" + lexicon[item.nt] + " " + getPartOfTree(item.bt!![0]) + ")"
                else -> "(" + lexicon[item.nt]  + " " + getPartOfTree(item.bt!![0]) + " " + getPartOfTree(item.bt!![1]) + ")"
            }
        }
        return getPartOfTree(this)
    }

//    override fun compareTo(other: Item): Int =
//        compareValues(this.wt, other.wt)

    override fun compareTo(other: Item): Int {
        return when {
           this.wt > other.wt -> 1
            this.wt < other.wt -> -1
            else -> 0
        }
    }
}