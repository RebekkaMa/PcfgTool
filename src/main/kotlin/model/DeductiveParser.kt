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
    private var queue = MinMaxPriorityQueue.orderedBy(compareBy<Pair<Item, Double>> { (_, weight) -> weight })
        .expectedSize(rankBeam ?: 100).create<Pair<Item, Double>>()
    private val accessFoundItemsFromLeft =
        HashMap<Pair<Int, Int>, MutableMap<Int, Item>>(initialArraySize)
    private val accessFoundItemsFromRight =
        HashMap<Pair<Int, Int>, MutableMap<Int, Item>>(initialArraySize)

    fun weightedDeductiveParsing(sentence: IntArray): Pair<IntArray, Item?> {
        fillQueueWithItemsFromLexicalRules(sentence)
        var long : Long = 0
        while (queue.isNotEmpty()) {
            val (selectedItem, _) = queue.pollLast()
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
                queue.offer(
                    Item(index, lhs, index + 1, ruleProbability, null) to (outsideScores?.let { ruleProbability * (it[lhs] ?: throw Exception())} ?: ruleProbability)
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
                    insertItemToQueue(
                        Item(
                            selectedItem.i,
                            lhs,
                            combinationItem.j,
                            ruleProbability * selectedItem.wt * combinationItem.wt,
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
                    insertItemToQueue(
                        Item(
                            combinationItem.i,
                            lhs,
                            selectedItem.j,
                            newProbability,
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
            insertItemToQueue(
                Item(
                    selectedItem.i,
                    lhs,
                    selectedItem.j,
                    ruleProbability * selectedItem.wt,
                    listOf(selectedItem)
                ), thresholdBeam, rankBeam
            )
        }
    }

    fun prune(thresholdBeam: Double?, rankBeam: Int?) {
        if (thresholdBeam != null && !queue.isEmpty()) {
            val m = queue.peekLast()!!.second
            while (!queue.isEmpty() && queue.peekFirst()!!.second <= m) {
                queue.pollFirst()
            }
        }
        if (rankBeam != null && !queue.isEmpty()) {
            while (queue.size > rankBeam) {
                queue.pollFirst()
            }
        }
    }

    fun insertItemToQueue(item: Item, thresholdBeam: Double?, rankBeam: Int?) {

        fun thresholdBeam() {
            if (item.wt > ((queue.peekLast()?.second ?: 0.0) * thresholdBeam!!)) {
                queue.offer(
                    item to (outsideScores?.let { item.wt * (it[item.nt] ?: throw Exception())} ?: item.wt)
                )
            }
        }

        fun rankBeam() {
            if (queue.size < rankBeam!!) {
                queue.offer(
                    item to (outsideScores?.let { item.wt * (it[item.nt] ?: throw Exception())} ?: item.wt)
                )
            } else
                if ((queue.peekFirst()?.second ?: 0.0) < item.wt) {
                    queue.pollFirst()
                    queue.offer(
                        item to (outsideScores?.let { item.wt * (it[item.nt] ?: throw Exception())} ?: item.wt)
                    )
                }
        }

        when {
            queue.isEmpty() -> queue.offer(
                item to (outsideScores?.let { item.wt * (it[item.nt] ?: throw Exception())} ?: item.wt)
            ) //TODO
            thresholdBeam != null && rankBeam != null -> {
                if (item.wt > queue.peekLast()?.second!!) {
                    rankBeam()
                }
            }
            thresholdBeam != null -> thresholdBeam()
            rankBeam != null -> rankBeam()
            else -> {
                queue.offer(
                    item to (outsideScores?.let { item.wt * (it[item.nt] ?: throw Exception())} ?: item.wt)
                )
            }
        }
    }
}