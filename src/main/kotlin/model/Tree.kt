package model

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import evaluators.BinarisedNodeParser
import utils.getWindow
import utils.joinToStringWithStartAndEnd

class Tree(var atom: String, val children: MutableList<Tree> = mutableListOf()) {
    fun addChild(expression: Tree) {
        children.add(expression)
    }

    fun transformToRule(tree: Tree = this): Rule {
        val childrenString = tree.children.joinToString(separator = " ") { it.atom }
        return Rule(tree.isPreterminal(), tree.atom, childrenString.split(" "))
    }

    fun transformToRules(): ArrayList<Rule> {
        val rules: ArrayList<Rule> = ArrayList()
        val depth = DeepRecursiveFunction<Tree, Unit> {
            if (it.children.isEmpty()) return@DeepRecursiveFunction
            rules.add(transformToRule(it))
            it.children.map { child -> callRecursive(child) }
        }
        depth(this)
        return rules
    }

    fun getLeaves(): List<String> {
        val leaves = mutableListOf<String>()
        if (children.isEmpty()) leaves.add(atom) else children.forEach{leaves.addAll(it.getLeaves())}
        return leaves
    }

    fun setLeaves(leaves: List<String>) {
        var i = 0
        fun setLeaves(tree: Tree = this) {
            when {
                tree.children.isEmpty() -> {
                    tree.atom = leaves.getOrElse(i++) { tree.atom }
                }
                else -> {
                    tree.children.forEach {
                        setLeaves(it)
                    }
                }
            }
        }
        setLeaves()
    }

    fun binarise(vertical: Int, horizontal: Int): Tree {
        val binarisedNodeParser = BinarisedNodeParser()
        val parents = ArrayDeque<String>()

        fun addParents(atom: String, isAddedNode: Boolean): String {
            val newParents = if (isAddedNode) parents.drop(1) else parents
            return if (vertical == 1 || newParents.isEmpty()) atom else {
                newParents.joinToStringWithStartAndEnd(
                    prefix = "$atom^<", separator = ",", postfix = ">", limit = vertical - 1
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
            val (label, childrenAsStrings, _) = binarisedNodeParser.parseToEnd(tree.atom)
            val isAddedNode = childrenAsStrings.isNotEmpty()
            when {
                tree.isPreterminal() -> return tree
                numberOfChildren <= 2 -> {
                    addLableToParents(label, isAddedNode)
                    val binarisedChildren = tree.children.map { binarise(it) }
                    removeLableToParents(isAddedNode)
                    return Tree(
                        atom = addParents(tree.atom, isAddedNode),
                        children = binarisedChildren as MutableList<Tree>
                    )
                }

                else -> {
                    val childrenOfNewSecondChild = tree.children.getWindow(mutableListOf(), 1, horizontal)
                    val newSecondChildAtom = childrenOfNewSecondChild.joinToString(
                        prefix = "$label|<",
                        separator = ",",
                        postfix = ">"
                    ) { child -> child.atom }
                    addLableToParents(label, isAddedNode)
                    val binarisedFirstChild = binarise(tree.children.first())
                    val binarisedNewSecondChild = binarise(
                        Tree(
                            atom = newSecondChildAtom,
                            children = childrenOfNewSecondChild
                        )
                    )
                    removeLableToParents(isAddedNode)
                    return Tree(
                        atom = addParents(tree.atom, isAddedNode),
                        children = mutableListOf(binarisedFirstChild, binarisedNewSecondChild)
                    )
                }
            }
        }

        return binarise()
    }

    fun debinarise(tree: Tree = this): Tree {
        val binarisedNodeParser = BinarisedNodeParser()
        tree.atom = BinarisedNodeParser().parseToEnd(tree.atom).t1

        if (tree.isPreterminal()) return tree
        val lastChildOfSelectedTree = tree.children.last()
        val (_, childrenOfLastChildAsString , _) = binarisedNodeParser.parseToEnd(lastChildOfSelectedTree.atom)

        if (childrenOfLastChildAsString.isNotEmpty()) {
            tree.children.removeLast()
            lastChildOfSelectedTree.children.forEach {
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