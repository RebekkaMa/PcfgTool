package model

class Item(val i: Int, val nt: Int, val j: Int, var wt: Double, var bt: List<Item>?){

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


}