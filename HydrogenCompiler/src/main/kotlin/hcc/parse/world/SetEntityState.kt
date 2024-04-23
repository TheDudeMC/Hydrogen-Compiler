package hcc.parse.world

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel

class SetEntityState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<SetEntityStateModel>(line, rootFile, compilationModel) {
    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = line.lowercase().startsWith("setentity")

    override fun output(): SetEntityStateModel = SetEntityStateModel(lines)

    override fun execute() {
        val tokens = line.split(" ")
//        tokens.forEach { TDLogger.a("#set entity", it) }

        val entity = tokens[1]
        val path = tokens[2]
        val type = tokens[3]
        val scale = tokens[4]
        val refType = tokens[5]
        val ref = tokens[6]

        genCommands(entity, path, type, refType, ref, scale)
    }

    private fun genCommands(entity: String, path: String, type: String, refType: String, ref: String, scale: String) {
        when(refType) {
            "def" -> {
                val defName = ref.split(".").first()
                val internalRef = compilationModel.defs[ref.split(".").first()]
                val internalPath = ref.replaceFirst("$defName.", "")

                lines.add("execute store result score @a reg_rhs run data get storage minecraft:$internalRef data.$internalPath")

                val line = "execute store result entity $entity $path $type $scale run scoreboard players get @p reg_rhs"
                lines.add(line)
            }
        }
    }
}

class SetEntityStateModel(lines: List<String>): BaseParseStateModel(lines)