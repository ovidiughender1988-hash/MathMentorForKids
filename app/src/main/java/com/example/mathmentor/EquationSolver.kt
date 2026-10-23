package com.example.mathmentor

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class EquationResult(
    val answer: String,
    val steps: List<String>
) {
    fun formatForDisplay(): String = buildString {
        append(answer)
        append("\n\n")
        append(steps.joinToString(separator = "\n"))
    }
}

object EquationSolver {

    fun solve(input: String): EquationResult {
        val clean = normalizeInput(input)
        if (clean.isEmpty()) {
            return EquationResult(
                "Please enter a math problem.",
                listOf("Type or scan a problem to begin.")
            )
        }

        return try {
            if (clean.contains("=")) {
                solveEquation(clean)
            } else {
                solveArithmetic(clean)
            }
        } catch (e: Exception) {
            EquationResult(
                "This expression could not be solved with the current rules.",
                listOf(
                    "Try an equation like 3x + 7 = 19",
                    "or a calculation like 12 / 3 + 5",
                    "Use only numbers, +, -, *, /, parentheses, and x"
                )
            )
        }
    }

    private fun normalizeInput(raw: String): String {
        return raw
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("\u00A0", "")
            .replace("X", "x")
            .replace("•", "*")
            .trim()
    }

    private fun solveArithmetic(input: String): EquationResult {
        val value = evaluateExpression(input, variableValue = 0.0)
        val formatted = formatNumber(value)
        return EquationResult(
            "Answer = $formatted",
            listOf(
                "Original expression: $input",
                "Follow the order of operations: parentheses, exponents, multiplication/division, then addition/subtraction.",
                "Final result: $formatted"
            )
        )
    }

    private fun solveEquation(input: String): EquationResult {
        val parts = input.split("=", limit = 2)
        if (parts.size != 2) {
            return EquationResult(
                "Invalid equation.",
                listOf("Use a format like 2x + 5 = 17")
            )
        }

        val left = parts[0].trim()
        val right = parts[1].trim()

        if (!left.contains("x") && !right.contains("x")) {
            val lv = evaluateExpression(left, 0.0)
            val rv = evaluateExpression(right, 0.0)
            return EquationResult(
                "Left side = ${formatNumber(lv)} and right side = ${formatNumber(rv)}",
                listOf(
                    "No variable detected.",
                    "Left side: ${formatNumber(lv)}",
                    "Right side: ${formatNumber(rv)}"
                )
            )
        }

        val linear = trySolveLinear(left, right)
        if (linear != null) {
            return linear
        }

        val quadratic = trySolveQuadratic(left, right)
        if (quadratic != null) {
            return quadratic
        }

        return EquationResult(
            "This equation is not in a supported form yet.",
            listOf(
                "Try a linear equation like 3x + 7 = 19",
                "or a quadratic equation like x^2 - 5x + 6 = 0"
            )
        )
    }

    private fun trySolveLinear(left: String, right: String): EquationResult? {
        val exprLeft = simplifyExpression(left)
        val exprRight = simplifyExpression(right)

        val leftTerms = collectLinearTerms(exprLeft)
        val rightTerms = collectLinearTerms(exprRight)

        val leftCoeff = leftTerms.a
        val leftConst = leftTerms.b
        val rightCoeff = rightTerms.a
        val rightConst = rightTerms.b

        val totalCoeff = leftCoeff - rightCoeff
        val totalConst = rightConst - leftConst

        if (abs(totalCoeff) < 1e-9) {
            return null
        }

        val xValue = (-totalConst) / totalCoeff
        val formatted = formatNumber(xValue)
        return EquationResult(
            "x = $formatted",
            listOf(
                "Equation: $left = $right",
                "Move all x-terms to one side and constants to the other.",
                "Coefficient of x: ${formatNumber(totalCoeff)}",
                "Constant adjustment: ${formatNumber(totalConst)}",
                "x = -(${formatNumber(totalConst)}) / ${formatNumber(totalCoeff)} = $formatted"
            )
        )
    }

