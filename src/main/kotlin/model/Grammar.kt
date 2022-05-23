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
        suspend fun createFromRules(rules: List<Rule>): Grammar = coroutineScope {
            val absoluteRules = async { rules.groupingBy { it }.eachCount() }
            val lhsCount = async { rules.groupingBy { it.lhs }.eachCount() }
            val pcfgRules = absoluteRules.await()
                .mapValues { (rule, count) -> (count.toDouble() / (lhsCount.await().getOrElse(rule.lhs) { 1 })) }
            Grammar(pRules = pcfgRules)
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
                    accessRulesByFirstNtOnRhs.compute(rhsHashList[0]) { _, v ->
                        if (v != null) {
                            v.add(
                                newTuple
                            )
                            v
                        } else {
                            mutableListOf(newTuple)
                        }
                    }
                    accessRulesBySecondNtOnRhs.compute(rhsHashList[1]) { _, v ->
                        if (v != null) {
                            v.add(Tuple3(lhsHash, rhsHashList, ruleProbability))
                            v
                        } else {
                            mutableListOf(newTuple)
                        }
                    }
                }
                1 -> {
                    if (!rule.lexical) {
                        accessChainRulesByNtRhs.compute(rhsHashList[0]) { _, v ->
                            if (v != null) {
                                v.add(newTuple)
                                v
                            } else {
                                mutableListOf(newTuple)
                            }
                        }
                        numberOfTerminals += 1
                    } else {
                        accessRulesByTerminal.compute(rhsHashList[0]) { _, v ->
                            if (v != null) {
                                v.add(newTuple)
                                v
                            } else {
                                mutableListOf(newTuple)
                            }
                        }
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