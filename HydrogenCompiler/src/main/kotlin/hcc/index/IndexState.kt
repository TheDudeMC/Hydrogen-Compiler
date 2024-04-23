package hcc.index

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.state.ManageableState
import hcc.state.State
import hcc.state.StateModel

class IndexState(private val projectPath: String): ManageableState<IndexStateModel>() {

    private var indexRoot: HyFile? = null
    private val indexMap = HashMap<String, HyFile>()

    override fun isApplicable(): Boolean = true

    override fun execute() {

        val fileParseState = FileParseState(projectPath)
        fileParseState.executeState()
        indexRoot = fileParseState.output().rootFile

        generateMap(indexRoot!!)
    }

    private fun generateMap(hyFile: HyFile) {
        if (hyFile.isDirectory()) {
            hyFile.files.forEach {
                generateMap(it)
            }
            return
        }

        val path = hyFile.functionName()
        indexMap[path] = hyFile
    }

    override fun output(): IndexStateModel = IndexStateModel(indexRoot, projectPath, indexMap)

    override fun loadLazy(): State<StateModel> {
        return FileParseState(projectPath)
    }
}

class IndexStateModel(val indexRoot: HyFile?, val projectPath: String, val indexMap: HashMap<String, HyFile>): StateModel("Index")