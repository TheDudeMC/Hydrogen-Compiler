package hcc.state

class EmptyState: State<EmptyStateModel>() {
    override fun isApplicable(): Boolean = true

    override fun execute() {}

    override fun output(): EmptyStateModel = EmptyStateModel()
}

class EmptyStateModel: StateModel("End")