
import com.github.ajalt.clikt.core.subcommands
import controller.*

fun main(args: Array<String>) {
    PcfgTool().subcommands(
        Induce(), Parse(), Binarise(), Debinarise(), Unk(), Smooth(),
        Outside()
    ).main(args)
}