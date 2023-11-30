package analyzer

import parser.Stmt

class BareReturnAnalyzer : Analyzer("bare-return", "Detects bare return statements") {
    private var functionDepth = 0

    override fun visit(slangFun: Stmt.Fun) {
        functionDepth++
        super.visit(slangFun)
        functionDepth--
    }

    override fun visit(slangReturn: Stmt.Return) {
        if (functionDepth <= 0)
            diagnostic(slangReturn, "Bare return statement detected.")

        super.visit(slangReturn)
    }
}
