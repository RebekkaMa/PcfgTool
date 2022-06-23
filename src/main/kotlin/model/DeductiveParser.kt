package model

import com.github.h0tk3y.betterParse.utils.Tuple3
import org.jetbrains.kotlin.com.google.common.collect.MinMaxPriorityQueue

class DeductiveParser(
    private val initial: Int,
    private val accessRulesBySecondNtOnRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessRulesByFirstNtOnRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessChainRulesByNtRhs: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val accessRulesByTerminal: Map<Int, List<Tuple3<Int, IntArray, Double>>>,
    private val outsideScores: Map<Int, Double>?,
    private val thresholdBeam: Double?,
    private val rankBeam: Int?,
    initialArraySize: Int = 100_000,
) {
    private var queue = MinMaxPriorityQueue.expectedSize(rankBeam ?: 100).create<Item>()
    private val accessFoundItemsFromLeft =
        HashMap<Pair<Int, Int>, MutableMap<Int, Item>>(initialArraySize)
    private val accessFoundItemsFromRight =
        HashMap<Pair<Int, Int>, MutableMap<Int, Item>>(initialArraySize)

    fun weightedDeductiveParsing(sentence: IntArray): Pair<IntArray, Item?> {
        fillQueueWithItemsFromLexicalRules(sentence)
        var long: Long = 0
        while (queue.isNotEmpty()) {
            val selectedItem = queue.pollLast()
            if (selectedItem.i == 0 && selectedItem.nt == initial && selectedItem.j == sentence.size) {
                println("Long " + long)
                return sentence to selectedItem
            }
            if (addSelectedItemProbabilityToSavedItems(selectedItem)) continue
            findRulesAddItemsToQueueSecondNtOnRhs(selectedItem)
            findRulesAddItemsToQueueFirstNtOnRhs(selectedItem)
            findRulesAddItemsToQueueChain(selectedItem)
            prune(thresholdBeam = thresholdBeam, rankBeam = rankBeam)
            long++
        }
        println("Long " + long)

        return sentence to null
    }

    fun fillQueueWithItemsFromLexicalRules(sentence: IntArray) {
        sentence.forEachIndexed { index, word ->
            accessRulesByTerminal[word]?.forEach { (lhs, rhs, ruleProbability) ->
                val comparisonValue =
                    (outsideScores?.let { ruleProbability * (it[lhs] ?: throw Exception()) } ?: ruleProbability)
                queue.offer(
                    Item(index, lhs, index + 1, ruleProbability, comparisonValue, null)
                )
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
        accessFoundItemsFromRight.compute(Pair(selectedItem.nt, selectedItem.j)) outerCompute@{ _, v ->
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
                    val newProbability = ruleProbability * selectedItem.wt * combinationItem.wt
                    val comparisonValue =
                        (outsideScores?.let { newProbability * (it[lhs] ?: throw Exception()) } ?: newProbability)
                    insertItemToQueue(
                        Item(
                            selectedItem.i,
                            lhs,
                            combinationItem.j,
                            ruleProbability * selectedItem.wt * combinationItem.wt,
                            comparisonValue,
                            listOf(selectedItem, combinationItem)
                        ), thresholdBeam, rankBeam
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
                    val newProbability = ruleProbability * combinationItem.wt * selectedItem.wt
                    val comparisonValue =
                        (outsideScores?.let { newProbability * (it[lhs] ?: throw Exception()) } ?: newProbability)
                    insertItemToQueue(
                        Item(
                            combinationItem.i,
                            lhs,
                            selectedItem.j,
                            newProbability,
                            comparisonValue,
                            listOf(combinationItem, selectedItem)
                        ), thresholdBeam, rankBeam
                    )
                }
            }
        }
    }

    //Zeile 11
    fun findRulesAddItemsToQueueChain(selectedItem: Item) {
        accessChainRulesByNtRhs[selectedItem.nt]?.forEach { (lhs, rhs, ruleProbability) ->
            val newProbability = ruleProbability * selectedItem.wt
            val comparisonValue =
                (outsideScores?.let { newProbability * (it[lhs] ?: throw Exception()) } ?: newProbability)
            insertItemToQueue(
                Item(
                    selectedItem.i,
                    lhs,
                    selectedItem.j,
                    newProbability,
                    comparisonValue,
                    listOf(selectedItem)
                ), thresholdBeam, rankBeam
            )
        }
    }

    fun prune(thresholdBeam: Double?, rankBeam: Int?) {
        if (queue.isNotEmpty()) {
            if (thresholdBeam != null) {
                val m = queue.peekLast()!!.comparisonValue
                while (!queue.isEmpty() && queue.peekFirst()!!.comparisonValue <= m) {
                    queue.pollFirst()
                }
            }
            if (rankBeam != null) {
                while (queue.size > rankBeam) {
                    queue.pollFirst()
                }
            }
        }

    }

    fun insertItemToQueue(newItem: Item, thresholdBeam: Double?, rankBeam: Int?) {

        val isItemOverThresholdBeam = { newItem.comparisonValue > ((queue.peekLast()?.comparisonValue ?: 0.0)) }
        val isMinOfQueueLessThanNewItem =
            {
                (queue.peekFirst()?.comparisonValue
                    ?: throw Exception("Internal Error: insertItemToQueue function - 1")) < newItem.comparisonValue
            }

        fun thresholdBeam() {
            if (isItemOverThresholdBeam()) queue.offer(newItem)
        }

        fun rankBeam() {
            if (queue.size < rankBeam!!) {
                queue.offer(newItem)
            } else
                if (isMinOfQueueLessThanNewItem()) {
                    queue.pollFirst()
                    queue.offer(newItem)
                }
        }

        when {
            queue.isEmpty() -> queue.offer(newItem) //TODO
            thresholdBeam != null && rankBeam != null -> {
                if (isItemOverThresholdBeam()) {
                    rankBeam()
                }
            }
            thresholdBeam != null -> thresholdBeam()
            rankBeam != null -> rankBeam()
            else -> queue.offer(newItem)

        }
    }
}