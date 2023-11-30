package interpreter

import parser.Expr
import parser.Program
import parser.Stmt
import tokenizer.Token
import typesystem.SlangType

class Interpreter(private val locals: Map<Int, Int>) : Expr.Visitor<SlangType>, Stmt.Visitor<Unit> {
    class UnreachableErr : Error()
    data class TypeErr(val msg: String) : Error()
    data class UndefinedVarErr(val ident: Token.Ident) : Error()
    data class VarAlreadyDefinedErr(val ident: Token.Ident) : Error()
    data class ArityErr(val got: Int, val expected: Int, val name: String) : Error()

    private data class ReturnValue(val value: SlangType) : Error()

    private val globals = Environment.new()
    private var environment = globals

    init {
        globals.defineVar("clock", object : SlangType.Callable {
            override fun call(interpreter: Interpreter, args: List<SlangType>): SlangType {
                return SlangType.Num(System.currentTimeMillis().toDouble())
            }
        })

        globals.defineVar("str", object : SlangType.Callable {
            override fun call(interpreter: Interpreter, args: List<SlangType>): SlangType {
                if (args.size != 1) throw ArityErr(args.size, 1, "str")
                return SlangType.Str(args[0].toString())
            }
        })

        globals.defineVar("str_part", object : SlangType.Callable {
            override fun call(interpreter: Interpreter, args: List<SlangType>): SlangType {
                if (args.size != 3) throw ArityErr(args.size, 3, "str_part")
                return SlangType.Str(args[0].str().split(args[1].str())[args[2].num().toInt()])
            }
        })

        globals.defineVar("str_count", object : SlangType.Callable {
            override fun call(interpreter: Interpreter, args: List<SlangType>): SlangType {
                if (args.size != 2) throw ArityErr(args.size, 2, "str_count")
                val arg1 = args[1].str()
                return SlangType.Num(args[0].str().count { it.toString() == arg1 }.toDouble())
            }
        })

        globals.defineVar("str_to_num", object : SlangType.Callable {
            override fun call(interpreter: Interpreter, args: List<SlangType>): SlangType {
                if (args.size != 1) throw ArityErr(args.size, 1, "str_to_num")
                return args[0].str().toDoubleOrNull()?.let { SlangType.Num(it) } ?: SlangType.Nil
            }
        })
    }

    fun interpret(program: Program) =
        program.forEach { visit(it) }

    fun interpret(stmts: List<Stmt>, env: Environment): SlangType {
        val oldEnv = environment
        environment = env

        return try {
            interpret(stmts)
            environment = oldEnv
            SlangType.Nil
        } catch (e: ReturnValue) {
            environment = oldEnv
            e.value
        }
    }

    /*
        Expressions.
     */

    override fun visit(binary: Expr.Binary): SlangType {
        val left = visit(binary.left)
        val right = visit(binary.right)

        return when (binary.operator) {
            Token.Plus -> {
                when (left) {
                    is SlangType.Num -> SlangType.Num(left.double + right.num())
                    is SlangType.Str -> SlangType.Str(left.string + right.str())
                    else -> throw TypeErr("Can only add strings or numbers.")
                }
            }

            Token.Minus -> SlangType.Num(left.num() - right.num())
            Token.Star -> SlangType.Num(left.num() * right.num())
            Token.Slash -> SlangType.Num(left.num() / right.num())

            Token.Less -> SlangType.Bool(left.num() < right.num())
            Token.LessEqual -> SlangType.Bool(left.num() <= right.num())
            Token.Greater -> SlangType.Bool(left.num() > right.num())
            Token.GreaterEqual -> SlangType.Bool(left.num() >= right.num())

            Token.EqualEqual -> SlangType.Bool(isEqual(left, right))
            Token.BangEqual -> SlangType.Bool(!isEqual(left, right))

            else -> throw UnreachableErr()
        }
    }

