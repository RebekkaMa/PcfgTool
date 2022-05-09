package model

class Rule(val lexical: Boolean, val lhs: String, val rhs: List<String>) {
    override fun toString(): String {
        return lhs + " -> " + rhs.joinToString(" ")
    }

    override fun equals(other: Any?): Boolean {
        if (other is Rule) {
            return lhs == other.lhs && rhs == other.rhs
        }
        return false
    }

    override fun hashCode(): Int {
        return lhs.hashCode()*31 + rhs.hashCode()
    }
}