package interpreter

import parser.Expr
import parser.Stmt
import tokenizer.Token
import java.util.Stack

class Resolver : Stmt.Visitor<Unit>, Expr.Visitor<Unit> {
    data class InitializerError(val ident: Token.Ident) : Error()

    private val scopes = Stack<MutableMap<String, Boolean>>()

    // Expr -> depth
    private val locals = mutableMapOf<Int, Int>()

    fun resolve(stmts: List<Stmt>): Map<Int, Int> {
        stmts.forEach { visit(it) }
        return locals
    }

    private fun resolveLocal(expr: Expr, ident: Token.Ident) {
        for (i in (scopes.size - 1) downTo 0) {
            val scope = scopes[i]
            if (scope.containsKey(ident.ident)) {
                locals[System.identityHashCode(expr)] = scopes.size - 1 - i
                break
            }
        }
    }

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(token: Token.Ident) {
        if (scopes.isEmpty()) return;
        scopes.peek()[token.ident] = false
    }

    private fun define(token: Token.Ident) {
        if (scopes.isEmpty()) return;
        scopes.peek()[token.ident] = true
    }

    override fun visit(binary: Expr.Binary) {
        visit(binary.left)
        visit(binary.right)
    }

    override fun visit(unary: Expr.Unary) {
        visit(unary.expr)
    }

    override fun visit(grouping: Expr.Grouping) {
        visit(grouping.expr)
    }

    override fun visit(literal: Expr.Literal) {}

    override fun visit(slangVar: Expr.Variable) {
        if (!scopes.isEmpty() && scopes.peek()[slangVar.ident.ident] == false)
            throw InitializerError(slangVar.ident)

        resolveLocal(slangVar, slangVar.ident)
    }

    override fun visit(assignment: Expr.Assignment) {
        visit(assignment.expr)
        resolveLocal(assignment, assignment.ident)
    }

    override fun visit(call: Expr.Call) {
        visit(call.callee)
        call.args.forEach { visit(it) }
    }

    override fun visit(expression: Stmt.Expression) {
        visit(expression.expr)
    }

    override fun visit(print: Stmt.Print) {
        visit(print.expr)
    }

    override fun visit(slangVar: Stmt.Var) {
        declare(slangVar.ident)
        visit(slangVar.expr)
        define(slangVar.ident)
    }

    override fun visit(slangIf: Stmt.If) {
        visit(slangIf.cond)
        visit(slangIf.then)
        slangIf.elze?.let { visit(it) }
    }

    override fun visit(block: Stmt.Block) {
        beginScope()
        block.stmts.forEach { visit(it) }
        endScope()
    }

    override fun visit(slangWhile: Stmt.While) {
        visit(slangWhile.cond)
        visit(slangWhile.stmt)
    }

    override fun visit(slangFun: Stmt.Fun) {
        declare(slangFun.name)
        define(slangFun.name)

        beginScope()
        slangFun.args.forEach {
            declare(it)
            define(it)
        }
        slangFun.body.forEach { visit(it) }
        endScope()
    }

    override fun visit(slangReturn: Stmt.Return) {
        visit(slangReturn.expr)
    }
}
