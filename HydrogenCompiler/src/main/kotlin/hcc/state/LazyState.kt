package hcc.state

class LazyState(private val lazy: () -> State<StateModel>): State<LazyStateModel>() {
    override fun isApplicable(): Boolean = true

    override fun output(): LazyStateModel = LazyStateModel(lazy)

    override fun execute() {
        //Do nothing
    }
}

class LazyStateModel(val lazy:() -> State<StateModel>): StateModel("Lazy State")