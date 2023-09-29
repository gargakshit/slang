package parser

import tokenizer.Token
import typesystem.SlangType

sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Unary(val operator: Token, val expr: Expr) : Expr()
    data class Grouping(val expr: Expr) : Expr()
    data class Literal(val value: SlangType) : Expr()
    data class Variable(var ident: Token.Ident) : Expr()
    data class Assignment(val ident: Token.Ident, val expr: Expr) : Expr()

    interface Visitor<T> {
        fun visit(expr: Expr): T =
            when (expr) {
                is Binary -> visit(expr)
                is Unary -> visit(expr)
                is Grouping -> visit(expr)
                is Literal -> visit(expr)
                is Variable -> visit(expr)
                is Assignment -> visit(expr)
            }

        fun visit(binary: Binary): T
        fun visit(unary: Unary): T
        fun visit(grouping: Grouping): T
        fun visit(literal: Literal): T
        fun visit(slangVar: Variable): T
        fun visit(assignment: Assignment): T
    }
}

sealed class Stmt {
    data class Expression(val expr: Expr) : Stmt()
    data class Print(val expr: Expr) : Stmt()
    data class Var(val ident: Token.Ident, val expr: Expr) : Stmt()
    data class If(val cond: Expr, val then: Stmt, val elze: Stmt?) : Stmt()
    data class Block(val stmts: List<Stmt>) : Stmt()
    data class While(val cond: Expr, val stmt: Stmt) : Stmt()

    interface Visitor<T> {
        fun visit(stmt: Stmt): T =
            when (stmt) {
                is Expression -> visit(stmt)
                is Print -> visit(stmt)
                is Var -> visit(stmt)
                is If -> visit(stmt)
                is Block -> visit(stmt)
                is While -> visit(stmt)
            }

        fun visit(expression: Expression): T
        fun visit(print: Print): T
        fun visit(slangVar: Var): T
        fun visit(slangIf: If): T
        fun visit(block: Block): T
        fun visit(slangWhile: While) : T
    }
}

typealias Program = List<Stmt>
