package typesystem

import interpreter.Environment
import interpreter.Interpreter
import parser.Stmt.Fun

sealed interface SlangType {
    data class Str(val string: String) : SlangType {
        override fun toString() = string
    }

    data class Num(val double: Double) : SlangType {
        override fun toString() = double.toString()
    }

    data class Bool(val bool: Boolean) : SlangType {
        override fun toString() = bool.toString()
    }

    data object Nil : SlangType {
        override fun toString() = "nil"
    }

    interface Callable : SlangType {
        fun call(interpreter: Interpreter, args: List<SlangType>): SlangType
    }

    data class Function(val decl: Fun) : Callable {
        override fun call(interpreter: Interpreter, args: List<SlangType>): SlangType {
            val env = interpreter.globals.fork()
            for (i in args.indices)
                env.defineVar(decl.args[i].ident, args[i])

            return interpreter.interpret(decl.body, env)
        }

        override fun toString() = "<fun>"
    }

    fun typeString() = when (this) {
        is Str -> "string"
        is Num -> "number"
        is Bool -> "boolean"
        Nil -> "nil"
        is Callable -> "<callable>"
    }
}
