package evaluators

import Rule
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar

import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser


class LexiconExpressionEvaluator : Grammar<Pair<Rule, Double>>() {

    val number by regexToken("[01]([\\.\\,]\\d*)?(\\s)*$")
    val text by regexToken("[\\w\\p{Punct}&&[^\\s\\(\\)]]+")
    val space by literalToken(" ", ignore = true)

    val property by number use {this.text.dropLastWhile{ it.isWhitespace() }.replace(",",".").toDouble()}
    val txt by text use { this.text }


    val rule: Parser<Pair<Rule,Double>> by
    (txt and txt and property).map { (lhs, rhs, property)-> Pair(Rule(lexical = true, lhs = lhs, rhs = listOf(rhs)), property) }

    override val rootParser: Parser<Pair<Rule, Double>> by rule
}