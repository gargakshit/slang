package parser

import tokenizer.Token
import typesystem.SlangType

@Suppress("DuplicatedCode")
class Parser(private val tokens: List<Token>) {
    data class Err(val token: Token, val msg: String) : Error()

    private var current = 0

    fun stmt(): Stmt {
        if (match(Token.Print)) return print()
        return expressionStmt()
    }

    // print    -> "print" expression ;
    private fun print(): Stmt {
        val expr = expression()
        expect("Expected ;", Token.Semi)

        return Stmt.Print(expr)
    }

    // print    -> expression ;
    private fun expressionStmt(): Stmt {
        val expr = expression()
        expect("Expected ;", Token.Semi)

        return Stmt.Expression(expr)
    }

    private fun expression(): Expr = equality()

    // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private fun equality(): Expr {
        var left = comparison()

        while (match(Token.BangEqual, Token.EqualEqual)) {
            val operator = previous()
            val right = comparison()

            left = Expr.Binary(left, operator, right)
        }

        return left
    }

    // comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private fun comparison(): Expr {
        var left = term()

        while (match(Token.Greater, Token.GreaterEqual, Token.Less, Token.LessEqual)) {
            val operator = previous()
            val right = term()

            left = Expr.Binary(left, operator, right)
        }

        return left
    }

    // term          → factor ( ( "-" | "+" ) factor )* ;
    private fun term(): Expr {
        var left = factor()

        while (match(Token.Minus, Token.Plus)) {
            val operator = previous()
            val right = factor()

            left = Expr.Binary(left, operator, right)
        }

        return left
    }

    // factor         → unary ( ( "/" | "*" ) unary )* ;
    private fun factor(): Expr {
        var left = unary()

        while (match(Token.Slash, Token.Star)) {
            val operator = previous()
            val right = unary()

            left = Expr.Binary(left, operator, right)
        }

        return left
    }

    // unary          → ( "!" | "-" ) unary | primary ;

    private fun unary(): Expr {
        while (match(Token.Bang, Token.Minus)) {
            val operator = previous()
            val expr = unary()

            return Expr.Unary(operator, expr)
        }

        return primary()
    }

    // primary        → NUMBER | STRING | "true" | "false" | "nil"
    //               | "(" expression ")" ;
    private fun primary(): Expr =
        when (val token = advance()) {
            is Token.Num -> Expr.Literal(SlangType.Num(token.double))
            is Token.Str -> Expr.Literal(SlangType.Str(token.string))

            Token.True -> Expr.Literal(SlangType.Bool(true))
            Token.False -> Expr.Literal(SlangType.Bool(false))

            Token.Nil -> Expr.Literal(SlangType.Nil)

            Token.LParen -> {
                val expr = expression()
                if (!match(Token.RParen)) {
                    throw Err(token, "Expected ')' after expression.")
                }

                Expr.Grouping(expr)
            }

            else -> throw Err(token, "Unexpected token '$token'.")
        }

    private fun previous() = tokens[current - 1]

    private fun peek() = tokens[current]

    private fun expect(msg: String, vararg types: Token) {
        if (!match(*types)) throw Err(peek(), msg)
    }

    private fun check(type: Token) =
        if (isEnd()) false else when (val typ = peek()) {
            // Handling data classes.
            is Token.Num -> type is Token.Num
            is Token.Ident -> type is Token.Ident
            is Token.Str -> type is Token.Str
            // Everything else is an object.
            else -> typ == type
        }

    private fun match(vararg types: Token): Boolean {
        types.forEach {
            if (check(it)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun advance(): Token {
        val token = tokens[current]
        current++
        return token
    }

    private fun isEnd() = tokens[current] == Token.EOF
}
