package hcc.parse.returning

import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel

class ReturnState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<ReturnStateModel>(line, rootFile, compilationModel) {
    companion object {
        fun generateReturn(line: String, compilationModel: FileCompilationModel): String {
            val def = line.split(".").first()

            if (compilationModel.defs.containsKey(def)) {
                return returnDef(line, compilationModel)
            } else if (compilationModel.vars.containsKey(line)) {
                return returnVar(line, compilationModel)
            } else if (line.startsWith("scoreboard")) {
                return "execute store result storage minecraft:return data int 1 run $line"
            } else if (line.startsWith("data")) {
                val storage = line.split(" ").last()
                return "data modify storage minecraft:return data set from storage $storage"
            }

            throw Exception("Condition not handled yet ($line)")
        }

        private fun returnVar(statement: String, compilationModel: FileCompilationModel): String =
            "execute store result storage minecraft:return data int 1 run scoreboard players get @p ${compilationModel.vars[statement]}"

        private fun returnDef(statement: String, compilationModel: FileCompilationModel): String {
            val def = statement.split(".").first()
            val path = statement.removePrefix(def)
            return "data modify storage return data set from storage ${compilationModel.defs[def]} data${path}"
        }
    }

    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = line.startsWith("return")

    override fun output(): ReturnStateModel = ReturnStateModel(lines)

    override fun execute() {
        val returnStatement = line.removePrefix("return ").trim()
        lines.add(generateReturn(returnStatement, compilationModel))
    }

    private fun returnVar(statement: String) {
        lines.add("execute store result storage minecraft:return data int 1 run scoreboard players get @p ${compilationModel.vars[statement]}")
    }

    private fun returnDef(statement: String) {
        val def = statement.split(".").first()
        val path = statement.removePrefix(def)
        lines.add("data modify storage return data set from storage ${compilationModel.defs[def]} data${path}")
    }
}

class ReturnStateModel(lines: List<String>): BaseParseStateModel(lines)