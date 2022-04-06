import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.parseToEnd

fun praktikumMain(args: Array<String>) {
    PcfgTool().subcommands(Induce(), Parse(), Binarise(), Debinarise(), unk(), Smooth(), Outside()).main(args)
}

class PcfgTool : CliktCommand() {
    override fun run() = Unit
}

class Induce : CliktCommand() {
    val grammar by argument().optional()

    val readNotEmptyLnOrNull = { val line = readlnOrNull(); if (line.isNullOrEmpty()) null else line }

    override fun run() {
        val rules = generateSequence(readNotEmptyLnOrNull).map{ expressionEvaluator.parseToEnd(it) }.flatMap { it.parseToRules() }.toList()
        if (grammar == null) {
            echo(createGrammar(ArrayList(rules)).toString())
        } else {
            writeToFiles(createGrammar(ArrayList(rules)), grammar.toString())
            echo("Done")
        }
    }
}

class Parse : CliktCommand() {
    val rules by argument()
    val lexicon by argument()

    val paradigma by option("-p", "--paradigma").choice("cyk", "deductive")
    val initialNonterminal by option("-i", "--initial-nonterminal").default("ROOT")
    val unking by option("-u", "--unking")
    val smoothing by option("-s", "--smoothing")
    val thresholdBeam by option("-t", "--threshold-beam")
    val rankBeam by option("-r", "--rank-beam")
    val kbest by option("-k", "--kbest")
    val astar by option("-a", "--astar")

    override fun run() {
        echo(22)
    }
}

class Binarise : CliktCommand() {
    val horizontal by option("-h", "--horizontal").int().default(999)
    val vertical by option("-v", "--vertical").int().default(1)

    override fun run() {
        echo(22)
    }
}

class Debinarise : CliktCommand() {
    override fun run() {
        echo(22)
    }
}

class unk : CliktCommand() {
    val threshold by option("-t", "--threshold")

    override fun run() {
        echo(22)
    }
}

class Smooth : CliktCommand() {
    val threshold by option("-t", "--threshold")

    override fun run() {
        echo(22)
    }
}

class Outside : CliktCommand() {
    val initial by option("-i", "--initial-nonterminal").default("ROOT")

    override fun run() {
        echo(22)
    }
}

fun testDummy(dummy: Boolean): Boolean {
    return !dummy
}
