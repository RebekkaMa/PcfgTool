import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow

fun praktikumMain(args: Array<String>) {
    PcfgTool().subcommands(Induce(),Parse(), Binarise(), Debinarise(), unk(), Smooth(), Outside()).main(args)
}

class PcfgTool : CliktCommand() {
    override fun run() = Unit
}

class Induce : CliktCommand() {
    val grammar by argument().optional()
    val expression by option().prompt()
    override fun run() {
        if (grammar == null) echo(parseExpressionToPcfg(expression))
        else parseExpressionToPcfg(expression, grammar!!)

    }
}

class Parse : CliktCommand() {
    val rules by argument()
    val lexicon by argument()
    val paradigma by option("-p", "--paradigma").choice("cyk","deductive")
    val initialNonterminal by option("-i", "--initial-nonterminal").default("ROOT")
    val unking by option("-u", "--unking")
    val smoothing by option("-s", "--smoothing")
    val thresholdBeam by option("-t", "--threshold-beam")
    val rankBeam by option("-r", "--rank-beam")
    val kbest by option("-k", "--kbest")
    val astar by option("-a", "--astar")

    override fun run() {
        echo("Parse $paradigma")
    }
}

class Binarise : CliktCommand() {
    val horizontal by option("-h", "--horizontal").int().default(999)
    val vertical by option("-v", "--vertical").int().default(1)

    override fun run() {
        echo("Binarise")
    }
}

class Debinarise : CliktCommand() {
    override fun run() {
        echo("Debinarise")
    }
}

class unk : CliktCommand() {
    val threshold by option("-t", "--threshold")

    override fun run() {
        echo("unk")
    }
}

class Smooth : CliktCommand() {
    val threshold by option("-t", "--threshold")

    override fun run() {
        echo("smooth")
    }
}

class Outside : CliktCommand() {
    val initial by option("-i", "--initial-nonterminal").default("ROOT")

    override fun run() {
        echo("outside")
    }
}

fun testDummy(dummy : Boolean): Boolean {
    return !dummy
}
