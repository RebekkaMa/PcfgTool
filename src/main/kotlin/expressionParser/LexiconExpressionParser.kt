package expressionParser

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import model.Rule


class LexiconExpressionParser : Grammar<Pair<Rule, Double>>() {

    val number by regexToken("[0-9]([\\.\\,]\\d*(E\\-\\d*)?)?(\\s)*$")
    val text by regexToken("[\\w\\p{Punct}ε&&[^\\s\\(\\)]]+")
    val space by literalToken(" ", ignore = true)

    val probability by number use {this.text.dropLastWhile{ it.isWhitespace() }.replace(",",".").toDouble()}
    val txt by text use { this.text }


    val rule: Parser<Pair<Rule,Double>> by
    (txt and txt and probability).map { (lhs, rhs, prob)-> Pair(Rule(lexical = true, lhs = lhs, rhs = listOf(rhs)), prob) }

    override val rootParser: Parser<Pair<Rule, Double>> by rule
}