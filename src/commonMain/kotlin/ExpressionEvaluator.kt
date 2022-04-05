import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

class ExpressionEvaluator : Grammar<Tree>() {
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val lab by regexToken("[\\w\\p{Punct}&&[^\\s\\(\\)]]+")
    val space by literalToken(" ", ignore = true)

    val label by lab use { Tree(atom = text) }

    val tree: Parser<Tree> by
    ((skip(lpar) and label and oneOrMore(parser(::tree)) and skip(rpar))).map { (t1, t2) ->
        t2.map { t1.addExpressionToList(it) }
        t1
    } or
            label

    override val rootParser: Parser<Tree> by tree
}