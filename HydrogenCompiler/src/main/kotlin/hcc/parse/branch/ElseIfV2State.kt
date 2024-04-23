package hcc.parse.branch

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.GenState
import hcc.parse.base.FileCompilationModel
import hcc.parse.base.MathEvaluatorState
import hcc.parse.base.ParseConstants
import java.io.File

class ElseIfV2State(line: String, rootFile: HyFile, compilationModel: FileCompilationModel, private val elseIfIndex: Int = 0): IfV2State(line, rootFile, compilationModel) {

    override fun isApplicable(): Boolean = line.startsWith("} else if")

    override fun output(): BaseBranchV2StateModel = BaseBranchV2StateModel(lines, currentIndex)

    override fun getExpression(line: String): String = line.removePrefix("} else if").removeSuffix("{").trim()

    override fun getIfIndex(): Int = compilationModel.ifIndex - 1

    override fun getFileName(): String  = getGeneratedElseIfFileName(getIfIndex(), elseIfIndex).replace(".tdfunction", "")

    override fun generateMCIfCommand(line: String, hasElse: Boolean, hasElseIf: Boolean) {
        super.generateMCIfCommand(line, hasElse, hasElseIf)
        compilationModel.ifIndex--
    }

    override fun generateIfCommand(line: String, operator: String, hasElse: Boolean, hasElseIf: Boolean) {
        super.generateIfCommand(line, operator, hasElse, hasElseIf)
        compilationModel.ifIndex--
    }

    override fun generateElseIf(line: String) {
        generateElseIfHelper(line, elseIfIndex + 1)
    }

    override fun fetchElseIfIndex(): Int = elseIfIndex + 1
}