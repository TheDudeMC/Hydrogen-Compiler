package hcc.parse.branch

import hcc.base.models.HyFile
import hcc.parse.GenState
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel
import java.io.File

abstract class BaseBranchV2State(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<BaseBranchV2StateModel>(line, rootFile, compilationModel) {
    protected val lines = mutableListOf<String>()
    protected var currentIndex = index + 1

    protected fun isLineBranching(line: String): Boolean =
        line.startsWith("if") ||
                line.startsWith("} else") ||
                line.startsWith("for") ||
                line.startsWith("scope") ||
                line.startsWith("while")

    protected fun getGeneratedFileName(ifIndex: Int): String = "${rootFile.nameWithoutExtension()}_if_${ifIndex}.tdfunction"

    protected fun getGeneratedElseFileName(ifIndex: Int): String = "${rootFile.nameWithoutExtension()}_else_${ifIndex}.tdfunction"

    protected fun getGeneratedForFileName(ifIndex: Int): String = "${rootFile.nameWithoutExtension()}_for_${ifIndex}.tdfunction"

    protected fun getGeneratedWhileFileName(ifIndex: Int): String = "${rootFile.nameWithoutExtension()}_while_${ifIndex}.tdfunction"

    protected fun getGeneratedScopeFileName(ifIndex: Int): String = "${rootFile.nameWithoutExtension()}_scope_${ifIndex}.tdfunction"

    protected fun getGeneratedElseIfFileName(ifIndex: Int, elseIfIndex: Int): String = "${rootFile.nameWithoutExtension()}_else_${elseIfIndex}_if_${ifIndex}.tdfunction"

    protected open fun getFileName(): String = getGeneratedFileName(getIfIndex()).replace(".tdfunction", "")

    protected open fun getIfIndex(): Int = compilationModel.ifIndex

    override fun execute() {
        lines.clear()

        var bracketCount = 1
        currentIndex = index + 1

        while (true) {
            val currentLine = rootFile.lines[currentIndex].trim()
            if (currentLine.startsWith("}")) bracketCount--
            if (bracketCount == 0) break
            if (isLineBranching(currentLine)) bracketCount++
            currentIndex++
            if (currentIndex == rootFile.lines.size) throw Exception("Error: Was unable to find bracket closure for line #$index ($line)")
        }
    }

    protected fun generateIfBlock(ifBlock: List<String>) {
        val generatedFileName = "${getFileName()}_block"
        val generatedFilePath = "${rootFile.localPath}generated"
        val block = mutableListOf("()")
        block.addAll(ifBlock)

        val generatedIfFile = HyFile(generatedFileName, File("$generatedFilePath\\${generatedFileName}.tdfunction"), block, emptyList(), "${rootFile.localPath}generated\\")
        rootFile.outputChildren.add(generatedIfFile)

        val genState = GenState(generatedIfFile, compilationModel.projectPath, compilationModel.indexMap)
        genState.fileCompilationModel = compilationModel.clone()

        if (genState.isApplicable()) {
            genState.executeState()
        }
    }
}

open class BaseBranchV2StateModel(lines: List<String>, endLine: Int): BaseParseStateModel(lines, endLine)