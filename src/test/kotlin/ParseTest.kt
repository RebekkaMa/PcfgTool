
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
        val data = "Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n\n"
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        val startTime = System.currentTimeMillis()
        Parse().parse(listOf("src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
        standardOut.println("Gesamtzeit: " + (System.currentTimeMillis()-startTime))

        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }

    @Test
    fun parseGoldShortWithA(){
        val sentencesFile = File("src/main/resources/small/sentences")
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        val data = "Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .\n\n"
        val sentences = sentencesFile.inputStream()
        System.setIn(sentences)

        val startTime = System.currentTimeMillis()
        Parse().parse(listOf("-a", "src/test/resources/outsideShort.txt", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
        standardOut.println("Gesamtzeit: " + (System.currentTimeMillis()-startTime))

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

        val startTime = System.currentTimeMillis()
        Parse().parse(listOf("-a" , "src/test/resources/outsideShort.txt","-r", "300", "src/main/resources/small/grammar.rules", "src/main/resources/small/grammar.lexicon"))
        standardOut.println("Gesamtzeit: " + (System.currentTimeMillis()-startTime))

        File("src/main/resources/small/gold_b.mrg").readText() shouldBe outputStreamCaptor.toString()
    }

}