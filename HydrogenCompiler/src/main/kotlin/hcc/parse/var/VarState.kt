package hcc.parse.`var`

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import hcc.parse.base.*
import hcc.parse.minecraft.MCCommandState
import hcc.parse.returning.ReturnState
import java.util.Stack

//TODO: Handle static vars
class VarState(line: String, rootFile: HyFile, compilationModel: FileCompilationModel): BaseParseState<VarStateModel>(line, rootFile, compilationModel) {
    companion object {
        val operators = listOf("^", "*", "/", "+", "-")
    }

    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = doesLineStartWithVar()

    override fun output(): VarStateModel = VarStateModel(lines)

    override fun execute() {
        lines.clear()

        if (line.startsWith("var")) {
            executeInitialization()
        } else {
            executeUpdate()
        }
    }

    private fun executeUpdate() {
        val varName = line.split("=").first().trim()
        val internalVarName = compilationModel.vars[varName] ?: throw Exception("No var can be found with name \"$varName\" in file ${rootFile.name}")
        val rhs = line.split("=").last().trim()

        val generatedLine = if (rhs.toIntOrNull() != null) {
            "scoreboard players set @a $internalVarName $rhs"
        } else if (compilationModel.vars.containsKey(rhs)) {
            val internalRefName = compilationModel.vars[rhs]
            "execute store result score @a $internalVarName run scoreboard players get @p $internalRefName"
        } else if (compilationModel.parameters.vars.containsKey(rhs)) {
            val internalRefName = compilationModel.parameters.vars[rhs]
            "execute store result score @a $internalVarName run scoreboard players get @p $internalRefName"
        } else if (isOnlyDef(rhs)) {
            "execute store result score @a $internalVarName run data get storage minecraft:${getDefName(rhs)} data${getDefPath(rhs)} 1"
        } else {
            val mathState = MathEvaluatorState(internalVarName, line.split("=").last().trim(), rootFile, compilationModel)
            mathState.executeState()
            lines.addAll(mathState.output().outputLines)

            return
        }

        lines.add(generatedLine)
    }

    private fun executeInitialization() {
        val lineComponents = line.split(" ")
        val fileName = rootFile.name.replace(".tdfunction", "")
        val internalVarName = "${rootFile.localPath.replace("\\", "_")}${fileName}_reg_${lineComponents[1]}"

        compilationModel.vars[lineComponents[1]] = internalVarName

        val rhs = line.split("=").last().trim()

        val generatedLineOne = "scoreboard objectives add $internalVarName dummy \"$internalVarName\""

        val generatedLineTwo = if (rhs.toIntOrNull() != null) {
            "scoreboard players set @a $internalVarName $rhs"
        } else if (compilationModel.vars.containsKey(rhs)) {
            val internalRefName = compilationModel.vars[rhs]
            "execute store result score @a $internalVarName run scoreboard players get @p $internalRefName"
        } else if (compilationModel.parameters.vars.containsKey(rhs)) {
            val internalRefName = compilationModel.parameters.vars[rhs]
            "execute store result score @a $internalVarName run scoreboard players get @p $internalRefName"
        } else if (isOnlyDef(rhs)) {
            "execute store result score @a $internalVarName run data get storage minecraft:${getDefName(rhs)} data${getDefPath(rhs)} 1"
        } else if (MCCommandState.isMCCommand(rhs)) {
            ReturnState.generateReturn(rhs, compilationModel)
        } else {
            val mathState = MathEvaluatorState(internalVarName, line.split("=").last().trim(), rootFile, compilationModel)
            mathState.executeState()
            lines.add(generatedLineOne)
            lines.addAll(mathState.output().outputLines)

            return
        }

        lines.add(generatedLineOne)
        lines.add(generatedLineTwo)

        if (MCCommandState.isMCCommand(rhs)) {
            lines.add("execute store result score @a $internalVarName run data get storage minecraft:return data 1")
        }
    }