    override fun visit(unary: Expr.Unary): SlangType {
        val value = visit(unary.expr)

        return when (unary.operator) {
            Token.Minus -> SlangType.Num(-value.num())
            Token.Bang -> SlangType.Bool(!isTruthy(value))
            else -> throw UnreachableErr()
        }
    }

    override fun visit(grouping: Expr.Grouping): SlangType =
        visit(grouping.expr)

    override fun visit(literal: Expr.Literal): SlangType =
        literal.value

    override fun visit(slangVar: Expr.Variable): SlangType {
        val depth = locals[System.identityHashCode(slangVar)]
        if (depth != null)
            return environment.getAt(depth, slangVar.ident.ident)!!

        return globals.getVar(slangVar.ident.ident) ?: throw UndefinedVarErr(slangVar.ident)
    }

    override fun visit(assignment: Expr.Assignment): SlangType {
        val value = visit(assignment.expr)

        val depth = locals[System.identityHashCode(assignment)]
        if (depth != null) {
            environment.setAt(depth, assignment.ident.ident, value)
        } else {
            globals.setVar(assignment.ident.ident, value)
        }

        return value
    }

    override fun visit(call: Expr.Call): SlangType {
        return visit(call.callee)
            .callable()
            .call(this, call.args.map { visit(it) })
    }

    /*
        Statements.
     */

    override fun visit(expression: Stmt.Expression) {
        visit(expression.expr)
    }

    override fun visit(print: Stmt.Print) {
        println(visit(print.expr))
    }

    override fun visit(slangVar: Stmt.Var) {
        if (!environment.defineVar(slangVar.ident.ident, visit(slangVar.expr)))
            throw VarAlreadyDefinedErr(slangVar.ident)
    }

    override fun visit(slangIf: Stmt.If) {
        if (isTruthy(visit(slangIf.cond))) visit(slangIf.then)
        else slangIf.elze?.let { visit(it) }
    }

    override fun visit(block: Stmt.Block) {
        val env = environment

        environment = environment.fork()
        interpret(block.stmts)
        environment = env
    }

    override fun visit(slangWhile: Stmt.While) {
        while (isTruthy(visit(slangWhile.cond)))
            visit(slangWhile.stmt)
    }

    override fun visit(slangFun: Stmt.Fun) {
        if (!environment.defineVar(slangFun.name.ident, SlangType.Function(slangFun, environment)))
            throw VarAlreadyDefinedErr(slangFun.name)
    }

    override fun visit(slangReturn: Stmt.Return) {
        throw ReturnValue(visit(slangReturn.expr))
    }

    private fun isTruthy(value: SlangType) =
        when (value) {
            SlangType.Nil -> false
            is SlangType.Bool -> value.bool
            else -> true
        }

    private fun isEqual(left: SlangType, right: SlangType): Boolean =
        when (left) {
            is SlangType.Num -> right != SlangType.Nil && left.double == right.num()
            is SlangType.Str -> right != SlangType.Nil && left.string == right.str()
            is SlangType.Bool -> right != SlangType.Nil && left.bool == right.bool()
            SlangType.Nil -> right == SlangType.Nil
            is SlangType.Callable -> false
        }
}

private fun SlangType.num(): Double =
    when (this) {
        is SlangType.Num -> double
        else -> throw Interpreter.TypeErr("Expected a number, got ${typeString()}.")
    }

private fun SlangType.bool(): Boolean =
    when (this) {
        is SlangType.Bool -> bool
        else -> throw Interpreter.TypeErr("Expected a boolean, got ${typeString()}.")
    }

private fun SlangType.str(): String =
    when (this) {
        is SlangType.Str -> string
        else -> throw Interpreter.TypeErr("Expected a string, got ${typeString()}.")
    }

private fun SlangType.callable(): SlangType.Callable =
    when (this) {
        is SlangType.Callable -> this
        else -> throw Interpreter.TypeErr("Can only call a callable.")
    }
