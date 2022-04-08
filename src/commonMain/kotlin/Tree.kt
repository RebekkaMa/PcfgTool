class Tree(val atom: String, val children: ArrayList<Tree> = ArrayList()) {
    fun addExpressionToList(expression: Tree) {
        children.add(expression)
    }

    //Tiefensuche
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
        return Rule(expression.children.first().children.isEmpty() , expression.atom, childrenString)
    }

    fun parseToRules(): ArrayList<Rule> {
        val rules: ArrayList<Rule> = ArrayList()

        fun parse(expression: Tree) {
            if (expression.children.isEmpty()) return
            rules.add(parseToRule(expression))
            expression.children.map { parse(it) }
            return
        }
        parse(this)
        return rules
    }
}