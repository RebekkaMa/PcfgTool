//import com.github.h0tk3y.betterParse.combinators.*
//import com.github.h0tk3y.betterParse.grammar.Grammar
//import com.github.h0tk3y.betterParse.grammar.parseToEnd
//import com.github.h0tk3y.betterParse.grammar.parser
//import com.github.h0tk3y.betterParse.lexer.literalToken
//import com.github.h0tk3y.betterParse.lexer.regexToken
//import com.github.h0tk3y.betterParse.parser.Parser
//import java.util.*
//import java.util.function.Consumer
//import kotlin.collections.ArrayList
//
//
//class ExpressionEvaluator : Grammar<Tree>() {
//    val lpar by literalToken("(")
//    val rpar by literalToken(")")
//    val lab by regexToken("\\w+")
//    val space by literalToken(" ", ignore = true)
//
//    val label by lab use { Tree(atom = text) }
//
//    val tree: Parser<Tree> by
//    ((skip(lpar) and label and oneOrMore(parser(::tree)) and skip(rpar))).map { (t1, t2) ->
//        t2.map { t1.addExpressionToList(it) }
//        t1
//    } or
//            label
//
//    override val rootParser: Parser<Tree> by tree
//}
//
//
//class Tree(val atom: String, val children: ArrayList<Tree> = ArrayList()) {
//    fun addExpressionToList(expression: Tree) {
//        children.add(expression)
//    }
//
//    //Tiefensuche
//    fun printExpressionTree(expression: Tree = this) {
//        print(expression.atom)
//        if (expression.children.isEmpty()) {
//            return
//        }
//        print('(')
//        expression.children.map { printExpressionTree(it) }
//        print(')')
//
//    }
//
//    fun parseToRule(expression: Tree = this): Rule {
//        val childrenString = expression.children.joinToString(separator = " ") { it.atom }
//        return Rule(expression.atom, childrenString)
//    }
//
//    fun parseToRules(): ArrayList<Rule> {
//        val rules: ArrayList<Rule> = ArrayList()
//
//        fun parse(expression: Tree) {
//            if (expression.children.isEmpty()) return
//            rules.add(parseToRule(expression))
//            if (expression.children.isNotEmpty()) {
//                expression.children.map { parse(it) }
//            }
//            return
//        }
//        parse(this)
//        return rules
//    }
//}
//
//
//enum class RuleType {
//    LEXICAL,
//    NONLEXICAL
//}
//
//class Rule(val lhs: String, val rhs: String) {
//    override fun toString(): String {
//        return "$lhs --> $rhs"
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (other is Rule) {
//            return lhs == other.lhs && rhs == other.rhs
//        }
//        return false
//    }
//
//    override fun hashCode(): Int {
//        return Objects.hash(lhs, rhs)
//    }
//}
//
//fun <T> countByForEachLoopWithGetOrDefault(inputList: List<T>): Map<T, Long>? {
//    val resultMap: MutableMap<T, Long> = HashMap()
//    inputList.forEach(Consumer { e: T -> resultMap[e] = resultMap.getOrDefault(e, 0L) + 1L })
//    return resultMap
//}
//
//fun createGrammar(rules : ArrayList<Rule>): MyGrammar {
//
//    val absoluteRules = rules.groupingBy { it }.eachCount()
//    val lhsCount = rules.groupingBy { it.lhs }.eachCount()
//    //val relativeRules : HashMap<Rule, Float> = HashMap<Rule,Float>()
//    //absoluteRules.forEach { (rule, count) -> println(count/(lhsCount.getOrDefault(rule.lhs, 0))) ;relativeRules[rule] = (count.toFloat() / (1+lhsCount.getOrDefault(rule.lhs,0))) }
//    val pcfgRules = absoluteRules.mapValues { (rule, count) -> (count.toFloat()/(lhsCount.getOrDefault(rule.lhs, 0)) )}
//
//
//    return MyGrammar(rules[0].lhs, pcfgRules)
//}
//
//class MyGrammar(val initial: String, val pRules: Map<Rule, Float>){
//    override fun toString(): String {
//        return "Initial: " + initial + "\n" + "Rules: \n" + pRules.map { (rule, p) -> "$rule    $p \n" }.toString()
//    }
//}
//
//fun main(args: Array<String>) {
//    //val expr = "(S (N John)(VP (V hit) (NP (D the) (N John) (N ball))))"
//    //val expr = "(S John)"
//    val expr = "(S (NP John)(VP (V hit) (NP (DET the) (N (ball (NP (DET the) (N ground)))))))"
//    val result = ExpressionEvaluator().parseToEnd(expr)
//
//   // println( createGrammar(result.parseToRules()).toString())
//
//}
//
//
//
//
//

