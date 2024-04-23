package hcc.state

import hcc.index.FileParseState
import hcc.index.FileParseStateModel
import hcc.index.IndexStateModel
import hcc.index.IndexState
import hcc.parse.ParseState
import hcc.parse.ParseStateModel
import hcc.write.WriteFileState
import hcc.write.WriteFileStateModel

class StateManager(var rootState: State<StateModel>) {

    private val stateEvaluator = StateEvaluator()

    fun execute() {
        if (rootState.isApplicable()) {
            rootState.executeState()
        }
    }

    fun evaluate() {
        if (rootState as? IndexState != null) rootState = stateEvaluator.evaluate((rootState as IndexState).output())
        else if (rootState as? LazyState != null) rootState = stateEvaluator.evaluate((rootState as LazyState).output())
        else if (rootState as? FileParseState != null) rootState = stateEvaluator.evaluate((rootState as FileParseState).output())
        else if (rootState as? ParseState != null) rootState = stateEvaluator.evaluate((rootState as ParseState).output())
        else if (rootState as? WriteFileState != null) rootState = stateEvaluator.evaluate((rootState as WriteFileState).output())
        else if (rootState == StateConstants.EMPTY_STATE) rootState = StateConstants.EMPTY_STATE
    }

}

class StateEvaluator {
    fun evaluate(model: LazyStateModel): State<StateModel> = model.lazy()
    fun evaluate(model: IndexStateModel): State<StateModel> = ParseState(model.indexRoot, model.projectPath, model.indexMap)
    fun evaluate(model: FileParseStateModel): State<StateModel> = StateConstants.EMPTY_STATE
    fun evaluate(model: ParseStateModel): State<StateModel> = WriteFileState(model.indexRoot)
    fun evaluate(model: WriteFileStateModel): State<StateModel> = StateConstants.EMPTY_STATE
}

class TestStateManager(var rootState: State<StateModel>) {

    private val stateEvaluator = TestStateEvaluator()

    fun execute() {
        if (rootState.isApplicable()) {
            rootState.executeState()
        }
    }

    fun evaluate() {
        if (rootState as? IndexState != null) rootState = stateEvaluator.evaluate((rootState as IndexState).output())
        else if (rootState as? LazyState != null) rootState = stateEvaluator.evaluate((rootState as LazyState).output())
        else if (rootState as? FileParseState != null) rootState = stateEvaluator.evaluate((rootState as FileParseState).output())
        else if (rootState as? ParseState != null) rootState = stateEvaluator.evaluate((rootState as ParseState).output())
        else if (rootState == StateConstants.EMPTY_STATE) rootState = StateConstants.EMPTY_STATE
    }

}

class TestStateEvaluator {
    fun evaluate(model: LazyStateModel): State<StateModel> = model.lazy()
    fun evaluate(model: IndexStateModel): State<StateModel> = ParseState(model.indexRoot, model.projectPath, model.indexMap)
    fun evaluate(model: FileParseStateModel): State<StateModel> = StateConstants.EMPTY_STATE
    fun evaluate(model: ParseStateModel): State<StateModel> = StateConstants.EMPTY_STATE
}