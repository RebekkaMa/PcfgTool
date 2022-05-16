package model

import com.github.h0tk3y.betterParse.utils.Tuple5
import java.util.SortedMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import kotlin.system.measureTimeMillis

class DeductiveParser(

    val initial: String,
    val accessRulesBySecondNtOnRhs: Map<String, MutableList<Pair<Rule, Double>>>,
    val accessRulesByFirstNtOnRhs: Map<String, MutableList<Pair<Rule, Double>>>,
    val accessChainRulesByNtRhs: Map<String, MutableList<Pair<Rule, Double>>>,
    val accessRulesByTerminal: Map<String, MutableList<Pair<Rule, Double>>>
) {

    val queue = PriorityBlockingQueue(100, compareBy<Tuple5<Int, String, Int, Double, Backtrace>> { it.t4 }.reversed())
    val accessFoundItemsFromLeft =
        hashMapOf<Pair<Int, String>, MutableMap<Int, Tuple5<Int, String, Int, Double, Backtrace>>>()
    val accessFoundItemsFromRight =
        hashMapOf<Pair<String, Int>, MutableMap<Int, Tuple5<Int, String, Int, Double, Backtrace>>>()


    fun weightedDeductiveParsing(sentence: List<String>): Pair<List<String>, Tuple5<Int, String, Int, Double, Backtrace>?> {

        try {
            fillQueueWithItemsFromLexicalRules(sentence)
            while (queue.isNotEmpty()) {
                val selectedItem: Tuple5<Int, String, Int, Double, Backtrace> = queue.poll()
                if (selectedItem.t1 == 0 && selectedItem.t2 == initial && selectedItem.t3 == sentence.size) return sentence to selectedItem
                if (addSelectedItemProbabilityToSavedItems(selectedItem)) continue
                findRulesAddItemsToQueueSecondNtOnRhs(selectedItem)
                findRulesAddItemsToQueueFirstNtOnRhs(selectedItem)
                findRulesAddItemsToQueueChain(selectedItem)
            }
            return sentence to null
        } finally {
            clearAll()
        }
    }

    fun fillQueueWithItemsFromLexicalRules(
        sentence: List<String>
    ) {
        sentence.forEachIndexed { index, word ->
            accessRulesByTerminal[word]?.forEach { (rule, ruleProbability) ->
                queue.add(Tuple5(index, rule.lhs, index + 1, ruleProbability, Backtrace(rule to ruleProbability, null)))
            }
        }
    }


    //Zeile 8
    fun addSelectedItemProbabilityToSavedItems(selectedItem: Tuple5<Int, String, Int, Double, Backtrace>): Boolean {
        var notNullProbabilityEntryLhs = false
        var notNullProbabilityEntryRhs = false
        accessFoundItemsFromLeft.compute(Pair(selectedItem.t1, selectedItem.t2)) { _, v ->
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
        accessFoundItemsFromRight.compute(Pair(selectedItem.t2, selectedItem.t3)) { _, v ->
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
    fun findRulesAddItemsToQueueSecondNtOnRhs(selectedItem: Tuple5<Int, String, Int, Double, Backtrace>) {
        val (i, nt, j, wt, bt) = selectedItem
        accessRulesByFirstNtOnRhs[nt]?.forEach { (rule, ruleProbability) ->
            accessFoundItemsFromLeft[Pair(
                j,
                rule.rhs.component2()
            )]?.forEach { _, (i2, nt2, j2, wt2, bt2) -> // --> j == i2
                if (rule.rhs.component2() == nt2 && j < j2) {
                    queue.add(
                        Tuple5(
                            i, rule.lhs, j2, ruleProbability * wt * wt2, Backtrace(
                                rule to ruleProbability * wt * wt2,
                                Pair(bt, bt2)
                            )
                        )
                    )
                }
            }
        }
    }

    //Zeile 10
    fun findRulesAddItemsToQueueFirstNtOnRhs(selectedItem: Tuple5<Int, String, Int, Double, Backtrace>) {
        val (i, nt, j, wt, bt) = selectedItem
        accessRulesBySecondNtOnRhs[nt]?.forEach { (rule, ruleProbability) ->
            accessFoundItemsFromRight[Pair(
                rule.rhs.component1(),
                i
            )]?.forEach { _, (i0, nt0, j0, wt0, bt0) ->    // --> j0 = i
                if (rule.rhs.component1() == nt0 && i0 < i) {
                    queue.add(
                        Tuple5(
                            i0,
                            rule.lhs,
                            j,
                            ruleProbability * wt0 * wt,
                            Backtrace(rule to ruleProbability * wt0 * wt, Pair(bt0, bt))
                        )
                    )
                }
            }
        }
    }

    //Zeile 11
    fun findRulesAddItemsToQueueChain(selectedItem: Tuple5<Int, String, Int, Double, Backtrace>) {
        val (i, nt, j, wt, bt) = selectedItem
        accessChainRulesByNtRhs[nt]?.forEach { (rule, ruleProbability) ->
            queue.add(
                Tuple5(
                    i,
                    rule.lhs,
                    j,
                    ruleProbability * wt,
                    Backtrace(rule to ruleProbability * wt, Pair(bt, null))
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