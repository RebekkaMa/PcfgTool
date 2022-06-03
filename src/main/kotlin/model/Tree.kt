package model

import Util.getWindow
import Util.joinToStringWithStartAndEnd
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import evaluators.MarkovizationNodeEvaluator

class Tree(var atom: String, val children: MutableList<Tree> = mutableListOf()) {
    fun addExpressionToList(expression: Tree) {
        children.add(expression)
    }

    fun parseToRule(tree: Tree = this): Rule {
        val childrenString = tree.children.joinToString(separator = " ") { it.atom }
        return Rule(tree.isPreterminal(), tree.atom, childrenString.split(" "))
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

    fun getLeaves(): List<String> {
        val leaves = mutableListOf<String>()
        when {
            children.isEmpty() -> leaves.add(atom)
            else -> {
                children.forEach { leaves.addAll(it.getLeaves()) }
            }
        }
        return leaves
    }


    fun setLeaves(leaves: List<String>) {
        var i = 0
        fun setLeavesPartOfTree(tree: Tree = this) {
            when {
                tree.children.isEmpty() -> {
                    tree.atom = leaves.getOrElse(i) { tree.atom }
                    i++
                }
                else -> {
                    tree.children.forEach {
                        setLeavesPartOfTree(it)
                    }
                }
            }
        }
        setLeavesPartOfTree()
    }

    fun binarise(vertical: Int, horizontal: Int): Tree {
        val markovizationNodeEvaluator = MarkovizationNodeEvaluator()
        val parents = ArrayDeque<String>()

        fun addParents(label: String, children: List<String>, isAddedNode: Boolean): String {
            val newParents = if (isAddedNode) parents.drop(1) else parents
            val fullLabel =
                if (isAddedNode) children.joinToString(prefix = "$label|<", separator = ",", postfix = ">") else label
            return if (vertical == 1 || newParents.isEmpty()) fullLabel else {
                newParents.joinToStringWithStartAndEnd(
                    prefix = "$fullLabel^<", separator = ",", postfix = ">", limit = vertical - 1
                )
            }
        }

        fun addLableToParents(label: String, isAddedNode: Boolean) {
            if (!isAddedNode) parents.addFirst(label)
        }

        fun removeLableToParents(isAddedNode: Boolean) {
            if (!isAddedNode) parents.removeFirst()
        }

        fun binarise(tree: Tree = this): Tree {
            val numberOfChildren = tree.children.size
            val (label, childrenAsStrings, _) = markovizationNodeEvaluator.parseToEnd(tree.atom)
            val isAddedNode = childrenAsStrings.isNotEmpty()
            when {
                tree.isPreterminal() -> return tree

                numberOfChildren <= 2 -> {
                    addLableToParents(label, isAddedNode)
                    val markovizedChildren = tree.children.map { binarise(it) }
                    removeLableToParents(isAddedNode)
                    return Tree(
                        atom = addParents(label, childrenAsStrings, isAddedNode),
                        children = markovizedChildren as MutableList<Tree>
                    )
                }
                else -> {
                    val binarisedSecondChildChildren = tree.children.getWindow(mutableListOf(), 1, horizontal)
                    val newNodeAtom = binarisedSecondChildChildren.joinToString(
                        prefix = "$label|<",
                        separator = ",",
                        postfix = ">"
                    ) { child -> child.atom }
                    val newAtomOfCurrentTree = addParents(label, childrenAsStrings, isAddedNode)
                    addLableToParents(label, isAddedNode)
                    val binarisedFirstChild = binarise(tree.children.first())
                    val binarisedSecondChild = binarise(
                        Tree(
                            atom = newNodeAtom,
                            children = binarisedSecondChildChildren
                        )
                    )
                    removeLableToParents(isAddedNode)
                    return Tree(
                        atom = newAtomOfCurrentTree,
                        children = mutableListOf(binarisedFirstChild, binarisedSecondChild)
                    )
                }
            }
        }
        return binarise()
    }

    fun debinarise(tree: Tree = this): Tree {
        val markovizationNodeEvaluator = MarkovizationNodeEvaluator()
        tree.atom = MarkovizationNodeEvaluator().parseToEnd(tree.atom).t1

        if (tree.isPreterminal()) return tree
        val lastChild = tree.children.last()
        val (_, childrenOfCurrentTree, _) = markovizationNodeEvaluator.parseToEnd(lastChild.atom)

        if (childrenOfCurrentTree.isNotEmpty()) {
            tree.children.removeLast()
            lastChild.children.forEach {
                tree.children.add(Tree(it.atom, it.children))
            }
            return debinarise(tree)
        }
        tree.children.onEach { debinarise(it) }
        return tree
    }

    private fun isPreterminal(): Boolean {
        return this.children.firstOrNull()?.children?.isEmpty() ?: false
    }

    override fun toString(): String {
        fun getPartOfTreeAsString(tree: Tree = this): String {
            return when (tree.children.size) {
                0 -> tree.atom
                1 -> "(" + tree.atom + " " + getPartOfTreeAsString(tree.children.first()) + ")"
                else -> {
                    tree.children.fold("(" + tree.atom) { acc, child -> acc + " " + getPartOfTreeAsString(child) } + ")"
                }
            }
        }
        return getPartOfTreeAsString()
    }
}