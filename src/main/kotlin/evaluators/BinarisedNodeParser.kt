package evaluators

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple3

class BinarisedNodeParser : Grammar<Tuple3<String, List<String>, List<String>>>() {

    val lab by regexToken("[\\w\\p{Punct}ε&&[^\\s\\(\\)\\<\\>\\^\\|]]+")
    val label by lab use { this.text}
    val labelWithOutComma by regexToken("[\\w\\p{Punct}ε&&[^\\s\\(\\)\\<\\>\\^\\|,]]+") use {this.text}
    val lessThan by literalToken("<")
    val greaterThan by literalToken(">")
    val verticalBar by literalToken("|")
    val circ by literalToken("^")
    val comma by literalToken(",")


    val listOfLabels by
    (skip(lessThan) and label and skip(greaterThan)) map {
        it.split(",")
    }

    val parents by
        skip(circ) and listOfLabels
    val children by
        skip(verticalBar) and listOfLabels

    val tuple: Parser<Tuple3<String, List<String>, List<String>>> by
    (label and (0..1 times (children)) and  (0..1 times (parents))).map { (a, b, c) ->
        Tuple3(a, b.firstOrNull() ?: listOf<String>(), c.firstOrNull() ?: listOf<String>())
    }

    override val rootParser: Parser<Tuple3<String, List<String>, List<String>>> by tuple
}