package model

import com.github.h0tk3y.betterParse.utils.Tuple3
import com.github.h0tk3y.betterParse.utils.Tuple7
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import utils.format

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
            if (initial == null) Grammar(pRules = rules.toMap()) else Grammar(initial, rules.toMap())
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
            val lhsAsInt = lexiconByString.getOrPut(rule.lhs) {
                lexiconByKey[++index] = rule.lhs
                index
            }
            val rhsAsInt = rule.rhs.map {
                lexiconByString.getOrPut(it) {
                    lexiconByKey[++index] = it
                    index
                }
            }.toIntArray()

            val newEntry = Tuple3(lhsAsInt, rhsAsInt, ruleProbability)

            when (rule.rhs.size) {
                2 -> {
                    accessRulesByFirstNtOnRhs.add(rhsAsInt[0], newEntry)
                    accessRulesBySecondNtOnRhs.add(rhsAsInt[1], newEntry)
                }
                1 -> {
                    if (!rule.lexical) {
                        accessChainRulesByNtRhs.add(rhsAsInt[0], newEntry)
                        numberOfTerminals += 1
                    } else {
                        accessRulesByTerminal.add(rhsAsInt[0], newEntry)
                    }
                }
                else -> {
                    throw Exception("Grammar: getGrammarDataStructuresForParsing -> Rule rhs is empty")
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

    fun getInsideWeights(
        accessRulesFromLhsNonLexical: MutableMap<String, MutableList<Pair<Rule, Double>>>,
        accessRulesFromLhsLexical: MutableMap<String, MutableList<Pair<Rule, Double>>>
    ): MutableMap<String, Double> {

        val insideValues = mutableMapOf<String, Double>()
        accessRulesFromLhsLexical.forEach { (lhs, rules) ->
            insideValues[lhs] = rules.maxOf { it.second }
        }
        var converged: Boolean
        do {
            converged = true
            accessRulesFromLhsNonLexical.forEach { (lhs, rules) ->
                insideValues.compute(lhs) { _, oldInValue ->
                    val whk = rules.maxOf { (rule, ruleProbability) ->
                        rule.rhs.fold(ruleProbability) { acc, s ->
                            acc * insideValues.getOrDefault(s, 0.0)
                        }
                    }
                    val newInValue = maxOf(oldInValue ?: 0.0, whk)
                    converged = newInValue == oldInValue && converged
                    newInValue
                }
            }
        } while (!converged)
        return insideValues
    }


    fun getOutsideWeights(
        insideValues: MutableMap<String, Double>,
        accessRulesFromRhs: MutableMap<String, MutableList<Tuple3<String, List<String>, Double>>>,
        nonTerminals: MutableSet<String>
    ): MutableMap<String, Double> {
        val outsideValues = mutableMapOf<String, Double>()
        outsideValues[initial] = 1.0
        val nonTerminalsWithoutInitial = nonTerminals.minus(initial)
        var converged: Boolean
        do {
            converged = true
            nonTerminalsWithoutInitial.forEach { nonTerminal ->
                outsideValues.compute(nonTerminal) { _, oldOutValue ->
                    val max = accessRulesFromRhs[nonTerminal]?.maxOf { (lhs, rhs, probability) ->
                        (outsideValues[lhs] ?: 0.0) * probability * (rhs.firstOrNull()
                            ?.let { insideValues.getOrDefault(it, 0.0) } ?: 1.0)
                    }
                    val newOutValue = maxOf(oldOutValue ?: 0.0, max ?: 0.0)
                    converged = newOutValue == oldOutValue && converged
                    newOutValue
                }
            }
        } while (!converged)
        return outsideValues
    }

    fun getViterbiOutsideScores(): MutableMap<String, Double> {
        val accessRulesFromLhsNonLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val accessRulesFromRhs = mutableMapOf<String, MutableList<Tuple3<String, List<String>, Double>>>()
        val accessRulesFromLhsLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val nonTerminals = mutableSetOf<String>()

        pRules.forEach { (rule, ruleProbability) ->
            if (rule.lexical) {
                accessRulesFromLhsLexical.add(rule.lhs, rule to ruleProbability)
            } else {
                accessRulesFromLhsNonLexical.add(rule.lhs, rule to ruleProbability)
                rule.rhs.forEach {
                    accessRulesFromRhs.add(it, Tuple3(rule.lhs, rule.rhs.minus(it), ruleProbability))
                    nonTerminals.add(it)
                }
            }
            nonTerminals.add(rule.lhs)
        }
        val insideValues = getInsideWeights(accessRulesFromLhsNonLexical, accessRulesFromLhsLexical)
        return getOutsideWeights(
            insideValues,
            accessRulesFromRhs,
            nonTerminals
        )
    }

    private fun <K, V> MutableMap<K, MutableList<V>>.add(key: K, item: V) {
        this.compute(key) { _, v ->
            if (v != null) {
                v.add(item)
                v
            } else {
                mutableListOf(item)
            }
        }
    }

    override fun toString(): String {
        return pRules.map { (rule, p) -> rule.toString() + " " + p.format(15) }.joinToString("\n")
    }
}