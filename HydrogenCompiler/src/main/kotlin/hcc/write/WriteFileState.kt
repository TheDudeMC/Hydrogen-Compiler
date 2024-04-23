package hcc.write

import hcc.base.models.HyFile
import hcc.state.State
import hcc.state.StateModel

class WriteFileState(private val indexRoot: HyFile?): State<WriteFileStateModel>() {
    override fun isApplicable(): Boolean = true

    override fun output(): WriteFileStateModel = WriteFileStateModel()

    override fun execute() {
        val indexRoot = indexRoot ?: return

        genFile(indexRoot)
    }

    fun genFile(root: HyFile) {
        root.files.forEach {
            genFile(it)
        }

        root.outputChildren.forEach {
            genFile(it)
        }

        root.output.forEach {
            root.outputFile.appendText("$it\n")
        }
    }
}

class WriteFileStateModel(): StateModel("Write File State")