package hcc.parse.util

import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel

class ScaleState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<ScaleStateModel>(line, rootFile, compilationModel) {
    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = line.startsWith("scale")

    override fun output(): ScaleStateModel = ScaleStateModel(lines)

    override fun execute() {
        val scaleValue = line.split(" ").last().toDouble()
        compilationModel.scale = scaleValue
    }
}

class ScaleStateModel(lines: List<String>): BaseParseStateModel(lines)
