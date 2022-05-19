package model

import com.github.h0tk3y.betterParse.utils.Tuple3
import com.github.h0tk3y.betterParse.utils.Tuple5
import java.util.*
import java.util.concurrent.PriorityBlockingQueue

class DeductiveParser(

    val initial: Int,
    val accessRulesBySecondNtOnRhs: Map<Int, List<Tuple3<Int, List<Int>, Double>>>,
    val accessRulesByFirstNtOnRhs: Map<Int, List<Tuple3<Int, List<Int>, Double>>>,
    val accessChainRulesByNtRhs: Map<Int, List<Tuple3<Int, List<Int>, Double>>>,
    val accessRulesByTerminal: Map<Int, List<Tuple3<Int, List<Int>, Double>>>,
    val lexicon: Map<Int, String>
) {


    val queue = PriorityQueue(100, compareBy<Tuple5<Int, Int, Int, Double, Backtrace>> {  it.t4}.reversed())
    val accessFoundItemsFromLeft =
        hashMapOf<Pair<Int, Int>, MutableMap<Int, Tuple5<Int, Int, Int, Double, Backtrace>>>()
    val accessFoundItemsFromRight =
        hashMapOf<Pair<Int, Int>, MutableMap<Int, Tuple5<Int, Int, Int, Double, Backtrace>>>()


    fun weightedDeductiveParsing(sentence: List<Int>): Pair<List<Int>, Tuple5<Int, Int, Int, Double, Backtrace>?> {
        try {
            fillQueueWithItemsFromLexicalRules(sentence)
            while (queue.isNotEmpty()) {
                val selectedItem: Tuple5<Int, Int, Int, Double, Backtrace> = queue.poll()
                //println("Selected Item: " + "(" + selectedItem.t1 + ", " + lexicon[selectedItem.t2] + ", " + selectedItem.t3 + ")")
                if (selectedItem.t1 == 0 && selectedItem.t2 == initial && selectedItem.t3 == sentence.size) return sentence to selectedItem
                //println("queue:")
//                queue.forEach {
//                    println("(" + it.t1 + ", " + lexicon[it.t2]+ ", " + it.t3 + ", " + it.t4 + ")")
//                }
                if (addSelectedItemProbabilityToSavedItems(selectedItem)) continue
                //println("------c")
//                accessFoundItemsFromLeft.forEach{
//                    it.value.forEach {
//                       println( "(" + it.value.t1 + ", " + lexicon[it.value.t2]+ ", " +it.value.t3 + "," + it.value.t4 +  ")")
//                    }
//                }

                findRulesAddItemsToQueueSecondNtOnRhs(selectedItem)
                findRulesAddItemsToQueueFirstNtOnRhs(selectedItem)
                findRulesAddItemsToQueueChain(selectedItem)
                //println("------------------------------------")
            }
            return sentence to null
        } finally {
            clearAll()
        }
    }

    fun fillQueueWithItemsFromLexicalRules(
        sentence: List<Int>
    ) {
        sentence.forEachIndexed { index, word ->
            accessRulesByTerminal[word]?.forEach { (lhs, rhs, ruleProbability) ->
                queue.add(Tuple5(index, lhs, index + 1, ruleProbability, Backtrace(lhs, rhs , ruleProbability, null)))
            }
        }
    }


    //Zeile 8
    fun addSelectedItemProbabilityToSavedItems(selectedItem: Tuple5<Int, Int, Int, Double, Backtrace>): Boolean {
        var notNullProbabilityEntryLhs = false
        var notNullProbabilityEntryRhs = false

        accessFoundItemsFromLeft.compute(Pair(selectedItem.t1, selectedItem.t2)) outerCompute@{ _, v ->
            if (v == null) {
                mutableMapOf(selectedItem.t3 to selectedItem)
            } else {
                v.compute(selectedItem.t3) { _, presentItem ->
                    when {
                        (presentItem?.t4 ?: 0.0) >= selectedItem.t4 -> {
                            notNullProbabilityEntryLhs = true
                            return@compute presentItem
                        }
                        else -> return@compute selectedItem
                    }
                }
                v
            }
        }
        if (notNullProbabilityEntryLhs) return true
        accessFoundItemsFromRight.compute(Pair(selectedItem.t2, selectedItem.t3)) outerCompute@ { _, v ->
            if (v == null) {
                mutableMapOf(selectedItem.t1 to selectedItem)
            } else {
                v.compute(selectedItem.t1) { _, presentItem ->
                    when {
                        (presentItem?.t4 ?: 0.0) >= selectedItem.t4 -> {
                            notNullProbabilityEntryRhs = true
                            return@compute presentItem
                        }
                        else -> return@compute selectedItem
                    }
                }
                v
            }
        }
        if (notNullProbabilityEntryLhs != notNullProbabilityEntryRhs) throw Exception("Internal Error: itemsRight and itemsLeft not equal")
        return notNullProbabilityEntryRhs
    }

    //Zeile 9
    fun findRulesAddItemsToQueueSecondNtOnRhs(selectedItem: Tuple5<Int, Int, Int, Double, Backtrace>) {
        val (i, nt, j, wt, bt) = selectedItem
        accessRulesByFirstNtOnRhs[nt]?.forEach { (lhs, rhs, ruleProbability) ->
            accessFoundItemsFromLeft[Pair(
                j,
               rhs.component2()
            )]?.forEach { _, (i2, nt2, j2, wt2, bt2) -> // --> j == i2
                if (rhs.component2() == nt2 && j < j2) {
                    queue.add(
                        Tuple5(
                            i, lhs, j2, ruleProbability * wt * wt2, Backtrace(lhs, rhs, ruleProbability * wt * wt2,
                                Pair(bt, bt2)
                            )
                        )
                    )
                }
            }
        }
    }

    //Zeile 10
    fun findRulesAddItemsToQueueFirstNtOnRhs(selectedItem: Tuple5<Int, Int, Int, Double, Backtrace>) {
        val (i, nt, j, wt, bt) = selectedItem
        accessRulesBySecondNtOnRhs[nt]?.forEach { (lhs, rhs, ruleProbability) ->
            accessFoundItemsFromRight[Pair(
                rhs.component1(),
                i
            )]?.forEach { _, (i0, nt0, j0, wt0, bt0) ->    // --> j0 = i
                if (rhs.component1() == nt0 && i0 < i) {
                    queue.add(
                        Tuple5(
                            i0,
                            lhs,
                            j,
                            ruleProbability * wt0 * wt,
                            Backtrace(lhs, rhs, ruleProbability * wt0 * wt, Pair(bt0, bt))
                        )
                    )
                }
            }
        }
    }

    //Zeile 11
    fun findRulesAddItemsToQueueChain(selectedItem: Tuple5<Int, Int, Int, Double, Backtrace>) {
        val (i, nt, j, wt, bt) = selectedItem
        accessChainRulesByNtRhs[nt]?.forEach { (lhs, rhs, ruleProbability) ->
            queue.add(
                Tuple5(
                    i,
                    lhs,
                    j,
                    ruleProbability * wt,
                    Backtrace(lhs, rhs, ruleProbability * wt, Pair(bt, null))
                )
            )
        }
    }

    fun clearAll() {
        queue.clear()
        accessFoundItemsFromLeft.clear()
        accessFoundItemsFromRight.clear()
    }
}