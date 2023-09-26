package parser

import tokenizer.Token
import typesystem.SlangType

sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Unary(val operator: Token, val expr: Expr) : Expr()
    data class Grouping(val expr: Expr) : Expr()
    data class Literal(val value: SlangType) : Expr()

    interface Visitor<T> {
        fun visit(expr: Expr): T =
            when (expr) {
                is Binary -> visit(expr)
                is Unary -> visit(expr)
                is Grouping -> visit(expr)
                is Literal -> visit(expr)
            }

        fun visit(binary: Binary): T
        fun visit(unary: Unary): T
        fun visit(grouping: Grouping): T
        fun visit(literal: Literal): T
    }
}

sealed class Stmt {
    data class Expression(val expr: Expr) : Stmt()
    data class Print(val expr: Expr) : Stmt()

    interface Visitor<T> {
        fun visit(stmt: Stmt): T =
            when (stmt) {
                is Expression -> visit(stmt)
                is Print -> visit(stmt)
            }

        fun visit(expression: Expression): T
        fun visit(print: Print): T
    }
}

typealias Program = List<Stmt>
