package hcc.parse.base

import hcc.base.models.HyFile
import hcc.parse.`var`.VarState
import hcc.state.State
import hcc.state.StateModel

abstract class BaseParseState<T: BaseParseStateModel>(var line: String, val rootFile: HyFile, val compilationModel: FileCompilationModel, var index: Int = -1): State<T>() {

    protected fun isDef(token: String): Boolean = compilationModel.defs.containsKey(token.split(".").first()) || compilationModel.parameters.defs.containsKey(token.split(".").first())

    protected fun getDefPath(token: String): String = token.replaceFirst(token.split(".").first(), "")

    protected fun getDefName(token: String): String = compilationModel.defs[token.split(".").first()] ?: compilationModel.parameters.defs[token.split(".").first()] ?: throw Exception("TODO: Handle crash")

    protected fun isOnlyDef(token: String): Boolean {
        MathEvaluatorState.operators.forEach {
            if (token.contains(it)) return false
        }

        return isDef(token)
    }
}

open class BaseParseStateModel(val outputLines: List<String>, val endLine: Int = -1): StateModel("Base Parse State")

data class FileCompilationModel(
    val projectPath: String,
    val defs: HashMap<String, String> = hashMapOf(),
    val vars: HashMap<String, String> = hashMapOf(),
    val consts: HashMap<String, String> = hashMapOf(),
    val parameters: ParametersModel = ParametersModel(),
    val indexMap: HashMap<String, HyFile> = HashMap(),
    var scale: Double = 1.0,
    var ifIndex: Int = 0,
    var forIndex: Int = 0,
    var whileIndex: Int = 0,
    var scopeIndex: Int = 0,
) {
    fun clone(): FileCompilationModel = FileCompilationModel(
        projectPath = projectPath,
        defs = defs,
        vars = vars,
        parameters = parameters,
        indexMap = indexMap,
    )
}

data class ParametersModel(val defs: HashMap<String, String> = hashMapOf(), val vars: HashMap<String, String> = hashMapOf())
