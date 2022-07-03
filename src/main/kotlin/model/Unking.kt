package model

fun replaceTokensByInts(
    lexiconByString: Map<String, Int>,
    tokensAsString: List<String>,
    unking: Boolean,
    smoothing: Boolean
): IntArray {
    return tokensAsString.mapIndexed { index, word ->
        val wordAsInt = lexiconByString[word]
        return@mapIndexed when {
            wordAsInt != null -> wordAsInt //TODO
            smoothing -> lexiconByString[getSignature(word, index + 1)] ?: -1
            unking -> lexiconByString["UNK"] ?: -1
            else -> -1
        }
    }.toIntArray()
}

fun getTerminalCountFromCorpus(corpus: List<Tree>): MutableMap<String, Int> {
    val wordcount = mutableMapOf<String, Int>()
    corpus.forEach { tree ->
        tree.getLeaves().forEach { leave ->
            wordcount.compute(leave) { _, count ->
                count?.inc() ?: 1
            }
        }
    }
    return wordcount
}

fun replaceRareWordsInTree(smooth: Boolean, wordcount: MutableMap<String, Int>, threshold: Int, tree: Tree): Tree {
    val newLeaves = mutableListOf<String>()
    tree.getLeaves().forEachIndexed { index, leave ->
        val newLeave = when {
            (wordcount[leave] ?: 0) <= threshold -> {
                if (smooth) getSignature(leave, index + 1) else "UNK"
            }
            else -> {
                leave
            }
        }
        newLeaves.add(newLeave)
    }
    tree.setLeaves(newLeaves)
    return tree
}

fun getSignature(word: String, positionInSentence: Int): String {
    if (word.isEmpty()) return "UNK"
    val letterSuffix = when {
        word.first().isUpperCase() && word.none { it.isLowerCase() } -> "-AC"
        word.first().isUpperCase() && positionInSentence == 1 -> "-SC"
        word.first().isUpperCase() -> "-C"
        word.any { it.isLowerCase() } -> "-L"
        word.any { it.isLetter() } -> "-U"
        else -> "-S"
    }
    val numberSuffix = when {
        word.all { it.isDigit() } -> "-N"
        word.any { it.isDigit() } -> "-n"
        else -> {
            ""
        }
    }
    val dashSuffix = when {
        word.any { it == '-' } -> "-H"
        else -> ""
    }
    val periodSuffix = when {
        word.any { it == '.' } -> "-P"
        else -> ""
    }
    val commaSuffix = when {
        word.any { it == ',' } -> "-C"
        else -> ""
    }
    val wordSuffix = when {
        word.count() > 3 && word.last().isLetter() -> word.last().lowercase()
        else -> ""
    }
    return "UNK$letterSuffix$numberSuffix$dashSuffix$periodSuffix$commaSuffix$wordSuffix"
}

