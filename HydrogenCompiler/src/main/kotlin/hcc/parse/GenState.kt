package hcc.parse

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel
import hcc.parse.branch.*
import hcc.parse.call.CallState
import hcc.parse.def.DefState
import hcc.parse.loop.ForLoopState
import hcc.parse.loop.WhileLoopState
import hcc.parse.minecraft.MCCommandState
import hcc.parse.parameters.ParameterState
import hcc.parse.returning.ReturnState
import hcc.parse.util.ScaleState
import hcc.parse.`var`.VarState
import hcc.parse.world.SetEntityState
import hcc.parse.world.SetEntityV2State
import hcc.parse.world.SetScoreState
import hcc.state.State
import hcc.state.StateModel
import java.io.File

class GenState(val indexRoot: HyFile, private val projectPath: String, private val indexMap: HashMap<String, HyFile>): State<GenStateModel>() {
    var fileCompilationModel = FileCompilationModel(projectPath = projectPath, indexMap = indexMap)

    override var shouldExecuteChildren: Boolean = false

    override fun isApplicable(): Boolean = true

    override fun output(): GenStateModel = GenStateModel()

    override fun execute() {
        if (indexRoot.isDirectory()) {
            indexRoot.files.forEach {
                val genState = GenState(it, projectPath, indexMap)
                if (genState.isApplicable()) {
                    genState.executeState()
                }
            }

            return
        }

        TDLogger.d("gen", "Beginning generation of ${indexRoot.name}...")

        val fileName = indexRoot.nameWithoutExtension()
        val path = File("$projectPath${indexRoot.localPath}")
        val file = File("$projectPath${indexRoot.localPath}$fileName.mcfunction")
        indexRoot.outputFile = file
        var ifSkip = -1

        if (!path.exists()) path.mkdir()

        val parameterState = ParameterState(indexRoot.lines.first(), indexRoot, fileCompilationModel)

        if (parameterState.isApplicable()) {
            parameterState.executeState()
            parameterState.output().outputLines.forEach {
                indexRoot.output.add(it)
            }
        }

        attachChildState(MCCommandState("", indexRoot, fileCompilationModel))
        attachChildState(ScaleState("", indexRoot, fileCompilationModel))
        attachChildState(DefState("", indexRoot, fileCompilationModel))
        attachChildState(VarState("", indexRoot, fileCompilationModel))
        attachChildState(CallState("", indexRoot, fileCompilationModel))
        attachChildState(SetEntityV2State("", indexRoot, fileCompilationModel))
        attachChildState(SetScoreState("", indexRoot, fileCompilationModel))

        attachChildState(IfV2State("", indexRoot, fileCompilationModel))
        attachChildState(ForLoopState("", indexRoot, fileCompilationModel))
        attachChildState(WhileLoopState("", indexRoot, fileCompilationModel))
        attachChildState(ScopeState("", indexRoot, fileCompilationModel))

        attachChildState(ReturnState("", indexRoot, fileCompilationModel))

        indexRoot.lines.subList(1, indexRoot.lines.size).forEachIndexed { index, line ->
            if (ifSkip > index) return@forEachIndexed

            children.forEach { child ->
                val child = (child as? BaseParseState) ?: return

                child.line = line.trim()
                child.index = index + 1

                if (child.isApplicable()) {
                    child.executeState()
                    val output = child.output() as? BaseParseStateModel ?: return@forEach
                    output.outputLines.forEach {
                        indexRoot.output.add(it)
                    }
                    ifSkip = output.endLine
                }
            }
        }

        TDLogger.d("gen", "Generation of ${indexRoot.name} complete")
    }
}

class GenStateModel: StateModel("Gen State")
