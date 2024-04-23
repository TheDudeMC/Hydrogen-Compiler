package hcc.parse.call

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel

class CallState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<CallStateModel>(line, rootFile, compilationModel) {
    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean {
        if (line.startsWith("call")) return true

        if (line.startsWith("delay")) {
            val tokens = line.split(" ")
            if (tokens.size > 3 && tokens[2] == "call") return true
        }

        return false
    }

    override fun output(): CallStateModel = CallStateModel(lines)

    override fun execute() {
        lines.clear()
        if (line.startsWith("delay")) {
            generateDelay()
        } else {
            generateFunction()
        }
    }

    private fun generateFunction() {
        val function = line.replace("call ", "")
        val functionName = function.split("(").first()
        val parameters = function.split("(").last().replace(")", "").split(",").map { it.trim() }

        if (function.contains("(")) {
            parameters.forEachIndexed { index, param ->
                val file = compilationModel.indexMap[functionName] ?: return@forEachIndexed

                val expressionFirst = file.getParamCommandHalf(index)
                lines.add("$expressionFirst${getSecondHalf(param, file, index)}")
            }
        }

        lines.add("function minecraft:$functionName")
    }

    //schedule function minecraft:.code/dude 1t append
    private fun generateDelay() {
        val delay = line.split(" ")[1]
        val function = line.replace("delay $delay call ", "")
        val functionName = function.split("(").first()
        val parameters = function.split("(").last().replace(")", "").split(",").map { it.trim() }

        if (function.contains("(")) {
            parameters.forEachIndexed { index, param ->
                val file = compilationModel.indexMap[functionName] ?: return@forEachIndexed

                val expressionFirst = file.getParamCommandHalf(index)
                lines.add("$expressionFirst${getSecondHalf(param, file, index)}")
            }
        }

        lines.add("schedule function minecraft:$functionName $delay append")
    }

    private fun getSecondHalf(param: String, file: HyFile, index: Int): String {
        //scoreboard objectives add foo dummy "foo"
        val setVar =
            //scoreboard objectives add help dummy "help me plz"
            if (param.toIntOrNull() != null) {
                val paramName = file.getParamInternalName(index)
                lines.add("scoreboard objectives add $paramName dummy \"$paramName\"")
                "scoreboard players set @a $paramName $param"
            } else if (compilationModel.vars.containsKey(param)) {
            val v = compilationModel.vars[param] ?: throw Exception("Fuck you")
            val paramName = file.getParamInternalName(index)
            lines.add("scoreboard objectives add $paramName dummy \"$paramName\"")
            "scoreboard players get @p $v"
        } else if (compilationModel.parameters.vars.containsKey(param)) {
            val v = compilationModel.parameters.vars[param] ?: throw Exception("Fuck you")
            lines.add("scoreboard objectives add $v dummy\"$v\"")
            "scoreboard players get @p $v"
        } else if (isDef(param)) {
            val defName = getDefName(param)
            val defPath = getDefPath(param)
            "from storage minecraft:$defName data$defPath"
        } else throw Exception("Variable name not found: $param, line: $line")

        return setVar
    }
}

class CallStateModel(lines: List<String>): BaseParseStateModel(lines)