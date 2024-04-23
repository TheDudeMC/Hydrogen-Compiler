package hcc.parse.branch

import hcc.base.models.HyFile
import hcc.parse.GenState
import hcc.parse.base.BaseParseState
import hcc.parse.base.FileCompilationModel
import java.io.File

class ElseV2State(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseBranchV2State(line, rootFile, compilationModel) {

    override fun isApplicable(): Boolean = line.startsWith("} else {")

    override fun output(): BaseBranchV2StateModel = BaseBranchV2StateModel(lines, currentIndex)

    override fun getFileName(): String  = getGeneratedElseFileName(getIfIndex()).replace(".tdfunction", "")

    override fun getIfIndex(): Int = compilationModel.ifIndex - 1

    override fun execute() {
        super.execute()

        val ifBlock = rootFile.lines.subList(index + 1, currentIndex)
        generateIfBlock(ifBlock)
    }

}