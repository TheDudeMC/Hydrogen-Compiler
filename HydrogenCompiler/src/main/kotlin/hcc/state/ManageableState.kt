package hcc.state

import hcc.base.logging.TDLogger

abstract class ManageableState<T: StateModel>: State<T>() {

    protected val stateManager = StateManager(LazyState {
        return@LazyState loadLazy()
    })

    open fun loadLazy(): State<StateModel> {
        TDLogger.v(STATE_TAG, "Load lazy empty state")
        return StateConstants.EMPTY_STATE
    }
}