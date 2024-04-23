package hcc.parse.world

import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel

class SetScoreState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<SetScoreV2StateModel>(line, rootFile, compilationModel) {
    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = line.startsWith("setscore")

    override fun output(): SetScoreV2StateModel = SetScoreV2StateModel(lines)

    override fun execute() {
        val tokens = line.split(" ")

        val player = tokens[1]
        val objective = tokens[2]
        val refType = tokens[3]
        val ref = tokens[4]

        genScoreCommand(player, objective, refType, ref)
    }

    private fun genScoreCommand(player: String, objective: String, refType: String, ref: String) {
        when (refType) {
            "def" -> {
                val defName = ref.split(".").first()
                val internalRef = compilationModel.defs[ref.split(".").first()]
                val internalPath = if (ref.contains(".")) ref.replaceFirst(defName, "") else ""
                lines.add("execute store result score $player $objective run data get storage minecraft:$internalRef data$internalPath")
            }

            "var" -> {
                val internalRef = compilationModel.vars[ref]
                lines.add("execute store result score $player $objective run scoreboard players get @p $internalRef")
            }
        }
    }
}

class SetScoreV2StateModel(lines: List<String>): BaseParseStateModel(lines)