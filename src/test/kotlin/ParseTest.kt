
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.Test

class ParseTest {

    @Test
    fun parseGoldShort(){
        val sentencesFile = File("src/main/resources/small/sentences")
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        Parse().parse(listOf("src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))

        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }

    @Test
    fun parseGoldShortWithAFormat(){
        val sentencesFile = File("src/main/resources/small/sentences")
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        Parse().parse(listOf("-a", "src/test/resources/outsideShort.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
        Parse().parse(listOf("-a", "src/test/resources/outsideShort.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))


        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }

    @Test
    fun parseGoldShortWithANotFormat(){
        val sentencesFile = File("src/main/resources/small/sentences")
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        Parse().parse(listOf("-a", "src/test/resources/outsideShort.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
        Parse().parse(listOf("-a", "src/test/resources/outsideShortNotFormat.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))


        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }


    @Test
    fun parseGoldShortWithThreshold(){
        val sentencesFile = File("src/main/resources/small/sentences")
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val data = "Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n\n"
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        Parse().parse(listOf("-a" , "src/test/resources/outsideShort.txt","-r", "300", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))

        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }
}