    private fun trySolveQuadratic(left: String, right: String): EquationResult? {
        val leftNorm = normalizePolynomial(left)
        val rightNorm = normalizePolynomial(right)

        val leftPoly = parsePolynomial(leftNorm)
        val rightPoly = parsePolynomial(rightNorm)
        val a = leftPoly.a - rightPoly.a
        val b = leftPoly.b - rightPoly.b
        val c = leftPoly.c - rightPoly.c

        if (abs(a) < 1e-9) {
            return null
        }

        val discriminant = b * b - 4.0 * a * c
        if (discriminant < 0.0) {
            return EquationResult(
                "No real solutions.",
                listOf(
                    "Equation is quadratic but the discriminant is negative.",
                    "Discriminant = ${formatNumber(discriminant)}"
                )
            )
        }

        val sqrtDisc = sqrt(discriminant)
        val x1 = (-b + sqrtDisc) / (2.0 * a)
        val x2 = (-b - sqrtDisc) / (2.0 * a)

        val x1Text = formatNumber(x1)
        val x2Text = formatNumber(x2)

        return EquationResult(
            "x = $x1Text or x = $x2Text",
            listOf(
                "Quadratic equation: $left = $right",
                "Write it in standard form: ${formatQuadratic(a, b, c)} = 0",
                "Discriminant: ${formatNumber(discriminant)}",
                "x = (-b ± √D) / 2a",
                "x = $x1Text or x = $x2Text"
            )
        )
    }

    private fun simplifyExpression(value: String): String {
        return value
            .replace(" ", "")
            .replace("(-", "(0-")
    }

    private data class LinearTerms(val a: Double, val b: Double)

    private fun collectLinearTerms(expr: String): LinearTerms {
        val simplified = expr.replace(" ", "")
        var a = 0.0
        var b = 0.0

        if (!simplified.contains("x")) {
            b = evaluateExpression(simplified, 0.0)
            return LinearTerms(0.0, b)
        }

        val signPattern = if (simplified.startsWith("-")) "-" else "+"
        val normalized = simplified.replace("+", ";").replace("-", ";")
        val parts = normalized.split(";").filter { it.isNotEmpty() }

        for (part in parts) {
            if (part.contains("x")) {
                val coefficientText = part.replace("x", "")
                val coefficient = if (coefficientText.isEmpty()) 1.0 else {
                    if (coefficientText == "/") 1.0 else coefficientText.toDouble()
                }
                a += coefficient
            } else {
                b += part.toDouble()
            }
        }

        if (simplified.contains("x") && !simplified.contains("+") && !simplified.contains("-")) {
            if (simplified.endsWith("x")) {
                a = 1.0
            }
        }

        return LinearTerms(a, b)
    }

    private data class Polynomial(val a: Double, val b: Double, val c: Double)

    private fun normalizePolynomial(expr: String): String {
        var text = expr.replace(" ", "")
        text = text.replace("-", "+-")
        val tokens = text.split("+")
            .filter { it.isNotEmpty() }
            .map { it.trim() }

        val terms = mutableListOf<String>()
        for (token in tokens) {
            if (token.isNotEmpty()) {
                terms.add(token)
            }
        }
        return terms.joinToString("+")
    }

    private fun parsePolynomial(expr: String): Polynomial {
        val cleaned = expr.replace(" ", "")
        val pieces = cleaned.split("+").filter { it.isNotEmpty() }

        var a = 0.0
        var b = 0.0
        var c = 0.0

        for (piece in pieces) {
            val signed = if (piece.startsWith("-")) "-" else "+"
            val term = if (piece.startsWith("-")) piece.substring(1) else piece
            val isQuadratic = term.contains("x^2")
            val isLinear = term.contains("x") && !term.contains("x^2")

            when {
                isQuadratic -> {
                    val coefficientText = term.replace("x^2", "")
                    a += if (coefficientText.isEmpty()) 1.0 else coefficientText.toDouble()
                }
                isLinear -> {
                    val coefficientText = term.replace("x", "")
                    b += if (coefficientText.isEmpty()) 1.0 else coefficientText.toDouble()
                }
                else -> c += term.toDouble()
            }
        }

        return Polynomial(a, b, c)
    }

