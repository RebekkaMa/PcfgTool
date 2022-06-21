import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class kbestTest {

    @Test
    fun basicTest(){
        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
        System.setIn("Fruit flies like bananas".byteInputStream())
        Parse().parse(listOf("-k","3","-i", "S", "src/test/resources/grammar2.rules" ,"src/test/resources/grammar2.lexicon"))

        outputStreamCaptor.toString() shouldBeEqualComparingTo "(S (NP (NN Fruit)) (VP (VBZ flies) (PP (IN like) (NP (NNS bananas)))))\n(S (NP (NN Fruit) (NNS flies)) (VP (VBP like) (NP (NNS bananas))))\n(NOPARSE Fruit flies like bananas)\n"
    }

}