package hcc.parse.branch

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.GenState
import hcc.parse.base.*
import java.io.File

open class IfV2State(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseBranchV2State(line, rootFile, compilationModel) {

    override fun isApplicable(): Boolean = line.startsWith("if")

    override fun output(): BaseBranchV2StateModel = BaseBranchV2StateModel(lines, currentIndex)

    override fun execute() {
        super.execute()

        val ifIndex = this.compilationModel.ifIndex
        val ifBlock = rootFile.lines.subList(index + 1, currentIndex)
        generateIfBlock(ifBlock)

        val expression = getExpression(line)
        val endLine = rootFile.lines[currentIndex]
        this.compilationModel.ifIndex = ifIndex

        var isOperation = false
        ParseConstants.IF_STATEMENT_OPERATORS.forEach {
            if (expression.contains(it)) {
                generateIfCommand(expression, it, hasElse(endLine), hasElseIf(endLine))
                isOperation = true
            }
        }

        if (!isOperation) {
            generateMCIfCommand(expression, hasElse(endLine), hasElseIf(endLine))
        }

        generateElseIf(endLine)
        generateElse(endLine)
    }


    protected open fun getExpression(line: String): String = line.removePrefix("if").removeSuffix("{").trim()


    open fun generateElseIf(line: String) {
        generateElseIfHelper(line, 0)
    }

    protected fun generateElseIfHelper(line: String, elseIfIndex: Int) {
        val elseIfState = ElseIfV2State(line.trim(), rootFile, compilationModel, elseIfIndex)
        elseIfState.index = currentIndex
        if (elseIfState.isApplicable()) {
            elseIfState.executeState()
            val output = elseIfState.output()
            currentIndex = output.endLine
        }
    }

    private fun generateElse(line: String) {
        val elseState = ElseV2State(line.trim(), rootFile, compilationModel)
        elseState.index = currentIndex
        if (elseState.isApplicable()) {
            elseState.executeState()
            val output = elseState.output()
            currentIndex = output.endLine
        }
    }

    protected fun hasElse(line: String): Boolean = ElseV2State(line.trim(), rootFile, compilationModel).isApplicable()

    protected fun hasElseIf(line: String): Boolean = ElseIfV2State(line.trim(), rootFile, compilationModel).isApplicable()

    open fun fetchElseIfIndex(): Int = 0

    open fun generateMCIfCommand(line: String, hasElse: Boolean, hasElseIf: Boolean) {
        val generatedFileName = getFileName()
        val generatedElseFileName = getGeneratedElseFileName(getIfIndex()).replace(".tdfunction", "")
        val generatedElseIfFileName = getGeneratedElseIfFileName(getIfIndex(), fetchElseIfIndex()).replace(".tdfunction", "")
        val generatedFilePath = "${rootFile.localPath}generated"
        val generatedMCFunction = ".$generatedFilePath\\${generatedFileName}".replace("\\", "/")
        val generatedMCElseFunction = ".$generatedFilePath\\${generatedElseFileName}".replace("\\", "/")
        val generatedMCElseIfFunction = ".$generatedFilePath\\${generatedElseIfFileName}".replace("\\", "/")

        val generatedLines = mutableListOf("()")
        generatedLines.add("execute if $line run function minecraft:${generatedMCFunction}_block")
        if (hasElse) generatedLines.add("execute unless $line run function minecraft:${generatedMCElseFunction}_block")
        if (hasElseIf) generatedLines.add("execute unless $line run function minecraft:${generatedMCElseIfFunction}")

        lines.add("function minecraft:$generatedMCFunction")

        val generatedIfFile = HyFile(generatedFileName, File("$generatedFilePath\\$generatedFileName.tdfunction"), generatedLines, emptyList(), "${rootFile.localPath}generated\\")
        rootFile.outputChildren.add(generatedIfFile)

        val genState = GenState(generatedIfFile, compilationModel.projectPath, compilationModel.indexMap)
        genState.fileCompilationModel = compilationModel.clone()

        if (genState.isApplicable()) {
            genState.executeState()
        }

        compilationModel.ifIndex++
    }

    open fun generateIfCommand(line: String, operator: String, hasElse: Boolean, hasElseIf: Boolean) {
        val generatedFileName = getFileName()
        val generatedElseFileName = getGeneratedElseFileName(getIfIndex()).replace(".tdfunction", "")
        val generatedElseIfFileName = getGeneratedElseIfFileName(getIfIndex(), fetchElseIfIndex()).replace(".tdfunction", "")
        val generatedFilePath = "${rootFile.localPath}generated"
        val generatedMCFunction = ".$generatedFilePath\\${generatedFileName}".replace("\\", "/")
        val generatedMCElseFunction = ".$generatedFilePath\\${generatedElseFileName}".replace("\\", "/")
        val generatedMCElseIfFunction = ".$generatedFilePath\\${generatedElseIfFileName}".replace("\\", "/")

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
        generatedLines.add("execute if score @p reg_if_lhs $mcOperator @p reg_if_rhs run function minecraft:${generatedMCFunction}_block")
        if (hasElse) generatedLines.add("execute unless score @p reg_if_lhs $mcOperator @p reg_if_rhs run function minecraft:${generatedMCElseFunction}_block")
        if (hasElseIf) generatedLines.add("execute unless score @p reg_if_lhs $mcOperator @p reg_if_rhs run function minecraft:${generatedMCElseIfFunction}")

        lines.add("function minecraft:$generatedMCFunction")

        val generatedIfFile = HyFile(generatedFileName, File("$generatedFilePath\\$generatedFileName.tdfunction"), generatedLines, emptyList(), "${rootFile.localPath}generated\\")
        rootFile.outputChildren.add(generatedIfFile)

        val genState = GenState(generatedIfFile, compilationModel.projectPath, compilationModel.indexMap)
        genState.fileCompilationModel = compilationModel.clone()

        if (genState.isApplicable()) {
            genState.executeState()
        }

        compilationModel.ifIndex++
    }
}
