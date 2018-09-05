package br.ufpe.cin.if710.calculadora

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.app.AlertDialog

class MainActivity : Activity() {

    //Ao ocorrerem mudanças de configuração, a expressão digitada e o último valor calculado devem permanecer visíveis na aplicação.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("resultText", findViewById<TextView>(R.id.text_info).text.toString())
        outState.putString("calcTextBox", findViewById<TextView>(R.id.text_calc).text.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Associando os listeners para cada botão da calculadora
        val calcTextBox = findViewById<EditText>(R.id.text_calc)

        if (savedInstanceState != null) {
            val calcText = savedInstanceState.getString("calcTextBox", "");
            calcTextBox.setText(calcText)
        }

        val digits = arrayOf(
                findViewById<Button>(R.id.btn_0),
                findViewById<Button>(R.id.btn_1),
                findViewById<Button>(R.id.btn_2),
                findViewById<Button>(R.id.btn_3),
                findViewById<Button>(R.id.btn_4),
                findViewById<Button>(R.id.btn_5),
                findViewById<Button>(R.id.btn_6),
                findViewById<Button>(R.id.btn_7),
                findViewById<Button>(R.id.btn_8),
                findViewById<Button>(R.id.btn_9)
        )

        for(i in 0..9){
            digits[i].setOnClickListener { calcTextBox.text.append(digits[i].text) }
        }

        val multButton = findViewById<Button>(R.id.btn_Multiply)
        multButton.setOnClickListener { calcTextBox.setText(calcTextBox.text.toString() + "*") }

        val divButton = findViewById<Button>(R.id.btn_Divide)
        divButton.setOnClickListener { calcTextBox.setText(calcTextBox.text.toString() + "/") }

        val addButton = findViewById<Button>(R.id.btn_Add)
        addButton.setOnClickListener { calcTextBox.setText(calcTextBox.text.toString() + "+") }

        val subButton = findViewById<Button>(R.id.btn_Subtract)
        subButton.setOnClickListener { calcTextBox.setText(calcTextBox.text.toString() + "-") }

        val dotButton = findViewById<Button>(R.id.btn_Dot)
        dotButton.setOnClickListener { calcTextBox.setText(calcTextBox.text.toString() + ".") }

        val openBracketsButton = findViewById<Button>(R.id.btn_LParen)
        openBracketsButton.setOnClickListener { calcTextBox.setText(calcTextBox.text.toString() + "(") }

        val closeBracketsButton = findViewById<Button>(R.id.btn_RParen)
        closeBracketsButton.setOnClickListener { calcTextBox.setText(calcTextBox.text.toString() + ")") }

        val powerButton = findViewById<Button>(R.id.btn_Power)
        powerButton.setOnClickListener { calcTextBox.setText(calcTextBox.text.toString() + "^") }

        val clearButton = findViewById<Button>(R.id.btn_Clear)
        clearButton.setOnClickListener{
            calcTextBox.setText("")
        }

        val resultText = findViewById<TextView>(R.id.text_info)

        if (savedInstanceState != null) {
            val result = savedInstanceState.getString("resultText", "");
            resultText.setText(result)
        }


        //Armazenando o resultado da expressão apos clicar em =
        val evalButton = findViewById<Button>(R.id.btn_Equal)
        evalButton.setOnClickListener{
            try {
                var exp = calcTextBox.text.toString()
                var res = eval(exp)
                resultText.text = res.toString()
            }catch(e:Exception){
                //Levantando dialog de error no caso de expressoes invalidas
                dialog(e.message)
            }
        }
    }

    fun dialog(message:String?){
        var dialog = AlertDialog.Builder(this)
        dialog.setTitle("Oops!")
        dialog.setMessage(message)
        dialog.show()
    }

    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
