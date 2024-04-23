package hcc.parse.loop

import hcc.base.models.HyFile
import hcc.parse.GenState
import hcc.parse.base.FileCompilationModel
import hcc.parse.base.MathEvaluatorState
import hcc.parse.base.ParseConstants
import hcc.parse.branch.BaseBranchV2State
import hcc.parse.branch.BaseBranchV2StateModel
import java.io.File

class WhileLoopState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseBranchV2State(line, rootFile, compilationModel) {
    override fun isApplicable(): Boolean = line.startsWith("while")

    override fun output(): BaseBranchV2StateModel = BaseBranchV2StateModel(lines, currentIndex)

    override fun getFileName(): String  = getGeneratedWhileFileName(getIfIndex()).replace(".tdfunction", "")

    override fun getIfIndex(): Int = compilationModel.whileIndex

    override fun execute() {
        super.execute()

        val ifBlock = rootFile.lines.subList(index + 1, currentIndex)

        val generatedFileName = getFileName()
        val generatedFilePath = "${rootFile.localPath}generated"
        val generatedMCFunction = ".$generatedFilePath\\${generatedFileName}".replace("\\", "/")
        val mutableIfBlock = mutableListOf<String>()
        mutableIfBlock.addAll(ifBlock)
        mutableIfBlock.add("call $generatedMCFunction")

        generateIfBlock(mutableIfBlock)

        val expression = line.trim().removePrefix("while").removeSuffix("{").trim()
        ParseConstants.IF_STATEMENT_OPERATORS.forEach {
            if (expression.contains(it)) {
                generateWhileCommand(expression, it)
            }
        }

        compilationModel.whileIndex++
    }

    private fun generateWhileCommand(line: String, operator: String) {
        val generatedFileName = getFileName()
        val generatedFilePath = "${rootFile.localPath}generated"
        val generatedMCFunction = ".$generatedFilePath\\${generatedFileName}".replace("\\", "/")

        val equationSides = line.split(operator)
        val lhs = equationSides.first().trim()
        val rhs = equationSides.last().trim()
        val mcOperator = operator.replace("==", "=")

        val lhsMathState = MathEvaluatorState("reg_if_lhs", lhs, rootFile, compilationModel)
        val rhsMathState = MathEvaluatorState("reg_if_rhs", rhs, rootFile, compilationModel)
        rhsMathState.executeState()
        lhsMathState.executeState()

        val generatedLines = mutableListOf("()")
        generatedLines.addAll(lhsMathState.output().outputLines)
        generatedLines.addAll(rhsMathState.output().outputLines)
        generatedLines.add("execute unless data storage while_loop_storage data.break if score @p reg_if_lhs $mcOperator @p reg_if_rhs run function minecraft:${generatedMCFunction}_block")
//        generatedLines.add("execute unless data storage while_loop_storage data.break if score @p reg_if_lhs $mcOperator @p reg_if_rhs run function minecraft:$generatedMCFunction")

        lines.add("data modify storage minecraft:while_loop_storage data.previous set from storage while_loop_storage data")
        lines.add("function minecraft:$generatedMCFunction")
        lines.add("data modify storage minecraft:while_loop_storage data set from storage while_loop_storage data.previous")

        val generatedForFile = HyFile(generatedFileName, File("$generatedFilePath\\$generatedFileName.tdfunction"), generatedLines, emptyList(), "${rootFile.localPath}generated\\")
        rootFile.outputChildren.add(generatedForFile)

        val genState = GenState(generatedForFile, compilationModel.projectPath, compilationModel.indexMap)
        genState.fileCompilationModel = compilationModel.clone()

        if (genState.isApplicable()) {
            genState.executeState()
        }
    }
}