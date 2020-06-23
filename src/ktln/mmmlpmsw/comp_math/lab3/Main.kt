package ktln.mmmlpmsw.comp_math.lab3

import ktln.mmmlpmsw.comp_math.lab3.gui.UserInterface

fun main() {
    val userInterface = UserInterface(object : Function {
        override fun getValue(arg: Double): Double {
            return Math.exp(arg)
        }
    })
    userInterface.draw(1500, 700)
}
