package hcc.state

abstract class State<out T: StateModel> {
    companion object {
        const val STATE_TAG = "State"
    }

    protected val children = mutableListOf<State<StateModel>>()
    protected open var shouldExecuteChildren = true

    abstract fun isApplicable(): Boolean

    abstract fun output(): T

    protected abstract fun execute()

    fun executeState() {
        execute()

        if (shouldExecuteChildren) {
            children.forEach {
                if (it.isApplicable()) {
                    it.executeState()
                }
            }
        }
    }

    fun attachChildState(state: State<StateModel>) {
        children.add(state)
    }
}

open class StateModel(val id: String)