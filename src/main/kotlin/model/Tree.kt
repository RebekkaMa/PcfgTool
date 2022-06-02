package model

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import evaluators.MarkovizationNodeEvaluator
import java.util.*

class Tree(var atom: String, val children: ArrayList<Tree> = ArrayList()) {
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

    fun getLeaves() : List<String>{
        val leaves = mutableListOf<String>()
        when{
            children.isEmpty() -> leaves.add(atom)
            else -> {
                children.forEach { leaves.addAll(it.getLeaves()) }
            }
        }
        return leaves
    }

    fun setLeaves(leaves : List<String>){
        var i = 0
        fun setLeavesPartOfTree(tree: Tree = this){
            when{
                tree.children.isEmpty() -> {
                    tree.atom = leaves.getOrElse(i){tree.atom}
                    i++
                }
                else -> {
                    tree.children.forEach{
                        setLeavesPartOfTree(it)
                    }
                }
            }
        }
        setLeavesPartOfTree()
    }

    fun binarise(vertical: Int, horizontal: Int): Tree {
        val intRange = 0 until vertical
        val parents = Stack<String>()

        fun addParents(atom : String, isAddedNode : Boolean): String{
            val newParents = if (isAddedNode) parents.dropLast(1) else parents
             return when{
                vertical == 1 || parents.isEmpty() -> atom
                parents.size >= vertical -> {
                    newParents.slice(intRange).reversed().joinToString(prefix = "$atom^<", separator = ",", postfix = ">")
                }
                else -> {
                    newParents.reversed().joinToString(prefix = "$atom^<", separator = ",", postfix = ">")
                }
            }
        }

        fun binarise(tree: Tree = this): Tree {
            val childrenCount = tree.children.size
            val isAddedNode = tree.atom.contains("[\\w\\p{Punct}&&[^\\s\\(\\)<>\\^]]+\\|<".toRegex())
            println(parents.forEach{ println(it) })
            println(isAddedNode)
            println(tree.atom)
            println("---------")
            when {
                tree.isPreterminal() -> return tree
                childrenCount <= 2 -> {
                    val atomWithParents = addParents(tree.atom, isAddedNode)
                    if (!isAddedNode) parents.push(tree.atom)
                    val markovizedChildren = tree.children.map { binarise(it) }
                    if (!isAddedNode) parents.pop()
                    return Tree(atomWithParents, markovizedChildren as ArrayList<Tree>)
                }
                else -> {
                    var markovizedChildrenRhsSize = 0
                    val childrenRhsSide = tree.children.drop(1).takeWhile {
                        markovizedChildrenRhsSize++
                        markovizedChildrenRhsSize <= horizontal
                    }
                    val atomWithChildren = childrenRhsSide.joinToString(prefix = tree.atom + "|<", separator = ",", postfix = ">"){ child -> child.atom}
                    val atomWithParents = addParents(tree.atom, isAddedNode)
                    if (!isAddedNode) parents.push(tree.atom)
                    val markovizedChildLhs = binarise(tree.children.first())
                    val markovizedChildrRhs = binarise(Tree(atomWithChildren, childrenRhsSide as ArrayList<Tree>))
                    if (!isAddedNode) parents.pop()
                    return Tree(atomWithParents, arrayListOf( markovizedChildLhs, markovizedChildrRhs))
                }
            }
        }
        return binarise()
    }

    fun debinarise(tree: Tree = this): Tree {
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
            return debinarise(tree)
        }
        tree.children.onEach { debinarise(it) }
        return tree
    }

    fun isPreterminal(): Boolean {
        return this.children.first().children.isEmpty() ?: false
    }

    override fun toString(): String {
        fun getPartOfTree(tree: Tree = this): String {
            return when (tree.children.size) {
                0 -> tree.atom
                1 -> "(" + tree.atom + " " + getPartOfTree(tree.children.first()) + ")"
                else -> {
                    val partOfTreeAsString =
                        tree.children.fold("(" + tree.atom) { acc, child -> acc + " " + getPartOfTree(child) }
                    "$partOfTreeAsString)"
                }
            }
        }
        return getPartOfTree()
    }
}