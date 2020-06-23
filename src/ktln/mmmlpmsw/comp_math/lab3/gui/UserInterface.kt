package ktln.mmmlpmsw.comp_math.lab3.gui

import ktln.mmmlpmsw.comp_math.lab3.Function
import ktln.mmmlpmsw.comp_math.lab3.SplinesStorage
import java.awt.GridLayout
import java.awt.event.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.swing.*

class UserInterface(private var baseFunction: Function) {
    private var CHANGE_COEFFICIENT = "2"
    private lateinit var mainFrame: JFrame
    private lateinit var interpolateFunction: Function
    private lateinit var mainPanel: JPanel
    private lateinit var graphPanel: JPanel
    private lateinit var controlPanel: JPanel
    private lateinit var xData: DoubleArray
    private var width = 0
    private var height = 0
    private val errorTitle = "Ошибка"
    fun draw(width: Int, height: Int) {
        SwingUtilities.invokeLater {
            createMainFrame(width, height)
            mainFrame!!.isVisible = true
            mainFrame!!.isResizable = false
        }
    }

    fun createMainFrame(width: Int, height: Int) {
        this.height = height
        this.width = width
        val firstXValueCount = 4
        val xAmount = 5
        mainFrame = JFrame("Лабораторная работа №3")
        mainFrame!!.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        mainFrame!!.setSize(width, height)
        mainPanel = JPanel(GridLayout(0, 2))
        mainFrame!!.contentPane = mainPanel
        controlPanel = JPanel(GridLayout(10, 1, 0, 20))
        mainPanel!!.add(controlPanel)
        createGraphPanel()
        createSelectFunctionPanel()
        val argsPanel = createInterpolationNodeFields(firstXValueCount, xAmount)
        var changeField = createChangeYValuePanel()
        createMakeGraphButton(xAmount, argsPanel, changeField)
        createInterpolatedValueField()
    }

    private fun getGraphPanel(width: Int, height: Int, changeX: Double): JPanel {
        Arrays.sort(xData)
        var change_coef = 0.0
        try {
            change_coef = CHANGE_COEFFICIENT.toDouble()
        } catch (ignored: NumberFormatException) {
        }
        val finalChange_coef = change_coef
        var yData = Arrays.stream(xData).map { x: Double -> if (java.lang.Double.compare(x, changeX) == 0) baseFunction.getValue(x) * finalChange_coef else baseFunction.getValue(x) }.toArray()
        var splinesStorage = SplinesStorage(xData, yData)
        var interpolateFunction = splinesStorage.interpolate()
        this.interpolateFunction = interpolateFunction
        val graphPanel: JPanel
        graphPanel = if (CHANGE_COEFFICIENT == "1") {
            GraphMaker(baseFunction, interpolateFunction, xData).getChart(width, height, changeX, baseFunction.getValue(changeX) * change_coef, false)
        } else GraphMaker(baseFunction, interpolateFunction, xData).getChart(width, height, changeX, baseFunction.getValue(changeX) * change_coef, true)
        graphPanel.setLocation(0, 0)
        graphPanel.setSize(width, height)
        return graphPanel
    }

    private fun createSelectFunctionPanel() {
        val selectFunctionPanel = JPanel()
        val label = JLabel("Выберите функцию")
        selectFunctionPanel.add(label)
        val selectedFunction = JComboBox<String>()
        selectedFunction.addItem("e^x")
        selectedFunction.addItem("1/(x^4 + 4)")
        selectedFunction.addItem("x^2")
        selectedFunction.addItem("|x|")
        selectFunctionPanel.add(selectedFunction)
        controlPanel!!.add(selectFunctionPanel)
        selectedFunction.addActionListener { e: ActionEvent ->
            when ((e.source as JComboBox<*>).selectedItem as String) {
                "e^x" -> baseFunction = object : Function {
                    override fun getValue(arg: Double): Double {
                        return Math.exp(arg)
                    }
                }
                "1/(x^4 + 4)" -> baseFunction = object : Function {
                    override fun getValue(arg: Double): Double {
                        return 1 / (Math.pow(arg, 4.0) + 4)
                    }
                }
                "x^2" -> baseFunction = object : Function {
                    override fun getValue(arg: Double): Double {
                        return arg * arg
                    }
                }
                "|x|" -> baseFunction = object : Function {
                    override fun getValue(arg: Double): Double {
                        return Math.abs(arg)
                    }
                }
            }
        }
    }

    private fun createInterpolationNodeFields(firstXValue: Int, xAmount: Int): AtomicReference<JPanel> {
        val selectPanel = JPanel()
        controlPanel!!.add(selectPanel)
        val labelAmount = JLabel("Выберите количество узлов интерполирования")
        selectPanel.add(labelAmount)
        val selectedXAmount = JComboBox<Int>()
        for (i in firstXValue until firstXValue + xAmount) selectedXAmount.addItem(i)
        selectPanel.add(selectedXAmount)
        val argsPanel = AtomicReference(generateButtons(4, mainFrame))
        controlPanel!!.add(argsPanel.get())
        selectedXAmount.addActionListener { e: ActionEvent? ->
            controlPanel!!.remove(argsPanel.get())
            argsPanel.set(generateButtons(selectedXAmount.selectedIndex + firstXValue, mainFrame))
            controlPanel!!.add(argsPanel.get(), 2)
            controlPanel!!.revalidate()
            controlPanel!!.repaint()
        }
        return argsPanel
    }

