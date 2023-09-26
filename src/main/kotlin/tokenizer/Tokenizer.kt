package tokenizer

class Tokenizer(private val source: String) {
    data class Err(val line: Int, val msg: String) : Error()

    private var line = 1
    private var start = 0
    private var current = 0
    private var tokens: MutableList<Token> = mutableListOf()

    companion object {
        private val keywords = mapOf(
            Pair("and", Token.And),
            Pair("class", Token.Class),
            Pair("else", Token.Else),
            Pair("false", Token.False),
            Pair("for", Token.For),
            Pair("fun", Token.Fun),
            Pair("if", Token.If),
            Pair("nil", Token.Nil),
            Pair("or", Token.Or),
            Pair("print", Token.Print),
            Pair("return", Token.Return),
            Pair("super", Token.Super),
            Pair("this", Token.This),
            Pair("true", Token.True),
            Pair("var", Token.Var),
            Pair("while", Token.While)
        )
    }

    fun scan(): List<Token> {
        while (!isEnd()) {
            scanToken()
            start = current
        }

        tokens.add(Token.EOF)
        return tokens
    }

    private fun scanToken() {
        val token = when (val ch = advance()) {
            '(' -> Token.LParen
            ')' -> Token.RParen
            '{' -> Token.LBrace
            '}' -> Token.RBrace
            ',' -> Token.Comma
            '.' -> Token.Dot
            '-' -> Token.Minus
            '+' -> Token.Plus
            ';' -> Token.Semi
            '*' -> Token.Star

            '/' -> if (match('/')) {
                while (!isEnd() && advance() != '\n');
                null
            } else Token.Slash

            '!' -> if (match('=')) Token.BangEqual else Token.Bang
            '=' -> if (match('=')) Token.EqualEqual else Token.Equal
            '>' -> if (match('=')) Token.GreaterEqual else Token.Greater
            '<' -> if (match('=')) Token.LessEqual else Token.Less

            '"' -> string()

            ' ', '\t', '\r' -> null
            '\n' -> {
                line++
                null
            }

            else -> when {
                isDigit(ch) -> number()
                isAlpha(ch) -> ident()
                else -> throw Err(line, "Unexpected token: '$ch'.")
            }
        }

        token?.let { tokens.add(it) }
    }

    private fun ident(): Token {
        while (!isEnd() && isAlphaNumeric(peek())) {
            advance()
        }

        val ident = source.substring(start, current)
        keywords[ident]?.let { return it }

        return Token.Ident(ident)
    }

    private fun number(): Token {
        while (!isEnd() && isDigit(peek())) advance()
        if (match('.')) while (!isEnd() && isDigit(peek())) advance()

        return Token.Num(source.substring(start, current).toDouble())
    }

    private fun isDigit(char: Char?) = char in '0'..'9'

    private fun isAlpha(char: Char?) = char in 'a'..'z' || char in 'A'..'Z'

    private fun isAlphaNumeric(char: Char?) = isAlpha(char) || isDigit(char)

    private fun string(): Token {
        while (!isEnd() && peek() != '"') {
            if (peek() == '\n') line++
            advance()
        }

        if (isEnd()) throw Err(line, "Unterminated string literal.")

        // Consume the final ".
        advance()

        return Token.Str(source.substring(start + 1, current - 1))
    }

    private fun advance(): Char? {
        if (isEnd()) {
            return null
        }

        val ch = source[current]
        current++

        return ch
    }

    private fun peek() = if (!isEnd()) source[current] else null

    private fun match(char: Char): Boolean {
        if (!isEnd() && source[current] == char) {
            advance()
            return true
        }

        return false
    }

    private fun isEnd() = source.length <= current
}
