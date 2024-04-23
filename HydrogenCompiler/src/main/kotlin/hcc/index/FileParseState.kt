package hcc.index

import hcc.base.models.HyFile
import hcc.state.State
import hcc.state.StateModel
import java.io.File

class FileParseState(private val projectPath: String): State<FileParseStateModel>() {

    lateinit var indexedFiles: HyFile

    override fun isApplicable(): Boolean = true

    override fun execute() {
        indexedFiles = getFilesRecursive(file = File(projectPath), "")
    }

    override fun output(): FileParseStateModel = FileParseStateModel(indexedFiles)

    private fun getFilesRecursive(file: File, localPath: String): HyFile {
        if (!file.isDirectory) {

            val fileLines = mutableListOf<String>()

            file.forEachLine {
                fileLines.add(it)
            }

            return HyFile(file.name, file, fileLines, emptyList(), localPath)
        }

        val childrenFiles = mutableListOf<HyFile>()

        file.listFiles()?.forEach {

            if (it.name != ".code") childrenFiles.add(getFilesRecursive(it, "$localPath${file.name}\\"))
            else it.deleteRecursively()
        }

        return HyFile(file.name, file, emptyList(), childrenFiles, localPath)
    }
}

class FileParseStateModel(val rootFile: HyFile): StateModel("File Parse State")