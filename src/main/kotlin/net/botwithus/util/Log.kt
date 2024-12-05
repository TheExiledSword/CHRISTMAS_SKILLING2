package net.botwithus.util

import net.botwithus.rs3.script.ScriptConsole.println

class Log(private val debugMode: Boolean = true) {

    enum class Level {
        DEBUG, SUCCESS, WARN, ERROR
    }

    private fun log(level: Level, message: String) {
        var callingClass = ""
        val stackTrace = Thread.currentThread().stackTrace
        val callingMethod = stackTrace[3].methodName
        if (!debugMode) callingClass = stackTrace[3].className
        println("[${level.name}] [$callingClass.$callingMethod] $message")
    }

    fun debug(message: String) = log(Level.DEBUG, message)
    fun success(message: String) = log(Level.SUCCESS, message)
    fun warn(message: String) = log(Level.WARN, message)
    fun error(message: String) = log(Level.ERROR, message)
}