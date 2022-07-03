package controller

import com.github.ajalt.clikt.core.CliktCommand

class PcfgTool : CliktCommand() {
    override val commandHelp = """
        Tools zum PCFG-basierten Parsing natürlichsprachiger Sätze
    """
    override fun run() = Unit
}