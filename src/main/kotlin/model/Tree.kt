package model

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import evaluators.MarkovizationNodeEvaluator

class Tree(var atom: String, val children: ArrayList<Tree> = ArrayList()) {
    fun addExpressionToList(expression: Tree) {
        children.add(expression)
    }

    fun printExpressionTree(tree: Tree = this): String {
        return when (tree.children.size){
            0 -> atom
            1 ->  "(" + tree.atom + " " + printExpressionTree(tree.children.first()) + ")"
            else -> {
                val partOfTreeAsString = tree.children.fold("(" + tree.atom) { acc, child -> acc + " " + printExpressionTree(child) }
                "$partOfTreeAsString)"
            }
        }
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

    fun debinarize(tree: Tree = this): Tree {
        val markovizationNodeEvaluator = MarkovizationNodeEvaluator()
        tree.atom = MarkovizationNodeEvaluator().parseToEnd(tree.atom).t1

        if (tree.children.isEmpty()) return tree
        val lastChild = tree.children.last()
        val (_, children, _) = markovizationNodeEvaluator.parseToEnd(lastChild.atom)

        if (children.isNotEmpty()) {
            tree.children.removeLast()
            lastChild.children.forEach {
                tree.children.add(Tree(it.atom, it.children))
            }
            return debinarize(tree)
        }
        tree.children.onEach { debinarize(it) }
        return tree
    }
}