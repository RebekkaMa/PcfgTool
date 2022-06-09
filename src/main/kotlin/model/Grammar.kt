package model

import com.github.h0tk3y.betterParse.utils.Tuple3
import com.github.h0tk3y.betterParse.utils.Tuple7
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

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

    fun getTerminals(): List<String> {
        return pRules.keys.filter { it.lexical }.map { it.rhs.first() }.distinct()
    }

    fun getLexicon(): List<String> {
        return pRules.filterKeys { it.lexical }
            .map { (rule, p) -> rule.lhs + " " + rule.rhs.joinToString(" ") + " " + p.format(15) }
    }

    fun getRules(): List<String> {
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

    fun <K, V> MutableMap<K, MutableList<V>>.addTuple(key: K, tuple: V) {
        this.compute(key) { _, v ->
            if (v != null) {
                v.add(
                    tuple
                )
                v
            } else {
                mutableListOf(tuple)
            }
        }
    }

    fun inside(
        accessRulesFromLhsNonLexical: MutableMap<String, MutableList<Pair<Rule, Double>>>,
        accessRulesFromLhsLexical: MutableMap<String, MutableList<Pair<Rule, Double>>>
    ): MutableMap<String, Double> {

        val ins = mutableMapOf<String, Double>()
        accessRulesFromLhsLexical.forEach { (label, list) ->
            ins[label] = list.maxOf { it.second }
        }
        //Verwendet man pro Iteration die alten Werte, oder schon die neuen?
        for (i in 0..20){
            accessRulesFromLhsNonLexical.forEach { (label, list) ->
                ins.compute(label) { _, oldIns ->
                    val whk = list.maxOf { (rule, probability) ->
                        rule.rhs.fold(probability) { acc, s ->
                            acc * ins.getOrDefault(s, 0.0)
                        }
                    }
                    maxOf(oldIns ?: 0.0, whk)
                }
            }
        }
        return ins
    }

    fun outside(
        insideValues: MutableMap<String, Double>,
        accessRulesFromRhs: MutableMap<String, MutableList<Pair<Rule, Double>>>,
        accessRulesFromLhsLexical: MutableMap<String, MutableList<Pair<Rule, Double>>>,
        accessRulesFromLhsNonLexical: MutableMap<String, MutableList<Pair<Rule, Double>>>,
        nonTerminals: MutableSet<String>
    ): MutableMap<String, Double> {
        val out = mutableMapOf<String, Double>()
        nonTerminals.forEach { out[it] = if (it == initial) 1.0 else 0.0 }
        //TODO Statt nonTerminals accesRulesFromLhs verwenden

        //Verwendet man pro Iteration die alten Werte, oder schon die neuen?
        for (i in 0..20){
            nonTerminals.forEach { nonTerminal ->
                out.compute(nonTerminal) { _, oldOut ->
                    val max = accessRulesFromRhs[nonTerminal]?.maxOf { (rule, probability) ->
                        val wht = rule.rhs.fold(1.0) { acc, label ->
                            if (label == nonTerminal) {
                                acc
                            } else {
                                acc * insideValues.getOrDefault(label, 0.0)
                            }
                        }
                        (out[rule.lhs] ?: 0.0) * probability * wht
                    }
                   maxOf(oldOut ?: 0.0, max ?: 0.0)
                }
            }
        }
        return out
    }

    fun viterbiOutsideScore(): MutableMap<String, Double> {
        val accessRulesFromLhsNonLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val accessRulesFromRhs = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val accessRulesFromLhsLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val nonTerminals = mutableSetOf<String>()

        pRules.forEach { (rule, ruleProbability) ->
            if (rule.lexical) {
                accessRulesFromLhsLexical.addTuple(rule.lhs, rule to ruleProbability)
            } else {
                accessRulesFromLhsNonLexical.addTuple(rule.lhs, rule to ruleProbability)
                rule.rhs.forEach {
                    accessRulesFromRhs.addTuple(it, rule to ruleProbability)
                    nonTerminals.add(it)
                }
            }
            nonTerminals.add(rule.lhs)
        }
        val insideValues = inside(accessRulesFromLhsNonLexical, accessRulesFromLhsLexical)
        return outside(
            insideValues,
            accessRulesFromRhs,
            accessRulesFromLhsLexical,
            accessRulesFromLhsNonLexical,
            nonTerminals
        )
    }


    override fun toString(): String {
        return pRules.map { (rule, p) -> rule.toString() + " " + p.format(15) }.joinToString("\n")
    }
}

fun Double.format(fracDigits: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale.UK)
    val df = nf as DecimalFormat
    df.maximumFractionDigits = fracDigits
    return df.format(this)
}