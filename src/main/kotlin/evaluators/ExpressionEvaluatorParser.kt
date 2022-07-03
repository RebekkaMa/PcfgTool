package evaluators

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import model.Tree

class ExpressionEvaluatorParser : Grammar<Tree>() {
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val lab by regexToken("[\\w\\p{Punct}ε&&[^\\s\\(\\)]]+")
    val space by literalToken(" ", ignore = true)

    val label by lab use { Tree(atom = text) }

    val tree: Parser<Tree> by
    ((skip(lpar) and label and (label or (oneOrMore(parser(::tree)))) and skip(rpar))).map { (t1, t2) ->
        if (t2 is List<*>) t2.map { t1.addChild(it as Tree) }
        else t1.addChild(t2 as Tree)
        t1
    }

    override val rootParser: Parser<Tree> by tree
}