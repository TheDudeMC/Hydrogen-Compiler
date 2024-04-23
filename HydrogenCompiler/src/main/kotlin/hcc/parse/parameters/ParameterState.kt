package hcc.parse.parameters

import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel

class ParameterState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<ParameterStateModel>(line, rootFile, compilationModel) {
    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = line.startsWith("(") && line.endsWith(")")

    override fun output(): ParameterStateModel = ParameterStateModel(lines)

    override fun execute() {
        val pair = rootFile.getParameters()
        val vars = pair.first
        val defs = pair.second

        vars.keys.forEach {
            val v = vars[it] ?: return@forEach
            compilationModel.parameters.vars[it] = v
        }

        defs.keys.forEach {
            val d = defs[it] ?: return@forEach
            compilationModel.parameters.defs[it] = d
        }
    }

}

class ParameterStateModel(lines: List<String>): BaseParseStateModel(lines)