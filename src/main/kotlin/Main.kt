import analyzer.Analyzer
import analyzer.BareReturnAnalyzer
import analyzer.EmptyLoopAnalyzer
import interpreter.Interpreter
import interpreter.Resolver
import parser.Parser
import parser.Program
import tokenizer.Tokenizer
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) repl()
    else runFile(args[0])
}

val analyzers = listOf(BareReturnAnalyzer(), EmptyLoopAnalyzer())

fun analyze(program: Program) {
    analyzers.forEach { it.run(program) }
}

fun runFile(path: String) {
    try {
        val file = File(path)

        val content = file.readText()
        val tokens = Tokenizer(content).scan()
        val program = Parser(tokens).parse()
        val locals = Resolver().resolve(program)

        analyze(program)

        Interpreter(locals).interpret(program)
    } catch (e: Tokenizer.Err) {
        println("Tokenizer error on line ${e.line}: ${e.msg}")
    } catch (e: Parser.Err) {
        println("Parser error on '${e.token}': '${e.msg}'")
    } catch (e: Interpreter.TypeErr) {
        println("TypeError: '${e.msg}'.")
    } catch (e: Interpreter.UnreachableErr) {
        println("WE MESSED UP BIG TIME")
    } catch (e: Interpreter.UndefinedVarErr) {
        println("Undefined variable '${e.ident.ident}'.")
    } catch (e: Interpreter.VarAlreadyDefinedErr) {
        println("Variable '${e.ident.ident}' already defined.")
    } catch (e: Interpreter.ArityErr) {
        println("${e.name} requires ${e.expected} arguments, got ${e.got}.")
    }
}

fun repl() {
    while (true) {
        try {
            print("slang> ")

            val line = readlnOrNull() ?: return
            val tokens = Tokenizer(line).scan()
            println("Tokens: $tokens")

            val program = Parser(tokens).parse()
            println("AST: $program")

            val locals = Resolver().resolve(program)
            println("Locals: $locals")

            Interpreter(locals).interpret(program)
        } catch (e: Tokenizer.Err) {
            println("Tokenizer error on line ${e.line}: ${e.msg}")
        } catch (e: Parser.Err) {
            println("Parser error on '${e.token}': '${e.msg}'")
        } catch (e: Interpreter.TypeErr) {
            println("Type error: '${e.msg}'.")
        } catch (e: Interpreter.UnreachableErr) {
            println("WE MESSED UP BIG TIME")
        } catch (e: Interpreter.UndefinedVarErr) {
            println("Undefined variable '${e.ident.ident}'.")
        } catch (e: Interpreter.VarAlreadyDefinedErr) {
            println("Variable '${e.ident.ident}' already defined.")
        }
    }
}
