package hcc.parse.world

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel

class SetEntityV2State(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<SetEntityV2StateModel>(line, rootFile, compilationModel) {
    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = line.lowercase().startsWith("setentity")

    override fun output(): SetEntityV2StateModel = SetEntityV2StateModel(lines)

    override fun execute() {
        lines.clear()

        val tokens = line.split(" ")
//        tokens.forEach { TDLogger.a("#set entity", it) }

        val saveType = tokens[1]
        val entity = tokens[2]
        val path = tokens[3]

        if (saveType == "data") {
            val dataType = tokens[4]
            val refType = tokens[5]
            val ref = tokens[6]
            val scale = tokens[7]

            genDataCommand(entity, path, dataType, refType, ref, scale)
        } else {

            val type = tokens[4]
            val scale = tokens[5]
            val refType = tokens[6]
            val ref = tokens[7]

            genCommands(entity, path, type, refType, ref, scale)
        }

    }

    private fun genDataCommand(entity: String, path: String, dataType: String, refType: String, ref: String, scale: String) {
        when (refType) {
            "def" -> {
                val defName = ref.split(".").first()
                val internalRef = compilationModel.defs[ref.split(".").first()]
                val internalPath = if (ref.contains(".")) ".${ref.replaceFirst("$defName.", "")}" else ""

                //execute as @e[tag=camera,limit=1] run execute store result entity @s Pos[2] double 0.01 run data get storage minecraft:code_scenes_intro_script01_m data.data.pos[2] 1
                lines.add("execute as $entity run execute store result entity @s $path $dataType $scale run data get storage minecraft:$internalRef data$internalPath 1")
//                lines.add("execute as $entity run data modify entity @s $path set from storage minecraft:$internalRef data$internalPath $scale")
            }
        }
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

class SetEntityV2StateModel(lines: List<String>): BaseParseStateModel(lines)