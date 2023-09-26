import interpreter.Interpreter
import parser.Parser
import tokenizer.Tokenizer

fun main() {
    repl()
}

fun repl() {
    while (true) {
        try {
            print("slang> ")

            val line = readlnOrNull() ?: return
            val tokens = Tokenizer(line).scan()
            println("Tokens: $tokens")

            val stmt = Parser(tokens).stmt()
            println("AST: $stmt")

            Interpreter().interpret(listOf(stmt))
        } catch (e: Tokenizer.Err) {
            println("Tokenizer error on line ${e.line}: ${e.msg}")
        } catch (e: Parser.Err) {
            println("Parser error: ${e.msg}")
        } catch (e: Interpreter.TypeErr) {
            println("Type error: ${e.msg}")
        } catch (e: Interpreter.UnreachableErr) {
            println("WE MESSED UP BIG TIME")
        }
    }
}
