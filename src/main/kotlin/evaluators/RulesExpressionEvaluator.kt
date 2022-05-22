package evaluators

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar

import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import model.Rule


class RulesExpressionEvaluator : Grammar<Pair<Rule, Double>>() {

    val arrow by literalToken("->")
    val number by regexToken("[01]([\\.\\,]\\d*)?(\\s)*$")
    val nonlexical by regexToken("[\\w\\p{Punct}&&[^\\s\\(\\)]]+")
    val space by literalToken(" ", ignore = true)

    val probability by number use { this.text.dropLastWhile { it.isWhitespace() }.replace(",", ".").toDouble() }
    val nl by nonlexical use { this.text }
    val arr by arrow use { this.text }

    val rule: Parser<Pair<Rule, Double>> by
    ((nl or arr) and skip(arr) and (1..2 times (nl or arr)) and probability).map { (lhs, rhs, probability) ->
        Pair(
            Rule(
                lexical = false,
                lhs = lhs,
                rhs = rhs
            ), probability
        )
    }

    override val rootParser: Parser<Pair<Rule, Double>> by rule
}