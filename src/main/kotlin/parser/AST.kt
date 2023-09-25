package parser

import tokenizer.Token
import typesystem.SlangType

sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Unary(val operator: Token, val expr: Expr) : Expr()
    data class Grouping(val expr: Expr) : Expr()
    data class Literal(val value: SlangType) : Expr()

    abstract class Visitor<T> {
        fun accept(expr: Expr): T =
            when (expr) {
                is Binary -> visitBinary(expr)
                is Unary -> visitUnary(expr)
                is Grouping -> visitGrouping(expr)
                is Literal -> visitLiteral(expr)
            }

        abstract fun visitBinary(binary: Binary): T
        abstract fun visitUnary(unary: Unary): T
        abstract fun visitGrouping(grouping: Grouping): T
        abstract fun visitLiteral(literal: Literal): T
    }
}
