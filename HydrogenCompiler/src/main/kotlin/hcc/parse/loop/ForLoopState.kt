package hcc.parse.loop

import hcc.base.models.HyFile
import hcc.parse.GenState
import hcc.parse.base.FileCompilationModel
import hcc.parse.base.MathEvaluatorState
import hcc.parse.branch.*
import java.io.File

class ForLoopState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseBranchV2State(line, rootFile, compilationModel) {
    override fun isApplicable(): Boolean = line.startsWith("for")

    override fun output(): BaseBranchV2StateModel = BaseBranchV2StateModel(lines, currentIndex)

    override fun getFileName(): String  = getGeneratedForFileName(getIfIndex()).replace(".tdfunction", "")

    override fun getIfIndex(): Int = compilationModel.forIndex

    override fun execute() {
        super.execute()

        val ifBlock = rootFile.lines.subList(index + 1, currentIndex)
        generateIfBlock(ifBlock)

        val expression = line.trim().removePrefix("for").removeSuffix("{").trim()
        generateForCommand(expression)

        compilationModel.forIndex++
    }

    private fun generateForCommand(expression: String) {
        val generatedFileName = getFileName()
        val generatedFilePath = "${rootFile.localPath}generated"
        val generatedMCFunction = ".$generatedFilePath\\${generatedFileName}".replace("\\", "/")

        val loopMathState = MathEvaluatorState("reg_for_loop", expression, rootFile, compilationModel)
        loopMathState.executeState()

        lines.addAll(loopMathState.output().outputLines)
        lines.add("data modify storage minecraft:for_loop_storage data.previous set from storage for_loop_storage data")
        lines.add("execute store result storage minecraft:for_loop_storage data.value int 1 run scoreboard players get @p reg_for_loop")
        lines.add("function minecraft:$generatedMCFunction")
        lines.add("data modify storage minecraft:for_loop_storage data set from storage for_loop_storage data.previous")

        val generatedLines = mutableListOf("()")
        generatedLines.add("execute store result score @a reg_for_loop run data get storage for_loop_storage data.value")
        generatedLines.add("execute if score @p reg_for_loop > internal reg_zero run function minecraft:${generatedMCFunction}_block")
        generatedLines.add("execute store result score @a reg_for_loop run data get storage for_loop_storage data.value")
        generatedLines.add("scoreboard players remove @a reg_for_loop 1")
        generatedLines.add("execute store result storage for_loop_storage data.value int 1 run scoreboard players get @p reg_for_loop")
        generatedLines.add("execute if score @p reg_for_loop > internal reg_zero run function minecraft:$generatedMCFunction")

        val generatedForFile = HyFile(generatedFileName, File("$generatedFilePath\\$generatedFileName.tdfunction"), generatedLines, emptyList(), "${rootFile.localPath}generated\\")
        rootFile.outputChildren.add(generatedForFile)

        val genState = GenState(generatedForFile, compilationModel.projectPath, compilationModel.indexMap)
        genState.fileCompilationModel = compilationModel.clone()

        if (genState.isApplicable()) {
            genState.executeState()
        }
    }
}
