import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class Tree(val atom: String, val children: ArrayList<Tree> = ArrayList()) {
    fun addExpressionToList(expression: Tree) {
        children.add(expression)
    }

    fun printExpressionTree(): String {
        var treeString = ""
        fun printTree(tree: Tree){
            treeString += tree.atom
            if (tree.children.isEmpty()) {
                return
            }
            treeString = "$treeString("
            tree.children.map { printTree(it) }
            treeString = "$treeString)"
        }
        printTree(this)
        return treeString
    }

    fun parseToRule(expression: Tree = this): Rule{
        val childrenString = expression.children.joinToString(separator = " ") { it.atom }
        return Rule(expression.children.first().children.isEmpty() , expression.atom, childrenString.split(" "))
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun parseToRules(): ArrayList<Rule> {
        val rules: ArrayList<Rule> = ArrayList()

        val mutualRecursion = object {
            val even: DeepRecursiveFunction<Tree, Unit> = DeepRecursiveFunction {
                if (it.children.isEmpty()) return@DeepRecursiveFunction
                rules.add(parseToRule(it))
                it.children.map { child -> odd.callRecursive(child) }
            }
            val odd: DeepRecursiveFunction<Tree, Unit> = DeepRecursiveFunction {
                if (it.children.isEmpty()) return@DeepRecursiveFunction
                rules.add(parseToRule(it))
                it.children.map { child -> even.callRecursive(child) }
            }
        }
        mutualRecursion.even.invoke(this)

        return rules

    }

//    @OptIn(ExperimentalStdlibApi::class)
//    fun parseToRules(): ArrayList<Rule> {
//            val rules: ArrayList<Rule> = ArrayList()
//
//            val mutualRecursion = object {
//                val even: DeepRecursiveFunction<Tree, Unit> = DeepRecursiveFunction {
//                    if (it.children.isEmpty()) return@DeepRecursiveFunction
//                    rules.add(parseToRule(it))
//                    it.children.map { child -> odd.callRecursive(child) }
//                }
//                val odd: DeepRecursiveFunction<Tree, Unit> = DeepRecursiveFunction {
//                    if (it.children.isEmpty()) return@DeepRecursiveFunction
//                    rules.add(parseToRule(it))
//                    it.children.map { child -> even.callRecursive(child) }
//                }
//            }
//
//            mutualRecursion.even.invoke(this)
//
//        return rules
//
//    }
}