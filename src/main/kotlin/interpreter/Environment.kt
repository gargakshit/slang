package interpreter

import typesystem.SlangType

class Environment private constructor(private val parent: Environment?) {
    data class ScopeError(val depth: Int) : Error()

    private val vars: MutableMap<String, SlangType> = HashMap()

    companion object {
        fun new() = Environment(null)
    }

    fun defineVar(name: String, value: SlangType): Boolean {
        if (vars.containsKey(name)) return false

        vars[name] = value
        return true
    }

    fun setVar(name: String, value: SlangType): Boolean {
        val env = getEnv(name)
        if (env != null) {
            env.vars[name] = value
            return true
        }

        return false
    }

    fun getVar(name: String): SlangType? =
        getEnv(name)?.vars?.get(name)

    fun fork(): Environment = Environment(this)

    fun getAt(depth: Int, name: String): SlangType? =
        getEnvAt(depth).vars[name]

    fun setAt(depth: Int, name: String, value: SlangType) {
        getEnvAt(depth).vars[name] = value
    }

    private fun getEnvAt(depth: Int): Environment {
        var env = this
        for (i in 0..<depth) {
            val parent = env.parent ?: throw ScopeError(depth)
            env = parent
        }

        return env
    }

    private fun getEnv(name: String): Environment? {
        if (vars.containsKey(name)) return this
        return parent?.getEnv(name)
    }
}
