package ktln.mmmlpmsw.comp_math.lab3.gui

import ktln.mmmlpmsw.comp_math.lab3.Function
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.XYChart
import org.knowm.xchart.style.markers.SeriesMarkers
import java.awt.Color
import java.lang.Math.abs
import javax.swing.JPanel

class GraphMaker(private val baseFunction: Function,
                 private val function: Function,
                 private val xData: DoubleArray) {
    fun getChart(width: Int, height: Int, changeX: Double,
                 changeY: Double, isChanged: Boolean): JPanel {

        val chart = XYChart(width, height)
        chart.styler.xAxisMin = xData[0]
        chart.styler.xAxisMax = xData[xData.size - 1]

        val yData = DoubleArray(xData.size)
        for (i in yData.indices)
            yData[i] = baseFunction.getValue(xData[i])
        val points = chart.addSeries("Узлы", xData, yData)
        points.marker = SeriesMarkers.CIRCLE
        points.markerColor = Color.BLUE
        points.lineColor = Color.WHITE

        if (isChanged) {
            val changeSeries = chart.addSeries("Измененное значение",
                    doubleArrayOf(changeX), doubleArrayOf(changeY))
            changeSeries.marker = SeriesMarkers.CIRCLE
            changeSeries.markerColor = Color.RED
            changeSeries.lineColor = Color.WHITE
        }

        val step = abs(xData[xData.size - 1] - xData[0]) /width
        var xGraphing = DoubleArray(width)
        var yBaseFunction = DoubleArray(width)
        var ySpline = DoubleArray(width)

        for (i in yBaseFunction.indices) {
            val arg = xData[0] + step * i
            yBaseFunction[i] = baseFunction.getValue(arg)
            ySpline[i] = function.getValue(arg)
            xGraphing[i] = arg
        }

        chart.addSeries("Исходная функция", xGraphing, yBaseFunction).
                marker = SeriesMarkers.NONE
        chart.addSeries("Интерполированная функция", xGraphing, ySpline).
                marker = SeriesMarkers.NONE

        return XChartPanel(chart)

    }
}