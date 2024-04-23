import hcc.RootManager
import hcc.base.logging.TDLogger

fun main(args: Array<String>) {
    TDLogger.a("Main", "Hydrogen Compiler Started (${args.contentToString()}")
    TDLogger.a("Main", "Logger level set to ${TDLogger.LEVEL}")

    val manager = RootManager(args.first())
    manager.activate()
}
