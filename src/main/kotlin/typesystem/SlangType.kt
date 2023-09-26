package typesystem

sealed class SlangType {
    data class Str(val string: String) : SlangType() {
        override fun toString() = string
    }

    data class Num(val double: Double) : SlangType() {
        override fun toString() = double.toString()
    }

    data class Bool(val bool: Boolean) : SlangType() {
        override fun toString() = bool.toString()
    }

    data object Nil : SlangType() {
        override fun toString() = "nil"
    }

    fun typeString() = when (this) {
        is Str -> "string"
        is Num -> "number"
        is Bool -> "boolean"
        Nil -> "nil"
    }
}
