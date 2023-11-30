package analyzer

import parser.Expr
import parser.Program
import parser.Stmt

abstract class Analyzer(private val name: String, private val description: String) :
    Expr.Visitor<Unit>,
    Stmt.Visitor<Unit> {
    fun run(program: Program) {
        println("$name: $description")
        program.forEach { visit(it) }
    }

    private fun diagnostic(repr: String, message: String) {
        println(
            "[Diagnostic]: $repr\n" +
                    "              $message"
        )
    }

    protected fun diagnostic(expr: Expr, message: String) = diagnostic(expr.toString(), message)
    protected fun diagnostic(stmt: Stmt, message: String) = diagnostic(stmt.toString(), message)

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

    override fun visit(slangVar: Expr.Variable) {}

    override fun visit(assignment: Expr.Assignment) {
        visit(assignment.expr)
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
        visit(slangVar.expr)
    }

    override fun visit(slangIf: Stmt.If) {
        visit(slangIf.cond)
        visit(slangIf.then)
        slangIf.elze?.let { visit(it) }
    }

    override fun visit(block: Stmt.Block) {
        block.stmts.forEach { visit(it) }
    }

    override fun visit(slangWhile: Stmt.While) {
        visit(slangWhile.cond)
        visit(slangWhile.stmt)
    }

    override fun visit(slangFun: Stmt.Fun) {
        slangFun.body.forEach { visit(it) }
    }

    override fun visit(slangReturn: Stmt.Return) {
        visit(slangReturn.expr)
    }
}
