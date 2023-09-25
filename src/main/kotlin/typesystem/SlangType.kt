package typesystem

sealed class SlangType {
    data class Str(val string: String) : SlangType()
    data class Num(val double: Double) : SlangType()
    data class Bool(val bool: Boolean) : SlangType()
    data object Nil : SlangType()

    fun typeString() = when (this) {
        is Str -> "string"
        is Num -> "number"
        is Bool -> "boolean"
        Nil -> "nil"
    }
}
