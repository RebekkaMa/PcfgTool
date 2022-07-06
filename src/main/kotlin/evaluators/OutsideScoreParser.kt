package evaluators

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

class OutsideScoreParser : Grammar<Pair<String,Double>>() {

    val number by regexToken("[0-9]([\\.\\,]\\d*(E\\-\\d*)?)?(\\s)*$")
    val text by regexToken("[\\w\\p{Punct}ε&&[^\\s\\(\\)]]+")
    val space by literalToken(" ", ignore = true)

    val probability by number use {this.text.dropLastWhile{ it.isWhitespace() }.replace(",",".").toDouble()}
    val txt by text use { this.text }


    val viterbiOutsindeWeight: Parser<Pair<String, Double>> by
    (txt and probability).map { (nt, prob) -> Pair( nt, prob) }

    override val rootParser: Parser<Pair<String, Double>> by viterbiOutsindeWeight
}