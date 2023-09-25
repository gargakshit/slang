package interpreter

import parser.Expr
import tokenizer.Token
import typesystem.SlangType

class Interpreter : Expr.Visitor<SlangType>() {
    class UnreachableErr : Error()
    data class TypeErr(val msg: String) : Error()

    override fun visitBinary(binary: Expr.Binary): SlangType {
        val left = accept(binary.left)
        val right = accept(binary.right)

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

    override fun visitUnary(unary: Expr.Unary): SlangType {
        val value = accept(unary.expr)

        return when (unary.operator) {
            Token.Minus -> SlangType.Num(-value.num())
            Token.Bang -> SlangType.Bool(!isTruthy(value))
            else -> throw UnreachableErr()
        }
    }

    override fun visitGrouping(grouping: Expr.Grouping): SlangType =
        accept(grouping.expr)

    override fun visitLiteral(literal: Expr.Literal): SlangType =
        literal.value

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
