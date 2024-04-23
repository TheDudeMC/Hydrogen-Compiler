package hcc.parse.def

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.base.*
import hcc.parse.minecraft.MCCommandState
import hcc.parse.returning.ReturnState

//TODO: Handle static defs
class DefState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<DefStateModel>(line, rootFile, compilationModel) {
    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = doesLineStartWithDef()

    override fun output(): DefStateModel = DefStateModel(lines)

    override fun execute() {
        lines.clear()

        if (line.startsWith("def") || line.startsWith("static def")) {
            executeInitialization()
        } else {
            executeUpdate()
        }
    }

    private fun executeUpdate() {
        val internalVarName = evaluateLHS()

        val rhs = getRHS(line)

        if (rhs.toDoubleOrNull() != null || rhs.startsWith("{")){
            val generatedLine = "data modify storage minecraft:$internalVarName set value $rhs"
            lines.add(generatedLine)
            return
        }

        val mathState = MathEvaluatorState(internalVarName, rhs, rootFile, compilationModel, MathDestinationType.DEF)
        mathState.executeState()

        lines.addAll(mathState.output().outputLines)
    }

    private fun executeInitialization() {
        val line = if (line.startsWith("static")) line.substring(6) else line

        val lineComponents = line.split(" ")
        val fileName = rootFile.name.replace(".tdfunction", "")
        val internalVarName = "${rootFile.localPath.replace("\\", "_")}${fileName}_${lineComponents[1]}"

        compilationModel.defs[lineComponents[1]] = internalVarName

        val rhs = getRHS(line)

        if (rhs.toDoubleOrNull() != null || rhs.startsWith("{")){
            val generatedLine = "data modify storage minecraft:$internalVarName data set value $rhs"
            lines.add(generatedLine)
            return
        } else if (isDef(rhs)) {
            //data modify storage test data set from storage source data
            val generatedLine = "data modify storage minecraft:$internalVarName data set from storage minecraft:${getDefName(rhs)} data${getDefPath(rhs)}"
            lines.add(generatedLine)
            return
        } else if (MCCommandState.isMCCommand(rhs)) {
            lines.add(ReturnState.generateReturn(rhs, compilationModel))
            lines.add("data modify storage minecraft:$internalVarName data set from storage minecraft:return data")
            return
        }

//        else if (rhs.startsWith("entity") || rhs.startsWith("block") || rhs.startsWith("storage")) {
//            val generatedLine = "data modify storage minecraft:$internalVarName data set from $rhs"
//            lines.add(generatedLine)
//            return
//        }

        val mathState = MathEvaluatorState(internalVarName, rhs, rootFile, compilationModel, MathDestinationType.DEF)
        mathState.executeState()

        lines.addAll(mathState.output().outputLines)
    }

    private fun getRHS(line: String): String {
        var l = line
        while (!l.startsWith("=")) {
            if (l.isEmpty()) throw Exception("Error incorrect def syntax, line: $line")
            l = l.substring(1)
        }

        return l.substring(1).trim()
    }

    private fun evaluateLHS(): String {
        val lhs = line.split("=").first().trim()
        val pathBuilder = StringBuilder("data")

        val defName = if (lhs.contains(".")) {
            val words = lhs.split(".")

            words.subList(1, words.size).forEach {
                pathBuilder.append(".$it")
            }

            compilationModel.defs[words.first()] ?: "ERROR: Could not find Def name for line: $line"
        } else {
            compilationModel.defs[lhs] ?: "ERROR: Could not find Def name for line: $line"
        }

        return "$defName $pathBuilder"
    }

    private fun doesLineStartWithDef(): Boolean {
        if (line.startsWith("def")) return true
        if (line.isEmpty() || !line.contains(" ")) return false

        val firstComponent = line.split(" ").first()

        if (firstComponent.contains(".")) {
            val firstWord = firstComponent.split(".").first()
            return compilationModel.defs.containsKey(firstWord)
        }

        return compilationModel.defs.containsKey(firstComponent)
    }
}

class DefStateModel(lines: List<String>): BaseParseStateModel(lines)