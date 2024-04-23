package hcc

import hcc.index.IndexState
import hcc.state.StateConstants
import hcc.state.StateManager

class RootManager(val projectPath: String) {

    fun activate() {

        val manager = StateManager(IndexState(projectPath))

        while (manager.rootState != StateConstants.EMPTY_STATE) {
            manager.execute()
            manager.evaluate()
        }
    }


}