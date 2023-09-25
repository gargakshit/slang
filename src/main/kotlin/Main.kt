import interpreter.Interpreter
import parser.Parser
import tokenizer.Tokenizer

fun main() {
    repl()
}

fun repl() {
    while (true) {
        print("slang> ")

        val line = readlnOrNull() ?: return
        val tokens = Tokenizer(line).scan()
        println("Tokens: $tokens")

        val expr = Parser(tokens).expression()
        println("AST: $expr")

        val value = Interpreter().accept(expr)
        println("Evaluated: $value")
    }
}
