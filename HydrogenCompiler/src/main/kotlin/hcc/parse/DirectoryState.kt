package hcc.parse

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.state.State
import hcc.state.StateModel
import java.io.File

class DirectoryState(private val file: HyFile, private val projectPath: String): State<DirectoryStateModel>() {
    companion object {
        const val DIRECTORY_STATE_TAG = "DIRECTORY"
    }

    override fun isApplicable(): Boolean = file.isDirectory()

    override fun output(): DirectoryStateModel = DirectoryStateModel()

    override fun execute() {
        if (file.isDirectory()) {

            TDLogger.a(DIRECTORY_STATE_TAG, "Generating Path... $projectPath${file.localPath}${file.name}")

            val dir = File("$projectPath${file.localPath}${file.name}")
            dir.mkdir()

            file.files.forEach {
                val directoryState = DirectoryState(it, projectPath)

                if (directoryState.isApplicable()) {
                    directoryState.executeState()
                }
            }
        }
    }

}

class DirectoryStateModel: StateModel("Directory State")