package analyzer

import parser.Stmt

class EmptyLoopAnalyzer :
    Analyzer("empty-loop", "Detects loops with empty bodies") {
    override fun visit(slangWhile: Stmt.While) {
        when (val stmt = slangWhile.stmt) {
            is Stmt.Block -> {
                visit(slangWhile.cond)
                if (stmt.stmts.isEmpty())
                    diagnostic(slangWhile, "Empty loop body detected")
            }

            else -> super.visit(slangWhile)
        }
    }
}