    private fun generateButtons(argsAmount: Int, mainFrame: JFrame?): JPanel {
        val argsPanel = JPanel(GridLayout(1, argsAmount))
        xData = DoubleArray(argsAmount)
        for (i in 0 until argsAmount) {
            val xValue = JTextField()
            xValue.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent) {
                    try {
                        if (xValue.text != "") {
                            xData[i] = xValue.text.replace(',', '.').toDouble()
                            if (java.lang.Double.isInfinite(baseFunction.getValue(xData[i]))) {
                                xData[i] = 0.0
                                throw IllegalArgumentException()
                            }
                        }
                    } catch (e1: NumberFormatException) {
                        JOptionPane.showMessageDialog(mainFrame, "Значения Х должны быть числами",
                                "Ошибка", JOptionPane.WARNING_MESSAGE)
                    } catch (e1: IllegalArgumentException) {
                        JOptionPane.showMessageDialog(mainFrame, "Значение Х выходит за допустимые пределы",
                                "Ошибка", JOptionPane.WARNING_MESSAGE)
                    }
                }
            })
            xValue.addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent) {
                    val keyCode = e.keyCode
                    val caretPosition = xValue.caretPosition
                    if (keyCode == KeyEvent.VK_LEFT && i != 0 && caretPosition == 0) argsPanel.getComponent(i - 1).requestFocus() else if (keyCode == KeyEvent.VK_RIGHT && i != argsAmount - 1 && caretPosition == xValue.text.length) argsPanel.getComponent(i + 1).requestFocus()
                }
            })
            argsPanel.add(xValue)
        }
        return argsPanel
    }

    private fun createGraphPanel() {
        graphPanel = JPanel(null)
        graphPanel!!.setSize(width, height)
        mainPanel!!.add(graphPanel)
    }

    private fun createChangeYValuePanel(): JTextField {
        val changePanel = JPanel()
        controlPanel!!.add(changePanel)
        val changeLabel = JLabel("Введите узел, в котором нужно поменять значение функции:")
        changePanel.add(changeLabel)
        val changeField = JTextField(7)
        changePanel.add(changeField)
        return changeField
    }

    private fun createMakeGraphButton(xAmount: Int, argsPanel: AtomicReference<JPanel>, changeField: JTextField) {
        val mainButton = JButton("Интерполировать")
        mainButton.addActionListener { event: ActionEvent? ->
            for (i in 0 until xAmount - 1) {
                if ((argsPanel.get().getComponent(i) as JTextField).text == "") {
                    JOptionPane.showMessageDialog(mainFrame, "Заполните все значения Х",
                            errorTitle, JOptionPane.ERROR_MESSAGE)
                    return@addActionListener
                }
            }
            val changeX: Double
            try {
                if (changeField.text == "") {
                    CHANGE_COEFFICIENT = "1"
                    changeX = xData[0]
                } else {
                    CHANGE_COEFFICIENT = "2"
                    changeX = changeField.text.replace(',', '.').toDouble()
                }
            } catch (e: NumberFormatException) {
                JOptionPane.showMessageDialog(mainFrame, "Выберите узел, в котором нужно заменить значение функции",
                        errorTitle, JOptionPane.ERROR_MESSAGE)
                return@addActionListener
            }
            if (Arrays.stream(xData).noneMatch { n: Double -> java.lang.Double.compare(n, changeX) == 0 }) {
                JOptionPane.showMessageDialog(mainFrame, "Точка, в которой нужно заменить " +
                        "значение функции должна быть узлом интерполяции", errorTitle, JOptionPane.ERROR_MESSAGE)
            } else {
                mainPanel!!.remove(graphPanel)
                graphPanel = getGraphPanel(width, height, changeX)
                mainPanel!!.add(graphPanel)
                mainPanel!!.revalidate()
                mainPanel!!.repaint()
            }
        }
        controlPanel!!.add(mainButton)
    }

    private fun createInterpolatedValueField() {
        val findValuePanel = JPanel()
        controlPanel!!.add(findValuePanel)
        val findValueLabel = JLabel("Введите значение х, в котором нужно найти значение функции")
        findValuePanel.add(findValueLabel)
        val findValueField = JTextField(3)
        findValuePanel.add(findValueField)
        val valueLabel = JLabel(String.format("f(%s)=%s", "?", "?"))
        val findValueButton = JButton("Найти")
        findValuePanel.add(valueLabel)
        findValuePanel.add(findValueButton)
        findValueButton.addActionListener { e: ActionEvent? ->
            if (interpolateFunction == null) JOptionPane.showMessageDialog(mainFrame, "Сначала интерполируйте функцию",
                    errorTitle, JOptionPane.ERROR_MESSAGE) else {
                try {
                    val value = interpolateFunction!!.getValue(findValueField.text.replace(',', '.').toDouble())
                    valueLabel.text = String.format("f(%s)=%.3f", findValueField.text, value)
                } catch (e1: NumberFormatException) {
                    JOptionPane.showMessageDialog(mainFrame, "Значения Х должно быть числом",
                            errorTitle, JOptionPane.ERROR_MESSAGE)
                }
            }
        }
    }

}
