package tokenizer

sealed class Token {
    // Single character tokens.
    data object LParen : Token()
    data object RParen : Token()
    data object LBrace : Token()
    data object RBrace : Token()
    data object Semi : Token()
    data object Plus : Token()
    data object Minus : Token()
    data object Slash : Token()
    data object Star : Token()
    data object Comma : Token()
    data object Dot : Token()

    // Single or 2 character tokens.
    data object Bang : Token()
    data object BangEqual : Token()
    data object Equal : Token()
    data object EqualEqual : Token()
    data object Less : Token()
    data object LessEqual : Token()
    data object Greater : Token()
    data object GreaterEqual : Token()

    // Literals.
    data class Ident(val ident: String) : Token()
    data class Str(val string: String) : Token()
    data class Num(val double: Double) : Token()

    // Operators.
    data object And : Token()
    data object Class : Token()
    data object Else : Token()
    data object False : Token()
    data object Fun : Token()
    data object For : Token()
    data object If : Token()
    data object Nil : Token()
    data object Or : Token()
    data object Print : Token()
    data object Return : Token()
    data object Super : Token()
    data object This : Token()
    data object True : Token()
    data object Var : Token()
    data object While : Token()

    // Special.
    data object EOF : Token()
}