    private fun evaluateRHS(rhs: String): String {
        val expression = getPostFixExpression(rhs)
        val operands = expression.mapNotNull { if (!operators.contains(it)) it else null }
        val builder = StringBuilder("[${evaluateTokenRHS(operands.first())}")

        operands.subList(1, operands.size).forEachIndexed { index, token ->
            val store = evaluateTokenRHS(token)
            builder.append(", $store")
        }

        builder.append("]")

        val storageCommand = "data modify storage minecraft:rhs_storage data set value $builder"

        lines.add(storageCommand)

        operands.forEachIndexed { index, token ->
            generateTokenRHS(token, index)
        }

        var opIndex = 0
        val evaluatedOperands = Stack<Int>()

        expression.forEachIndexed { index, token ->
            if (operators.contains(token)) {
                val op2 = getStorageVarIndex(index - 1, opIndex - 1, expression, evaluatedOperands)
                val op1 = if (operators.contains(expression[index - 1])) {
                    evaluatedOperands.pop()
                } else {
                    getStorageVarIndex(index - 2, opIndex - 2, expression, evaluatedOperands)
                }

                lines.add("execute store result score @a reg_rhs_op_1 run data get storage minecraft:rhs_storage data[$op1] 1")
                lines.add("execute store result score @a reg_rhs_op_2 run data get storage minecraft:rhs_storage data[$op2] 1")
                lines.add("scoreboard players operation @a reg_rhs_op_1 $token= @p reg_rhs_op_2")
                lines.add("execute store result storage minecraft:rhs_storage data[$op2] int 1 run scoreboard players get @p reg_rhs_op_1")

                evaluatedOperands.push(opIndex - 1)

            } else {
                opIndex++
            }
        }

        lines.add("execute store result score @a reg_rhs run scoreboard players get @p reg_rhs_op_1")

        return "reg_rhs"
    }

    private fun evaluateTokenRHS(token: String): String {
        if (token.toIntOrNull() != null) return token

        return "0"
    }

    private fun generateTokenRHS(token: String, index: Int) {
        if (compilationModel.vars.keys.contains(token) || compilationModel.parameters.vars.keys.contains(token)) {
            val internalName = compilationModel.vars[token] ?: compilationModel.parameters.vars[token] ?: return

            lines.add("execute store result storage minecraft:rhs_storage data[$index] int 1 run scoreboard players get @p $internalName")
        }

        if (isDef(token)) {
            val defName = getDefName(token)
            val defPath = getDefPath(token)

            lines.add("execute store result storage minecraft:rhs_storage data[$index] int 1 run data get storage minecraft:$defName data$defPath 1")
        }
    }

    private fun getStorageVarIndex(index: Int, opIndex: Int, expression: List<String>, evaluatedOperands: Stack<Int>): Int {
        if (operators.contains(expression[index])) {
            if (evaluatedOperands.isEmpty()) throw Exception("Error: Could not build rhs evaluations")
            return evaluatedOperands.pop()
        }

        return opIndex
    }

    private fun getPostFixExpression(rhs: String): List<String> {
        val tokens = rhs.split(" ")
        val stack = Stack<String>()

        val postFixList = mutableListOf<String>()

        stack.push("(")

        tokens.forEach {
            when (it) {
                "^", "(" -> stack.push(it)
                "*", "/" -> postFixHelper(it, stack, postFixList, listOf("^"))
                "+", "-" -> postFixHelper(it, stack, postFixList, listOf("^", "*", "/"))
                ")" -> postFixHelper(it, stack, postFixList, listOf("("))

                else -> {
                    postFixList.add(it)
                }
            }
        }

        while (stack.isNotEmpty()) {
            val token = stack.pop()
            if (token != "(" && token != ")") postFixList.add(token)
        }

        return postFixList
    }

    private fun postFixHelper(token: String, stack: Stack<String>, expression: MutableList<String>, operators: List<String>) {
        val predicate = {
            if (operators.contains("(")) {
                stack.peek() != "("
            } else {
                operators.contains(stack.peek())
            }
        }

        while (predicate()) {
            expression.add(stack.pop())
        }
        stack.push(token)
    }

    private fun doesLineStartWithVar(): Boolean {
        if (line.startsWith("var")) return true
        if (line.isEmpty() || !line.contains(" ")) return false

        val lhs = line.split("=").first().trim()

        if (lhs.contains(".")) {
            val firstWord = lhs.split(".").first()
            return compilationModel.vars.containsKey(firstWord)
        }

        return compilationModel.vars.containsKey(lhs)
    }
}

class VarStateModel(lines: List<String>): BaseParseStateModel(lines)