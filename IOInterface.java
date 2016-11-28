import java.util.Scanner;

/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
public class IOInterface {
    IOInterface() {
        commandAnalyzer = new CommandAnalyzer();
        String inputString;                                 // ���������ַ���
        Scanner scanner = new Scanner(System.in);           // ����������
        do {
            inputString = scanner.nextLine();
            dispatch(inputString);
        } while(nextLoop);                                  // ѭ�����ղ�����ÿһ�����룬ֱ��dispatcher֪ͨ���ٽ���������Ϊֹ
        scanner.close();
    }
    private boolean nextLoop;
    public String outputString;                                         // ������ַ���
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
                String variable = commandAnalyzer.operand.replaceAll("\\s", "");                      // �Բ�������ȥ�ո������
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
                // ��񻯸�ֵʽ,ȥ�����п�ͷ�Ŀո�
            }
            else
                outputString = "No such variable in this expression.";
            return;
        }
        if (commandAnalyzer.inputType == 0)
            outputString = "Bye";
        else                                                                                            // ���벻�ܱ�ʶ��
            outputString = "No valid input recognised. ";
    }
}
