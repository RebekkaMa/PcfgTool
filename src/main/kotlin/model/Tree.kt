package model

class Tree(val atom: String, val children: ArrayList<Tree> = ArrayList()) {
    fun addExpressionToList(expression: Tree) {
        children.add(expression)
    }

    fun printExpressionTree(): String {
        var treeString = ""
        fun printTree(tree: Tree) {
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

    fun parseToRule(expression: Tree = this): Rule {
        val childrenString = expression.children.joinToString(separator = " ") { it.atom }
        return Rule(expression.children.first().children.isEmpty(), expression.atom, childrenString.split(" "))
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun parseToRules(): ArrayList<Rule> {
        val rules: ArrayList<Rule> = ArrayList()
        val depth = DeepRecursiveFunction<Tree, Unit> {
            if (it.children.isEmpty()) return@DeepRecursiveFunction
            rules.add(parseToRule(it))
            it.children.map { child -> callRecursive(child) }
        }
        depth(this)
        return rules
    }
}