package hcc.parse.minecraft

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.base.BaseParseState
import hcc.parse.base.BaseParseStateModel
import hcc.parse.base.FileCompilationModel

class SaveDefCommandState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<SaveDefCommandStateModel>(line, rootFile, compilationModel) {
    companion object {

        private val SAVE_COMMAND_DEF = CommandModel(hashMapOf("_def" to CommandModel()))
        private val SAVE_COMMAND_VAR = CommandModel(hashMapOf("_var" to CommandModel()))
        private val SAVE_COMMAND_ENTITY = CommandModel(hashMapOf("_entity" to CommandModel()))

        private val SAVE_COMMAND_DATA_SOURCE = CommandModel(
            hashMapOf(
                "from " to CommandModel(
                    hashMapOf(
                        "def " to SAVE_COMMAND_DEF,
                        "var " to SAVE_COMMAND_VAR,
//                        "entity " to SAVE_COMMAND_ENTITY,
                    )
                )
            )
        )

        private val SAVE_COMMAND_SELECTOR = CommandModel(
            hashMapOf(
                "block" to CommandModel(),
                "entity " to CommandModel(
                    hashMapOf(
                        "_entity" to CommandModel(
                            hashMapOf(
                                "_path" to CommandModel(
                                    hashMapOf(
                                        "append " to SAVE_COMMAND_DATA_SOURCE,
                                        "insert " to CommandModel(
                                            hashMapOf(
                                                "_index" to SAVE_COMMAND_DATA_SOURCE
                                            )
                                        ),
                                        "merge " to SAVE_COMMAND_DATA_SOURCE,
                                        "prepend " to SAVE_COMMAND_DATA_SOURCE,
                                        "set " to SAVE_COMMAND_DATA_SOURCE
                                    )
                                )
                            )
                        )
                    )
                ),
                "storage " to CommandModel(
                    hashMapOf(
                        "_storage" to CommandModel(
                            hashMapOf(
                                "_path" to CommandModel(
                                    hashMapOf(
                                        "append " to SAVE_COMMAND_DATA_SOURCE,
                                        "insert " to CommandModel(
                                            hashMapOf(
                                                "_index" to SAVE_COMMAND_DATA_SOURCE
                                            )
                                        ),
                                        "merge " to SAVE_COMMAND_DATA_SOURCE,
                                        "prepend " to SAVE_COMMAND_DATA_SOURCE,
                                        "set " to SAVE_COMMAND_DATA_SOURCE
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        val SAVE_COMMAND_ROOT = CommandModel(
            hashMapOf(
                "data " to CommandModel(
                    hashMapOf(
                        "merge " to CommandModel(),
                        "modify " to SAVE_COMMAND_SELECTOR,
                    )
                ),
                "execute " to CommandModel(
                    hashMapOf(
                        "store " to CommandModel(
                            hashMapOf(
                                "result " to CommandModel(
                                    hashMapOf(
                                        "entity " to CommandModel(
                                            hashMapOf(
                                                "_entity" to CommandModel(
                                                    hashMapOf(
                                                        "_path" to CommandModel(
                                                            hashMapOf(
                                                                "byte " to CommandModel(),
                                                                "double " to CommandModel(),
                                                                "float " to CommandModel(
                                                                    hashMapOf(
                                                                        "_scale" to CommandModel(
                                                                            hashMapOf(
                                                                                "def " to SAVE_COMMAND_DEF,
                                                                                "var " to SAVE_COMMAND_VAR,
                                                                            )
                                                                        )
                                                                    )
                                                                ),
                                                                "int " to CommandModel(),
                                                                "long " to CommandModel(),
                                                                "short " to CommandModel(),
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        ),
                                        "storage " to CommandModel(
                                            hashMapOf(
                                                "_storage" to CommandModel(
                                                    hashMapOf(
                                                        "_path" to CommandModel(
                                                            hashMapOf(
                                                                "byte " to CommandModel(),
                                                                "double " to CommandModel(),
                                                                "float " to CommandModel(
                                                                    hashMapOf(
                                                                        "_scale" to CommandModel(
                                                                            hashMapOf(
                                                                                "def " to SAVE_COMMAND_DEF,
                                                                                "var " to SAVE_COMMAND_VAR,
                                                                            )
                                                                        )
                                                                    )
                                                                ),
                                                                "int " to CommandModel(
                                                                    hashMapOf(
                                                                        "_scale" to CommandModel(
                                                                            hashMapOf(
                                                                                "def " to SAVE_COMMAND_DEF,
                                                                                "var " to SAVE_COMMAND_VAR,
                                                                            )
                                                                        )
                                                                    )
                                                                ),
                                                                "long " to CommandModel(),
                                                                "short " to CommandModel(),
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = isApplicableHelper(line, SAVE_COMMAND_ROOT)

    private fun isApplicableHelper(line: String, commandModel: CommandModel): Boolean {
        commandModel.keywords.keys.forEach {
            val childCommandModel = commandModel.keywords[it] ?: return@forEach
            if (line.startsWith(it)) {
                return isApplicableHelper(line.substring(it.length), childCommandModel)
            } else if (it == "_entity") {
                if (line.startsWith("@a") || line.startsWith("@e") || line.startsWith("@r") || line.startsWith("@a") || line.startsWith("@s")) {
                    var l = line.substring(2)

                    if (l.startsWith("[")) {
                        while (!l.startsWith("]")) {
                            if (l.isEmpty()) throw Exception("Invalid specifier in line $line")
                            l = l.substring(1)
                        }
                    }

                    l = l.substring(2)

                    return isApplicableHelper(l, childCommandModel)
                }
            } else if (it == "_path") {
                var l = line

                while (!l.startsWith(" ")) {
                    if (l.isEmpty()) throw Exception("Invalid path in line $line")
                    l = l.substring(1)
                }

                l = l.substring(1)

                return isApplicableHelper(l, childCommandModel)
            } else if (it == "_scale") {
                val scale = line.split(" ").first()
                if (scale.toDoubleOrNull() != null) {
                    return isApplicableHelper(line.removePrefix(scale).trim(), childCommandModel)
                }

                return false
            } else if (it == "_storage") {
                val storage = line.split(" ").first()
                return isApplicableHelper(line.removePrefix(storage).trim(), childCommandModel)
            } else if (it == "_def" || it == "_var") {
                return true
            }
        }

        return false
    }

    override fun output(): SaveDefCommandStateModel = SaveDefCommandStateModel(lines)

    override fun execute() {
        SAVE_COMMAND_ROOT.keywords.keys.forEach {
            val childCommandModel = SAVE_COMMAND_ROOT.keywords[it] ?: return@forEach
            if (line.startsWith(it)) {
                if (it == "data ") {
                    generateDataSaveCommand(line, childCommandModel)
                } else if (it == "execute ") {
                    generateStoreSaveCommand(line, childCommandModel)
                }
            }
        }

        val command = generateDataSaveCommand(line, SAVE_COMMAND_ROOT)
        lines.add(command)
    }

    private fun generateStoreSaveCommand(line: String, commandModel: CommandModel): String {
        commandModel.keywords.keys.forEach {
            val childCommandModel = commandModel.keywords[it] ?: return@forEach
            if (line.startsWith(it)) {
                if (it == "def ") return "run data get storage ${generateStoreSaveCommand(line.substring(it.length), childCommandModel)}"
                if (it == "var ") return "run scoreboard players get @p ${generateStoreSaveCommand(line.substring(it.length), childCommandModel)}"
                return "$it${generateStoreSaveCommand(line.substring(it.length), childCommandModel)}"
            } else if (it == "_entity") {
                val entityPair = generateEntityBlock(line)
                return "${entityPair.first}${generateStoreSaveCommand(entityPair.second, childCommandModel)}"
            } else if (it == "_path") {
                val pathPair = generatePathBlock(line)
                return "${pathPair.first} ${generateStoreSaveCommand(pathPair.second, childCommandModel)}"
            } else if (it == "_scale") {
                val scalePair = generateScaleBlock(line)

                return "${scalePair.first} ${generateStoreSaveCommand(scalePair.second, childCommandModel)}"
            }  else if (it == "_def") {
                return generateDefBlock(line)
            } else if (it == "_var") {
                return generateVarBlock(line)
            }
        }

        return ""
    }

    private fun generateDataSaveCommand(line: String, commandModel: CommandModel): String {
        commandModel.keywords.keys.forEach {
            val childCommandModel = commandModel.keywords[it] ?: return@forEach
            if (line.startsWith(it)) {
                if (it == "def ") return "storage ${generateDataSaveCommand(line.substring(it.length), childCommandModel)}"
                if (it == "var ") return "run scoreboard players get @p ${generateDataSaveCommand(line.substring(it.length), childCommandModel)}"
                return "$it${generateDataSaveCommand(line.substring(it.length), childCommandModel)}"
            } else if (it == "_entity") {
                val entityPair = generateEntityBlock(line)
                return "${entityPair.first}${generateDataSaveCommand(entityPair.second, childCommandModel)}"
            } else if (it == "_path") {
                val pathPair = generatePathBlock(line)
                return "${pathPair.first} ${generateDataSaveCommand(pathPair.second, childCommandModel)}"
            } else if (it == "_scale") {
                val scalePair = generateScaleBlock(line)
                return "${scalePair.first} ${generateDataSaveCommand(scalePair.second, childCommandModel)}"
            } else if (it == "_storage") {
                val storagePair = generateStorageBlock(line)
                return "${storagePair.first} ${generateDataSaveCommand(storagePair.second, childCommandModel)}"
            } else if (it == "_def") {
                return generateDefBlock(line)
            } else if (it == "_var") {
                return generateVarBlock(line)
            }
        }

        return ""
    }

    private fun generateEntityBlock(line: String): Pair<String, String> {
        if (line.startsWith("@a") || line.startsWith("@e") || line.startsWith("@r") || line.startsWith("@a") || line.startsWith("@s")) {
            val entityBuilder = StringBuilder(line.substring(0, 2))
            var l = line.substring(2)

            if (l.startsWith("[")) {
                while (!l.startsWith("]")) {
                    if (l.isEmpty()) throw Exception("Invalid specifier in line $line")
                    entityBuilder.append(l.first())
                    l = l.substring(1)
                }
            }

            entityBuilder.append("] ")
            l = l.substring(2)

            return Pair(entityBuilder.toString(), l)
        }

        return Pair("", line)
    }

    private fun generateStorageBlock(line: String): Pair<String, String> {
        val pathBuilder = StringBuilder()
        var l = line

        while (!l.startsWith(" ")) {
            if (l.isEmpty()) throw Exception("Invalid path in line $line")
            pathBuilder.append(l.first())
            l = l.substring(1)
        }

        l = l.substring(1)

        return Pair(pathBuilder.toString(), l)
    }

    private fun generatePathBlock(line: String): Pair<String, String> {
        val pathBuilder = StringBuilder()
        var l = line

        while (!l.startsWith(" ")) {
            if (l.isEmpty()) throw Exception("Invalid path in line $line")
            pathBuilder.append(l.first())
            l = l.substring(1)
        }

        l = l.substring(1)

        return Pair(pathBuilder.toString(), l)
    }

    private fun generateScaleBlock(line: String): Pair<String, String> {
        val scaleBuilder = StringBuilder()
        var l = line

        while (!l.startsWith(" ")) {
            if (l.isEmpty()) throw Exception("Invalid path in line $line")
            scaleBuilder.append(l.first())
            l = l.substring(1)
        }

        l = l.substring(1)

        return Pair(scaleBuilder.toString(), l)
    }

    private fun generateDefBlock(line: String): String {
        val defName = line.split(".").first()
        val defPath = line.replace("$defName.", "")

        val internalDefName = if (compilationModel.defs.containsKey(defName))
            compilationModel.defs[defName]
        else if (compilationModel.parameters.defs.containsKey(defName))
            compilationModel.parameters.defs[defName]
        else throw Exception("Def ($defName) not defined at line: ${this.line}")

        return "minecraft:$internalDefName data.$defPath"
    }

    private fun generateVarBlock(line: String): String {
        val internalVarName = if (compilationModel.vars.containsKey(line)) {
            compilationModel.vars[line]
        } else if (compilationModel.parameters.vars.containsKey(line)) {
            compilationModel.parameters.vars[line]
        } else throw Exception("Var ($line) not define at line: ${this.line}")

        return internalVarName ?: throw Exception("Fuck you")
    }
}

class SaveDefCommandStateModel(outputLines: List<String>): BaseParseStateModel(outputLines)

class CommandModel(val keywords: HashMap<String, CommandModel> = HashMap())