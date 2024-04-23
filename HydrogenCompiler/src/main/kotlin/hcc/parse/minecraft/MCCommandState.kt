package hcc.parse.minecraft

import hcc.base.models.HyFile
import hcc.parse.base.*

class MCCommandState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<MCCommandStateModel>(line, rootFile, compilationModel) {
    companion object {
        fun isMCCommand(line: String): Boolean {
            ParseConstants.MC_COMMANDS.forEach {
                if (line.startsWith("$it ")) return true
            }

            return false
        }
    }

    val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = isMCCommand(line)

    override fun output(): MCCommandStateModel = MCCommandStateModel(lines)

    override fun execute() {
        lines.clear()
        val saveDefState = SaveDefCommandState(line, rootFile, compilationModel)

        if (saveDefState.isApplicable()) {
            saveDefState.executeState()
            lines.addAll(saveDefState.output().outputLines)
        } else {
            lines.add(line)
        }
    }
}

class MCCommandStateModel(outputLines: List<String>): BaseParseStateModel(outputLines)