package hcc.parse.base

import hcc.base.logging.TDLogger
import hcc.base.models.HyFile
import java.util.*

enum class MathDestinationType {
    VAR,
    DEF
}

class MathEvaluatorState(
    private val destination: String,
    line: String,
    rootFile: HyFile,
    compilationModel: FileCompilationModel,
    private val destinationType: MathDestinationType = MathDestinationType.VAR
): BaseParseState<MathEvaluatorStateModel>(line, rootFile, compilationModel) {
    companion object {
        val operators = listOf("^", "*", "/", "+", "-")
        val types = listOf("byte", "double", "float", "int", "long", "short")
    }

    private val lines = mutableListOf<String>()

    override fun isApplicable(): Boolean = true

    override fun output(): MathEvaluatorStateModel = MathEvaluatorStateModel(lines)

    override fun execute() {
        val optionalType = line.split(" ").first()
        var trimmedLine = line
        val type = if (types.contains(optionalType)) {
            trimmedLine = line.replaceFirst(optionalType, "").trim()
            optionalType
        } else "int"
        val expression = getPostFixExpression(trimmedLine)
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

        if (operands.size == 1) lines.add("execute store result score @a reg_rhs_op_1 run data get storage minecraft:rhs_storage data[0] 1")

        lines.add("execute store result score @a reg_rhs run scoreboard players get @p reg_rhs_op_1")

        if (destinationType == MathDestinationType.VAR) {
            lines.add("execute store result score @a $destination run scoreboard players get @p reg_rhs")
        } else if (destinationType == MathDestinationType.DEF) {
            //replace "float" with "byte, double, float, int, long, short"
            lines.add("execute store result storage minecraft:$destination $type ${compilationModel.scale} run scoreboard players get @p reg_rhs")
        }

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

    private fun getPostFixExpression(rhs: String): MutableList<String> {
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


    private fun evaluateTokenRHS(token: String): String {
        if (token.toIntOrNull() != null) return token

        return "0"
    }
}

class MathEvaluatorStateModel(lines: List<String>): BaseParseStateModel(lines)