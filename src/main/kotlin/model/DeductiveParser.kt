package model

import com.github.h0tk3y.betterParse.utils.Tuple3
import java.util.*

class DeductiveParser(
    private val initial: Int,
    private val accessRulesBySecondNtOnRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessRulesByFirstNtOnRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessChainRulesByNtRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessRulesByTerminal: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    initialArraySize: Int = 100_000,
) {

    private val queue = PriorityQueue(100, compareBy<Item> {  it.wt }.reversed())
    private val accessFoundItemsFromLeft =
        HashMap<Pair<Int, Int>, MutableMap<Int, Item>>(initialArraySize)
    private val accessFoundItemsFromRight =
        HashMap<Pair<Int, Int>, MutableMap<Int, Item>>(initialArraySize)

    fun weightedDeductiveParsing(sentence: IntArray): Pair<IntArray, Item?> {
        fillQueueWithItemsFromLexicalRules(sentence)
            while (queue.isNotEmpty()) {
                val selectedItem: Item = queue.poll()
                if (selectedItem.i == 0 && selectedItem.nt == initial && selectedItem.j == sentence.size){
                    return sentence to selectedItem
                }
                if (addSelectedItemProbabilityToSavedItems(selectedItem)) continue
                findRulesAddItemsToQueueSecondNtOnRhs(selectedItem)
                findRulesAddItemsToQueueFirstNtOnRhs(selectedItem)
                findRulesAddItemsToQueueChain(selectedItem)
            }
            return sentence to null
    }

    fun fillQueueWithItemsFromLexicalRules(sentence: IntArray) {
        sentence.forEachIndexed { index, word ->
            accessRulesByTerminal[word]?.forEach { (lhs, rhs, ruleProbability) ->
                queue.add(Item(index, lhs, index + 1, ruleProbability, null))
            }
        }
    }

    //Zeile 8
    fun addSelectedItemProbabilityToSavedItems(selectedItem: Item): Boolean {
        var notNullProbabilityEntryLhs = false
        var notNullProbabilityEntryRhs = false

        accessFoundItemsFromLeft.compute(Pair(selectedItem.i, selectedItem.nt)) outerCompute@{ _, v ->
            if (v == null) {
                mutableMapOf(selectedItem.j to selectedItem)
            } else {
                v.compute(selectedItem.j) { _, presentItem ->
                    when {
                        (presentItem?.wt ?: 0.0) >= selectedItem.wt -> {
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
        accessFoundItemsFromRight.compute(Pair(selectedItem.nt, selectedItem.j)) outerCompute@ { _, v ->
            if (v == null) {
                mutableMapOf(selectedItem.i to selectedItem)
            } else {
                v.compute(selectedItem.i) { _, presentItem ->
                    when {
                        (presentItem?.wt ?: 0.0) >= selectedItem.wt -> {
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
    fun findRulesAddItemsToQueueSecondNtOnRhs(selectedItem: Item) {
        accessRulesByFirstNtOnRhs[selectedItem.nt]?.forEach { (lhs, rhs, ruleProbability) ->
            accessFoundItemsFromLeft[Pair(
                selectedItem.j,
               rhs.component2()
            )]?.forEach { (_, combinationItem) -> // --> j == i2
                if (rhs.component2() == combinationItem.nt && selectedItem.j < combinationItem.j && combinationItem.wt > 0.0) {
                    queue.add(
                        Item(
                            selectedItem.i, lhs, combinationItem.j, ruleProbability * selectedItem.wt * combinationItem.wt, listOf(selectedItem, combinationItem)
                        )
                    )
                }
            }
        }
    }

    //Zeile 10
    fun findRulesAddItemsToQueueFirstNtOnRhs(selectedItem: Item) {
        accessRulesBySecondNtOnRhs[selectedItem.nt]?.forEach { (lhs, rhs, ruleProbability) ->
            accessFoundItemsFromRight[Pair(
                rhs.component1(),
                selectedItem.i
            )]?.forEach { (_, combinationItem) ->    // --> j0 = i
                if (rhs.component1() == combinationItem.nt && combinationItem.i < selectedItem.i && combinationItem.wt > 0.0) {
                    queue.add(
                        Item(
                            combinationItem.i,
                            lhs,
                            selectedItem.j,
                            ruleProbability * combinationItem.wt * selectedItem.wt,
                            listOf(combinationItem, selectedItem)
                        )
                    )
                }
            }
        }
    }
    //Zeile 11
    fun findRulesAddItemsToQueueChain(selectedItem: Item) {
        accessChainRulesByNtRhs[selectedItem.nt]?.forEach { (lhs, rhs, ruleProbability) ->
            queue.add(
                Item(
                    selectedItem.i,
                    lhs,
                    selectedItem.j,
                    ruleProbability * selectedItem.wt,
                    listOf(selectedItem)
                )
            )
        }
    }
}