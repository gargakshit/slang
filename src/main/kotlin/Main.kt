import interpreter.Interpreter
import parser.Parser
import tokenizer.Tokenizer

fun main() {
    repl()
}

fun repl() {
    val interpreter = Interpreter()

    while (true) {
        try {
            print("slang> ")

            val line = readlnOrNull() ?: return
            val tokens = Tokenizer(line).scan()
            println("Tokens: $tokens")

            val program = Parser(tokens).parse()
            println("AST: $program")

            interpreter.interpret(program)
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
