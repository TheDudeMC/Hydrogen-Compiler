package hcc.base.logging

class TDLogger {
    companion object {
        val LEVEL = TDLoggerLevel.DEBUG

        fun v(tag: String, message: String) {
            out(TDLoggerLevel.VERBOSE, "[V]: ${tag.uppercase()} ~> $message")
        }

        fun i(tag: String, message: String) {
            out(TDLoggerLevel.INFO, "[I]: ${tag.uppercase()} ~> $message")
        }

        fun d(tag: String, message: String) {
            out(TDLoggerLevel.DEBUG, "[D]: ${tag.uppercase()} ~> $message")
        }

        fun w(tag: String, message: String) {
            out(TDLoggerLevel.WARN, "[W]: ${tag.uppercase()} ~> $message")
        }

        fun e(tag: String, message: String) {
            out(TDLoggerLevel.ERROR, "[E]: ${tag.uppercase()} ~> $message")
        }

        fun a(tag: String, message: String) {
            out(TDLoggerLevel.ASSERT, "[A]: ${tag.uppercase()} ~> $message")
        }

        fun out(level: TDLoggerLevel, text: String) {
            when (LEVEL) {
                TDLoggerLevel.VERBOSE -> println(text)
                TDLoggerLevel.INFO -> if (level != TDLoggerLevel.VERBOSE) println(text)
                TDLoggerLevel.DEBUG -> if (level != TDLoggerLevel.VERBOSE && level != TDLoggerLevel.INFO) println(text)
                TDLoggerLevel.WARN -> if (level != TDLoggerLevel.VERBOSE && level != TDLoggerLevel.INFO && level != TDLoggerLevel.DEBUG) println(text)
                TDLoggerLevel.ERROR -> if (level == TDLoggerLevel.ERROR || level == TDLoggerLevel.ASSERT) println(text)
                TDLoggerLevel.ASSERT -> if (level == TDLoggerLevel.ASSERT) println(text)
            }
        }
    }
}

enum class TDLoggerLevel {
    VERBOSE,
    INFO,
    DEBUG,
    WARN,
    ERROR,
    ASSERT
}