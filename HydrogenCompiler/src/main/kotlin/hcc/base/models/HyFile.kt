package hcc.base.models

import hcc.base.logging.TDLogger
import java.io.DataOutput
import java.io.File


/**
 *     "HyFile" : {
 *       "Name": "File Name",
 *       "Path": "File",
 *       "Lines": "List(String)",
 *       "Files": "List(HyFile)"
 *     }
 */
data class HyFile(val name: String, val path: File, val lines: List<String>, val files: List<HyFile>, val localPath: String, val output: MutableList<String> = mutableListOf(), var outputFile: File = File(""), val outputChildren: MutableList<HyFile> = mutableListOf()) {
    companion object {
        const val HYFILE_TAG = "HyFile"
    }

    fun isDirectory(): Boolean = files.isNotEmpty()

    fun nameWithoutExtension(): String = name.split(".").first()

    fun functionName(): String = ".${localPath.replace("\\", "/")}${nameWithoutExtension()}"

    /**
     * Returns a pair of hashmaps for the parameters, vars are first, defs are second
     */
    fun getParameters(): Pair<HashMap<String, String>, HashMap<String, String>> {
        val line = lines.first()
        val tokens = line.substring(1 until line.length - 1).split(",").map { it.trim() }

        val vars = HashMap<String, String>()
        val defs = HashMap<String, String>()

        tokens.forEach {
            if (it.isEmpty()) return@forEach
            val keyword = it.split(" ").first()
            val name = it.split(" ").last()

            val fileName = this.name.replace(".tdfunction", "")
            val internalVarName = "${localPath.replace("\\", "_")}${fileName}_p_${name}"

            when (keyword) {
                "var" -> vars["_$name"] = internalVarName
                "def" -> defs["_$name"] = internalVarName
                else -> throw Exception("Unknown keyword for parameter in file $fileName")
            }
        }

        return Pair(vars, defs)
    }

    fun getParamCommandHalf(paramIndex: Int): String {
        val line = lines.first()
        val tokens = line.substring(1 until line.length - 1).split(",").map { it.trim() }

        val t = tokens[paramIndex]

        val internalName = if (t.startsWith("var")) {
            getParameters().first["_${t.split(" ").last()}"]
        } else {
            getParameters().second["_${t.split(" ").last()}"]
        }

        return if (t.startsWith("def")) "data modify storage minecraft:$internalName data set " else "execute store result score @a $internalName run "
    }

    fun getParamInternalName(paramIndex: Int): String {
        val line = lines.first()
        val tokens = line.substring(1 until line.length - 1).split(",").map { it.trim() }

        val t = tokens[paramIndex]

        val internalName = if (t.startsWith("var")) {
            getParameters().first["_${t.split(" ").last()}"]
        } else {
            getParameters().second["_${t.split(" ").last()}"]
        }

        return internalName ?: ""
    }

    fun printRecursive() {
        TDLogger.a(HYFILE_TAG, "${path.canonicalPath} ~> $name")

        files.forEach {
            it.printRecursive()
        }
    }
}