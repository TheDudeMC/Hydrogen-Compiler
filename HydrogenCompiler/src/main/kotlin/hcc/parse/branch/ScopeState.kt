package hcc.parse.branch

import hcc.base.models.HyFile
import hcc.parse.base.FileCompilationModel

class ScopeState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseBranchV2State(line, rootFile, compilationModel) {
    override fun isApplicable(): Boolean = line.startsWith("scope")

    override fun output(): BaseBranchV2StateModel = BaseBranchV2StateModel(lines, currentIndex)

    override fun getFileName(): String  = getGeneratedScopeFileName(getIfIndex()).replace(".tdfunction", "")

    override fun getIfIndex(): Int = compilationModel.scopeIndex

    override fun execute() {
        super.execute()

        val ifBlock = rootFile.lines.subList(index + 1, currentIndex)
        generateIfBlock(ifBlock)

        val expression = line.trim().removePrefix("scope").removeSuffix("{").trim()
        generateScopeCommand(expression)

        compilationModel.scopeIndex++
    }

    private fun generateScopeCommand(expression: String) {
        val generatedFileName = getFileName()
        val generatedFilePath = "${rootFile.localPath}generated"
        val generatedMCFunction = ".$generatedFilePath\\${generatedFileName}".replace("\\", "/")
        val tokens = expression.split(" ")

        lines.add("execute ${tokens.first()} ${tokens[1]} run function minecraft:${generatedMCFunction}_block")
    }
}