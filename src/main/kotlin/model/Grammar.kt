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

    fun getGrammarDataStructuresForParsing(): Tuple4<Map<String, MutableList<Pair<Rule, Double>>>, Map<String, MutableList<Pair<Rule, Double>>>, Map<String, MutableList<Pair<Rule, Double>>>, Map<String, MutableList<Pair<Rule, Double>>>>{

        val grammarRhs = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val grammarLhs = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val grammarChain = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()
        val grammarLexical = mutableMapOf<String, MutableList<Pair<Rule, Double>>>()

        this.pRules.forEach {
            when (it.key.rhs.size) {
                2 -> {
                    grammarLhs.compute(it.key.rhs.first()) { _, v ->
                        if (v != null) {
                            v.add(it.toPair())
                            v
                        } else {
                            mutableListOf(it.toPair())
                        }
                    }
                    grammarRhs.compute(it.key.rhs.component2()) { _, v ->
                        if (v != null) {
                            v.add(it.toPair())
                            v
                        } else {
                            mutableListOf(it.toPair())
                        }
                    }
                }
                1 -> {
                    if (!it.key.lexical) {
                        grammarChain.compute(it.key.rhs.first()) { _, v ->
                            if (v != null) {
                                v.add(it.toPair())
                                v
                            } else {
                                mutableListOf(it.toPair())
                            }
                        }
                    } else {
                        grammarLexical.compute(it.key.rhs.first()) { _, v ->
                            if (v != null) {
                                v.add(it.toPair())
                                v
                            } else {
                                mutableListOf(it.toPair())
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }
        return Tuple4(grammarRhs, grammarLhs, grammarChain, grammarLexical)
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