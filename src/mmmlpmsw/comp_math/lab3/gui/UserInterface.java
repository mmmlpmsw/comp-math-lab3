package mmmlpmsw.comp_math.lab3.gui;

import mmmlpmsw.comp_math.lab3.Function;
import mmmlpmsw.comp_math.lab3.SplinesStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class UserInterface {
    private final double CHANGE_COEFFICIENT = 2;
    private JFrame mainFrame;
    private Function baseFunction;
    private Function interpolateFunction;
    private JPanel mainPanel, graphPanel, controlPanel;
    private double[] xData;
    private int width, height;
    private final String errorTitle = "Ошибка";

    public UserInterface(Function baseFunction) {
        this.baseFunction = baseFunction;
    }

    public void draw(int width, int height) {
        SwingUtilities.invokeLater(() -> {
            createMainFrame(width, height);
            mainFrame.setVisible(true);
            mainFrame.setResizable(false);
        });
    }

    public void createMainFrame(int width, int height)  {
        this.height = height;
        this.width = width;
        final int firstXValueCount = 4;
        final int xAmount = 5;

        mainFrame = new JFrame("Лабораторная работа №3");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(width, height);
        mainPanel = new JPanel(new GridLayout(0, 2));
        mainFrame.setContentPane(mainPanel);
        controlPanel = new JPanel(new GridLayout(10, 1, 0, 20));
        mainPanel.add(controlPanel);
        createGraphPanel();
        createSelectFunctionPanel();
        AtomicReference<JPanel> argsPanel = createInterpolationNodeFields(firstXValueCount, xAmount);
        JTextField changeField = createChangeYValuePanel();
        createMakeGraphButton(xAmount, argsPanel, changeField);
        createInterpolatedValueField();
    }

    private JPanel getGraphPanel(int width, int height, double changeX) {

        Arrays.sort(xData);
        double[] yData = Arrays.stream(xData).map(x -> Double.compare(x, changeX) == 0 ?
                baseFunction.getValue(x) * CHANGE_COEFFICIENT : baseFunction.getValue(x)).toArray();

        Function interpolateFunction = new SplinesStorage(xData, yData).interpolate();
        this.interpolateFunction = interpolateFunction;

        JPanel graphPanel = new GraphMaker(baseFunction, interpolateFunction, xData).
                getChart(width, height, changeX, baseFunction.getValue(changeX) * CHANGE_COEFFICIENT);
        graphPanel.setLocation(0, 0);
        graphPanel.setSize(width, height);
        return graphPanel;
    }

    private void createSelectFunctionPanel() {
        JPanel selectFunctionPanel = new JPanel();
        JLabel label = new JLabel("Выберите функцию");
        selectFunctionPanel.add(label);
        JComboBox<String> selectedFunction = new JComboBox<>();
        selectedFunction.addItem("e^x");
        selectedFunction.addItem("1/(x^4 + 4)");
        selectedFunction.addItem("x^2");
        selectFunctionPanel.add(selectedFunction);
        controlPanel.add(selectFunctionPanel);
        selectedFunction.addActionListener(e -> {
            String function = (String)(((JComboBox)e.getSource()).getSelectedItem());
            switch (function) {
                case "e^x":
                    this.baseFunction = Math::exp;
                    break;
                case "1/(x^4 + 4)":
                    this.baseFunction = arg -> 1/(Math.pow(arg, 4) + 4);
                    break;
                case "x^2":
                    this.baseFunction = arg -> arg*arg;
                    break;

            }
        });
    }

    private AtomicReference<JPanel> createInterpolationNodeFields(int firstXValue, int xAmount) {
        JPanel selectPanel = new JPanel();
        controlPanel.add(selectPanel);
        JLabel labelAmount = new JLabel("Выберите количество узлов интерполирования");
        selectPanel.add(labelAmount);
        JComboBox<Integer> selectedXAmount = new JComboBox<>();
        for (int i = firstXValue; i < firstXValue + xAmount; i++)
            selectedXAmount.addItem(i);
        selectPanel.add(selectedXAmount);

        AtomicReference<JPanel> argsPanel = new AtomicReference<>(generateButtons(4, mainFrame));
        controlPanel.add(argsPanel.get());
        selectedXAmount.addActionListener(e -> {
            controlPanel.remove(argsPanel.get());
            argsPanel.set(generateButtons(selectedXAmount.getSelectedIndex() + firstXValue, mainFrame));
            controlPanel.add(argsPanel.get(), 2);
            controlPanel.revalidate();
            controlPanel.repaint();
        });
        return argsPanel;
    }

    private JPanel generateButtons(int argsAmount, JFrame mainFrame) {
        JPanel argsPanel = new JPanel(new GridLayout(1, argsAmount));
        xData = new double[argsAmount];
        for (int i = 0; i < argsAmount; i++) {
            int index = i;
            JTextField xValue = new JTextField();
            xValue.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        if (!xValue.getText().equals("")) {
                            xData[index] = Double.parseDouble(xValue.getText().replace(',', '.'));
                            if(Double.isInfinite(baseFunction.getValue(xData[index]))){
                                xData[index] = 0;
                                throw new IllegalArgumentException();
                            }
                        }
                    } catch (NumberFormatException e1) {
                        JOptionPane.showMessageDialog(mainFrame, "Значения Х должны быть числами",
                                "Ошибка", JOptionPane.WARNING_MESSAGE);
                    }
                    catch (IllegalArgumentException e1){
                        JOptionPane.showMessageDialog(mainFrame, "Значение Х выходит за допустимые пределы",
                                "Ошибка", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            xValue.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    int keyCode = e.getKeyCode();
                    int caretPosition = xValue.getCaretPosition();
                    if (keyCode == KeyEvent.VK_LEFT && index != 0 && caretPosition == 0)
                        argsPanel.getComponent(index - 1).requestFocus();
                    else if (keyCode == KeyEvent.VK_RIGHT && index != argsAmount - 1
                            && caretPosition == xValue.getText().length())
                        argsPanel.getComponent(index + 1).requestFocus();
                }
            });
            argsPanel.add(xValue);
        }
        return argsPanel;
    }

    private void createGraphPanel() {
        graphPanel = new JPanel(null);
        graphPanel.setSize(width, height);
        mainPanel.add(graphPanel);
    }

    private JTextField createChangeYValuePanel() {
        JPanel changePanel = new JPanel();
        controlPanel.add(changePanel);
        JLabel changeLabel = new JLabel("Введите узел, в котором нужно поменять значение функции:");
        changePanel.add(changeLabel);
        JTextField changeField = new JTextField(7);
        changePanel.add(changeField);
        return changeField;
    }

    private void createMakeGraphButton(int xAmount, AtomicReference<JPanel> argsPanel, JTextField changeField) {
        JButton mainButton = new JButton("Интерполировать");
        mainButton.addActionListener(event -> {
            for (int i = 0; i < xAmount - 1; i++) {
                if (((JTextField) argsPanel.get().getComponent(i)).getText().equals("")) {
                    JOptionPane.showMessageDialog(mainFrame, "Заполните все значения Х",
                            errorTitle, JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            double changeX;
            try {
                changeX = Double.parseDouble(changeField.getText().replace(',', '.'));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "Выберите узел, в котором нужно заменить значение функции",
                        errorTitle, JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (Arrays.stream(xData).noneMatch(n -> Double.compare(n, changeX) == 0)) {
                JOptionPane.showMessageDialog(mainFrame, "Точка, в которой нужно заменить " +
                        "значение функции должна быть узлом интерполяции", errorTitle, JOptionPane.ERROR_MESSAGE);
            }
            else {
                mainPanel.remove(graphPanel);
                graphPanel = getGraphPanel(width, height, changeX);
                mainPanel.add(graphPanel);
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
        controlPanel.add(mainButton);
    }

    private void createInterpolatedValueField() {
        JPanel findValuePanel = new JPanel();
        controlPanel.add(findValuePanel);
        JLabel findValueLabel = new JLabel("Введите значение х, в котором нужно найти значение функции");
        findValuePanel.add(findValueLabel);
        JTextField findValueField = new JTextField(3);
        findValuePanel.add(findValueField);
        JLabel valueLabel = new JLabel(String.format("f(%s)=%s", "?", "?"));
        JButton findValueButton = new JButton("Найти");
        findValuePanel.add(valueLabel);
        findValuePanel.add(findValueButton);
        findValueButton.addActionListener(e -> {
            if (interpolateFunction == null)
                JOptionPane.showMessageDialog(mainFrame, "Сначала интерполируйте функцию",
                        errorTitle, JOptionPane.ERROR_MESSAGE);
            else {
                try {
                    double value = interpolateFunction.getValue(
                            Double.parseDouble(findValueField.getText().replace(',', '.')));
                    valueLabel.setText(String.format("f(%s)=%.3f", findValueField.getText(), value));
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(mainFrame, "Значения Х должно быть числом",
                            errorTitle, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

}
