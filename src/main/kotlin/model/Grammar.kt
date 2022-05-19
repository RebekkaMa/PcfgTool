package model

import com.github.h0tk3y.betterParse.utils.Tuple3
import com.github.h0tk3y.betterParse.utils.Tuple4
import com.github.h0tk3y.betterParse.utils.Tuple5
import com.github.h0tk3y.betterParse.utils.Tuple6
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.HashMap

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

    fun getGrammarDataStructuresForParsing(): Tuple6<MutableMap<Int, MutableList<Tuple3<Int, List<Int>, Double>>>, MutableMap<Int, MutableList<Tuple3<Int, List<Int>, Double>>>, MutableMap<Int, MutableList<Tuple3<Int, List<Int>, Double>>>, MutableMap<Int, MutableList<Tuple3<Int, List<Int>, Double>>>, Map<Int, String>, Map<String, Int>> {

        val lexiconByKey = kotlin.collections.HashMap<Int, String>()
        val lexiconByString = kotlin.collections.HashMap<String, Int>()

        lexiconByKey[0] = initial
        lexiconByString[initial] = 0

        this.pRules.keys.groupBy { it.lhs }.keys.forEachIndexed { index, s ->
            lexiconByString.putIfAbsent(s, index + 1) ?: lexiconByKey.putIfAbsent(index + 1, s)
        }
        val lexiconByKeySize = lexiconByKey.size
        val lexiconByStringSize = lexiconByString.size

        this.pRules.keys.filter { it.lexical }.groupBy { it.rhs.first() }.keys.forEachIndexed { index, s ->
            lexiconByString.putIfAbsent(s,index + lexiconByStringSize) ?: lexiconByKey.putIfAbsent(lexiconByKeySize + index, s)
        }

        val accessRulesBySecondNtOnRhs = mutableMapOf<Int, MutableList<Tuple3<Int, List<Int>, Double>>>()
        val accessRulesByFirstNtOnRhs = mutableMapOf<Int, MutableList<Tuple3<Int, List<Int>, Double>>>()
        val accessChainRulesByNtRhs = mutableMapOf<Int, MutableList<Tuple3<Int, List<Int>, Double>>>()
        val accessRulesByTerminal = mutableMapOf<Int, MutableList<Tuple3<Int, List<Int>, Double>>>()

        this.pRules.forEach { (rule, ruleProbability) ->
            val lhsHash : Int = lexiconByString[rule.lhs] ?: throw Exception("Internal Error")
            val rhsHashList: List<Int> = rule.rhs.map {
                lexiconByString[it] ?: throw Exception("Internal Error")
            }
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
                            Tuple3(lhsHash, rhsHashList, ruleProbability)
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
                    } else {
                        accessRulesByTerminal.compute(rhsHashList.first()) { _, v ->
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
        return Tuple6(
            accessRulesBySecondNtOnRhs,
            accessRulesByFirstNtOnRhs,
            accessChainRulesByNtRhs,
            accessRulesByTerminal,
            lexiconByKey,
            lexiconByString
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