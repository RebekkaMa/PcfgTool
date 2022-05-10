package model

import com.github.h0tk3y.betterParse.utils.Tuple4
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

    fun getGrammarDataStructuresForParsing(): Tuple4<Map<String, MutableList<Pair<Rule, Double>>>, Map<String, MutableList<Pair<Rule, Double>>>, Map<String, MutableList<Pair<Rule, Double>>>, Map<String, MutableList<Pair<Rule, Double>>>> {

        val accessRulesBySecondNtOnRhs = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val accessRulesByFirstNtOnRhs = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val accessChainRulesByNtRhs = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val accessRulesByTerminal = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()

        this.pRules.forEach { (rule, ruleProbability) ->
            when (rule.rhs.size) {
                2 -> {
                    accessRulesByFirstNtOnRhs.compute(rule.rhs.first()) { _, v ->
                        if (v != null) {
                            v.add(rule to ruleProbability)
                            v
                        } else {
                            mutableListOf(rule to ruleProbability)
                        }
                    }
                    accessRulesBySecondNtOnRhs.compute(rule.rhs.component2()) { _, v ->
                        if (v != null) {
                            v.add(rule to ruleProbability)
                            v
                        } else {
                            mutableListOf(rule to ruleProbability)
                        }
                    }
                }
                1 -> {
                    if (!rule.lexical) {
                        accessChainRulesByNtRhs.compute(rule.rhs.first()) { _, v ->
                            if (v != null) {
                                v.add(rule to ruleProbability)
                                v
                            } else {
                                mutableListOf(rule to ruleProbability)
                            }
                        }
                    } else {
                        accessRulesByTerminal.compute(rule.rhs.first()) { _, v ->
                            if (v != null) {
                                v.add(rule to ruleProbability)
                                v
                            } else {
                                mutableListOf(rule to ruleProbability)
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }
        return Tuple4(
            accessRulesBySecondNtOnRhs,
            accessRulesByFirstNtOnRhs,
            accessChainRulesByNtRhs,
            accessRulesByTerminal
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