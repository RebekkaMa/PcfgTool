package model

import com.github.h0tk3y.betterParse.utils.Tuple4
import com.github.h0tk3y.betterParse.utils.Tuple5
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

    fun getGrammarDataStructuresForParsing(): Tuple5<MutableMap<Int, MutableList<Pair<Rule, Double>>>, MutableMap<Int, MutableList<Pair<Rule, Double>>>, MutableMap<Int, MutableList<Pair<Rule, Double>>>, MutableMap<Int, MutableList<Pair<Rule, Double>>>, Map<Int, String>> {

        val lexicon = this.pRules.keys.groupBy { it.lhs }.keys.associateBy { it.hashCode() }

        val accessRulesBySecondNtOnRhs = mutableMapOf<Int, MutableList<Pair<Rule, Double>>>()
        val accessRulesByFirstNtOnRhs = mutableMapOf<Int, MutableList<Pair<Rule, Double>>>()
        val accessChainRulesByNtRhs = mutableMapOf<Int, MutableList<Pair<Rule, Double>>>()
        val accessRulesByTerminal = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()

        this.pRules.forEach { (rule, ruleProbability) ->
            when (rule.rhs.size) {
                2 -> {
                    accessRulesByFirstNtOnRhs.compute(rule.rhs.first().hashCode()) { _, v ->
                        if (v != null) {
                            v.add(rule to ruleProbability)
                            v
                        } else {
                            mutableListOf(rule to ruleProbability)
                        }
                    }
                    accessRulesBySecondNtOnRhs.compute(rule.rhs.component2().hashCode()) { _, v ->
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
                        accessChainRulesByNtRhs.compute(rule.rhs.first().hashCode()) { _, v ->
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
        return Tuple5(
            accessRulesBySecondNtOnRhs,
            accessRulesByFirstNtOnRhs,
            accessChainRulesByNtRhs,
            accessRulesByTerminal,
            lexicon
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