    private fun formatQuadratic(a: Double, b: Double, c: Double): String {
        val aText = if (abs(a - 1.0) < 1e-9) "" else formatNumber(a)
        val bText = if (abs(b - 1.0) < 1e-9) "x" else "${formatNumber(b)}x"
        val cText = if (abs(c) < 1e-9) "" else formatNumber(c)
        return "$aText${if (aText.isNotEmpty()) "x^2" else "x^2"} ${if (b >= 0) "+" else "-"} $bText ${if (c >= 0) "+" else "-"} $cText"
    }

    private fun evaluateExpression(expression: String, variableValue: Double): Double {
        val clean = expression.replace(" ", "").trim()
        if (clean.isEmpty()) {
            return 0.0
        }

        val tokens = tokenizeExpression(clean)
        var index = 0

        fun parseExpression(): Double {
            var value = parseTerm()
            while (index < tokens.size && (tokens[index] == "+" || tokens[index] == "-")) {
                val op = tokens[index]
                index++
                val next = parseTerm()
                value = if (op == "+") value + next else value - next
            }
            return value
        }

        fun parseTerm(): Double {
            var value = parseFactor()
            while (index < tokens.size && (tokens[index] == "*" || tokens[index] == "/")) {
                val op = tokens[index]
                index++
                val next = parseFactor()
                value = if (op == "*") value * next else value / next
            }
            return value
        }

        fun parseFactor(): Double {
            if (index < tokens.size && (tokens[index] == "+" || tokens[index] == "-")) {
                val op = tokens[index]
                index++
                val value = parseFactor()
                return if (op == "+") value else -value
            }

            if (index < tokens.size && tokens[index] == "(") {
                index++
                val value = parseExpression()
                if (index >= tokens.size || tokens[index] != ")") {
                    throw IllegalArgumentException("Missing closing parenthesis")
                }
                index++
                return value
            }

            if (index < tokens.size && tokens[index] == "x") {
                index++
                return variableValue
            }

            if (index < tokens.size && tokens[index].contains("^") ) {
                val baseToken = tokens[index].substringBefore("^")
                val expToken = tokens[index].substringAfter("^")
                val baseValue = if (baseToken == "x") variableValue else baseToken.toDouble()
                val expValue = expToken.toDouble()
                index++
                return baseValue.pow(expValue)
            }

            if (index < tokens.size && tokens[index].matches(Regex("^\\d+(\\.\\d+)?$"))) {
                val value = tokens[index].toDouble()
                index++
                return value
            }

            throw IllegalArgumentException("Unexpected token: ${tokens.getOrNull(index) ?: "EOF"}")
        }

        val result = parseExpression()
        if (index != tokens.size) {
            throw IllegalArgumentException("Unconsumed tokens: ${tokens.subList(index, tokens.size)}")
        }
        return result
    }

    private fun tokenizeExpression(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0

        while (i < expression.length) {
            val ch = expression[i]

            when {
                ch.isWhitespace() -> i++
                ch.isDigit() || ch == '.' -> {
                    var text = ch.toString()
                    i++
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                        text += expression[i]
                        i++
                    }
                    tokens.add(text)
                }
                ch == 'x' || ch == 'X' -> {
                    tokens.add("x")
                    i++
                }
                ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '(' || ch == ')' -> {
                    tokens.add(ch.toString())
                    i++
                }
                ch == '^' -> {
                    tokens.add("^")
                    i++
                }
                else -> {
                    if (ch.isLetter()) {
                        var text = ch.toString()
                        i++
                        while (i < expression.length && expression[i].isLetter()) {
                            text += expression[i]
                            i++
                        }
                        tokens.add(text)
                    } else {
                        throw IllegalArgumentException("Unsupported character: $ch")
                    }
                }
            }
        }

        return tokens
    }

    private fun formatNumber(value: Double): String {
        return if (abs(value - value.roundToInt().toDouble()) < 1e-9) {
            value.roundToInt().toString()
        } else {
            String.format("%.2f", value)
        }
    }
}
