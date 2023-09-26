package interpreter

import typesystem.SlangType

class Environment private constructor(private val parent: Environment?) {
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

    private fun getEnv(name: String): Environment? {
        if (vars.containsKey(name)) return this
        return parent?.getEnv(name)
    }
}
