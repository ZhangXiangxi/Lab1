import java.util.Scanner;

/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
public class IOInterface {
    IOInterface() {
        commandAnalyzer = new CommandAnalyzer();
        String inputString;                                 // 接收输入字符串
        Scanner scanner = new Scanner(System.in);           // 建立输入流
        do {
            inputString = scanner.nextLine();
            dispatch(inputString);
        } while(nextLoop);                                  // 循环接收并处理每一行输入，直到dispatcher通知不再接收新输入为止
        scanner.close();
    }
    private boolean nextLoop;
    public String outputString;                                         // 输出的字符串
    public boolean readyForNextLoop;
    private String inputString;
    private Expression expression;
    private CommandAnalyzer commandAnalyzer;
    private void dispatch(String inputString) {
        receiveInputString(inputString);
        nextLoop = readyForNextLoop;
        System.out.println(outputString);
    }
    private void testForNextLoop() {
        readyForNextLoop = commandAnalyzer.inputType != 0;
    }
    public void receiveInputString(String inputString) {
        this.inputString = inputString;
        commandAnalyzer.recognise(inputString);
        testForNextLoop();
        dispatchTerm();
    }
    private void dispatchTerm() {
        if (commandAnalyzer.inputType == 3) {
            try {
                SimplifyController simplifyController = new SimplifyController();
                expression = simplifyController.compile(inputString);
                outputString = expression.toString();
            } catch (Exception e) {
                outputString = e.getMessage();
            }
            return;
        }
        if (commandAnalyzer.inputType == 2) {
            if (expression.isCompiled()) {
                String variable = commandAnalyzer.operand.replaceAll("\\s", "");                      // 对操作数，去空格后输入
                if (expression.hasVariable(variable)) {
                    DerivateController derivateController = new DerivateController();
                    outputString = derivateController.derivate(expression, variable);
                }
                else
                    outputString = "No such variable in this expression.";
            }
            else
                outputString = "No valid Expression has been given.";
            return;
        }
        if (commandAnalyzer.inputType == 1) {
            if (expression.isCompiled()) {
                SubstitutionController substitutionController = new SubstitutionController();
                outputString = substitutionController.simplify(expression, commandAnalyzer.operand.replaceAll("^[\\s]*(\\w+)", "$1"));
                // 规格化赋值式,去除所有开头的空格
            }
            else
                outputString = "No such variable in this expression.";
            return;
        }
        if (commandAnalyzer.inputType == 0)
            outputString = "Bye";
        else                                                                                            // 输入不能被识别
            outputString = "No valid input recognised. ";
    }
}
