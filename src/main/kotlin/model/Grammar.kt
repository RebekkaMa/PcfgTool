package model

import Util.format
import com.github.h0tk3y.betterParse.utils.Tuple3
import com.github.h0tk3y.betterParse.utils.Tuple7
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class Grammar(val initial: String = "ROOT", val pRules: Map<Rule, Double>) {

    companion object {
        suspend fun createFromRules(initial: String = "ROOT", rules: List<Rule>): Grammar = coroutineScope {
            val absoluteRules = async { rules.groupingBy { it }.eachCount() }
            val lhsCount = async { rules.groupingBy { it.lhs }.eachCount() }
            val pcfgRules = absoluteRules.await()
                .mapValues { (rule, count) -> (count.toDouble() / (lhsCount.await().getOrElse(rule.lhs) { 1 })) }
            Grammar(initial, pRules = pcfgRules)
        }

        suspend fun create(initial: String?, rules: Map<Rule, Double>): Grammar = coroutineScope {
            if (initial == null) {
                Grammar(pRules = rules.toMap())
            } else {
                Grammar(initial, rules.toMap())
            }
        }
    }

    fun getTerminalsAsStrings(): List<String> {
        return pRules.keys.filter { it.lexical }.map { it.rhs.first() }.distinct()
    }

    fun getLexiconAsStrings(): List<String> {
        return pRules.filterKeys { it.lexical }
            .map { (rule, p) -> rule.lhs + " " + rule.rhs.joinToString(" ") + " " + p.format(15) }
    }

    fun getRulesAsStrings(): List<String> {
        return pRules.filterKeys { !it.lexical }
            .map { (rule, p) -> rule.lhs + " -> " + rule.rhs.joinToString(" ") + " " + p.format(15) }
    }

    fun getGrammarDataStructuresForParsing(): Tuple7<MutableMap<Int, MutableList<Tuple3<Int, IntArray, Double>>>, MutableMap<Int, MutableList<Tuple3<Int, IntArray, Double>>>, MutableMap<Int, MutableList<Tuple3<Int, IntArray, Double>>>, MutableMap<Int, MutableList<Tuple3<Int, IntArray, Double>>>, Map<Int, String>, Map<String, Int>, Int> {

        val lexiconByKey = kotlin.collections.HashMap<Int, String>()
        val lexiconByString = kotlin.collections.HashMap<String, Int>()

        lexiconByKey[0] = initial
        lexiconByString[initial] = 0

        var index = 0
        var numberOfTerminals = 0

        val accessRulesBySecondNtOnRhs = mutableMapOf<Int, MutableList<Tuple3<Int, IntArray, Double>>>()
        val accessRulesByFirstNtOnRhs = mutableMapOf<Int, MutableList<Tuple3<Int, IntArray, Double>>>()
        val accessChainRulesByNtRhs = mutableMapOf<Int, MutableList<Tuple3<Int, IntArray, Double>>>()
        val accessRulesByTerminal = mutableMapOf<Int, MutableList<Tuple3<Int, IntArray, Double>>>()


        this.pRules.forEach { (rule, ruleProbability) ->
            val lhsHash = lexiconByString.getOrPut(rule.lhs) {
                index += 1
                lexiconByKey[index] = rule.lhs
                index
            }
            val rhsHashList = rule.rhs.map {
                lexiconByString.getOrPut(it) {
                    index += 1
                    lexiconByKey[index] = it
                    index
                }
            }.toIntArray()

            val newTuple = Tuple3(lhsHash, rhsHashList, ruleProbability)

            when (rule.rhs.size) {
                2 -> {
                    accessRulesByFirstNtOnRhs.addTuple(rhsHashList[0], newTuple)
                    accessRulesBySecondNtOnRhs.addTuple(rhsHashList[1], newTuple)
                }
                1 -> {
                    if (!rule.lexical) {
                        accessChainRulesByNtRhs.addTuple(rhsHashList[0], newTuple)
                        numberOfTerminals += 1
                    } else {
                        accessRulesByTerminal.addTuple(rhsHashList[0], newTuple)
                    }
                }
                else -> {
                }
            }
        }
        return Tuple7(
            accessRulesBySecondNtOnRhs,
            accessRulesByFirstNtOnRhs,
            accessChainRulesByNtRhs,
            accessRulesByTerminal,
            lexiconByKey,
            lexiconByString,
            index + 1 - numberOfTerminals
        )
    }

    fun <K, V> MutableMap<K, MutableList<V>>.addTuple(key: K, item: V) {
        this.compute(key) { _, v ->
            if (v != null) {
                v.add(
                    item
                )
                v
            } else {
                mutableListOf(item)
            }
        }
    }

    fun inside(
        accessRulesFromLhsNonLexical: MutableMap<String, MutableList<Pair<Rule, Double>>>,
        accessRulesFromLhsLexical: MutableMap<String, MutableList<Pair<Rule, Double>>>
    ): MutableMap<String, Double> {

        val insideValues = mutableMapOf<String, Double>()
        accessRulesFromLhsLexical.forEach { (lhs, rules) ->
            insideValues[lhs] = rules.maxOf { it.second }
        }
        var converged :Boolean
        do {
            converged = true
            accessRulesFromLhsNonLexical.forEach { (lhs, rules) ->
                insideValues.compute(lhs) { _, oldInValue ->
                    val whk = rules.maxOf { (rule, probability) ->
                        rule.rhs.fold(probability) { acc, s ->
                            acc * insideValues.getOrDefault(s, 0.0)
                        }
                    }
                    val newInValue = maxOf(oldInValue ?: 0.0, whk)
                    converged = newInValue == oldInValue  && converged
                    newInValue
                }
            }
        } while (!converged)
        insideValues.forEach { println(it) }
        return insideValues
    }


    fun outside(
        insideValues: MutableMap<String, Double>,
        accessRulesFromRhs: MutableMap<String, MutableList<Tuple3<String, List<String>, Double>>>,
        nonTerminals: MutableSet<String>
    ): MutableMap<String, Double> {
        val outsideValues = mutableMapOf<String, Double>()
        outsideValues[initial] = 1.0
        val nonTerminalsWithoutInitial = nonTerminals.minus(initial)
        var converged :Boolean
        do {
            converged = true
            nonTerminalsWithoutInitial.forEach { nonTerminal ->
                outsideValues.compute(nonTerminal) { _, oldOutValue ->
                    val max = accessRulesFromRhs[nonTerminal]?.maxOf { (lhs, rhs, probability) ->
                        (outsideValues[lhs] ?: 0.0) * probability * (rhs.firstOrNull()?.let { insideValues.getOrDefault(it,0.0) } ?: 1.0)
                    }
                    val newOutValue = maxOf(oldOutValue ?: 0.0, max ?: 0.0)
                    converged = newOutValue == oldOutValue && converged
                    newOutValue
                }
            }
        } while (!converged)
    return outsideValues
} //TODO Immer nur binarisierte Grammatiken?

fun viterbiOutsideScore(): MutableMap<String, Double> {
    val accessRulesFromLhsNonLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
    val accessRulesFromRhs = mutableMapOf<String, MutableList<Tuple3<String,List<String>, Double>>>()
    val accessRulesFromLhsLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
    val nonTerminals = mutableSetOf<String>()

    pRules.forEach { (rule, ruleProbability) ->
        if (rule.lexical) {
            accessRulesFromLhsLexical.addTuple(rule.lhs, rule to ruleProbability)
        } else {
            accessRulesFromLhsNonLexical.addTuple(rule.lhs, rule to ruleProbability)
            rule.rhs.forEach {
                accessRulesFromRhs.addTuple(it, Tuple3(rule.lhs, rule.rhs.minus(it), ruleProbability))
                nonTerminals.add(it)
            }
        }
        nonTerminals.add(rule.lhs)
    }
    val insideValues = inside(accessRulesFromLhsNonLexical, accessRulesFromLhsLexical)
    return outside(
        insideValues,
        accessRulesFromRhs,
        nonTerminals
    )
}
override fun toString(): String {
    return pRules.map { (rule, p) -> rule.toString() + " " + p.format(15) }.joinToString("\n")
}
}