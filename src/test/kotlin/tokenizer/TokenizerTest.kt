package tokenizer

import org.junit.jupiter.api.Test
import tokenizer.Token.*

class TokenizerTest {
    @Test
    fun scan() {
        val source = """
            class Foo {
                bar(a) {
                    print a;
                    print this.a;
                }
            }
            
            var foo = Foo();
            var a = bar();
            
            print a + foo.bar();
            
            fun foo() {
                return 1 + "i am a string";
            }
            
            print a == b;
        """.trimIndent()

        val tokens = Tokenizer(source).scan()
        val expected = listOf(
            Class,
            Ident("Foo"),
            LBrace,
            Ident("bar"),
            LParen,
            Ident("a"),
            RParen,
            LBrace,
            Print,
            Ident("a"),
            Semi,
            Print,
            This,
            Dot,
            Ident("a"),
            Semi,
            RBrace,
            RBrace,
            Var,
            Ident("foo"),
            Equal,
            Ident("Foo"),
            LParen,
            RParen,
            Semi,
            Var,
            Ident("a"),
            Equal,
            Ident("bar"),
            LParen,
            RParen,
            Semi,
            Print,
            Ident("a"),
            Plus,
            Ident("foo"),
            Dot,
            Ident("bar"),
            LParen,
            RParen,
            Semi,
            Fun,
            Ident("foo"),
            LParen,
            RParen,
            LBrace,
            Return,
            Num(1.0),
            Plus,
            Str("i am a string"),
            Semi,
            RBrace,
            Print,
            Ident("a"),
            EqualEqual,
            Ident("b"),
            Semi,
            EOF,
        )

        kotlin.test.assertEquals(tokens, expected)
    }
}