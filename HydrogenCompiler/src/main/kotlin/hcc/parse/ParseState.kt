package hcc.parse

import hcc.base.models.HyFile
import hcc.state.ManageableState
import hcc.state.StateModel

class ParseState(private val indexRoot: HyFile?, private val projectPath: String, private val indexMap: HashMap<String, HyFile>): ManageableState<ParseStateModel>() {

    override fun isApplicable(): Boolean = indexRoot != null

    override fun execute() {
        val indexRoot = indexRoot ?: return

        val directoryState = DirectoryState(indexRoot, "$projectPath\\.")
        if (directoryState.isApplicable()) {
            directoryState.executeState()
        }

        val fileGenState = GenState(indexRoot, "$projectPath\\.", indexMap)
        if (fileGenState.isApplicable()) {
            fileGenState.executeState()
        }
    }

    override fun output(): ParseStateModel = ParseStateModel(indexRoot)
}

class ParseStateModel(val indexRoot: HyFile?): StateModel("Parse State")