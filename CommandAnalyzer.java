import java.util.regex.*;

/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
class CommandAnalyzer {
    public CommandAnalyzer() {

    }
    public String operand;                                                  // ��������в�֣��õ���������inputType�������operand
    public int inputType;
    private String inputString;
    public void recognise(String inputString) {
        this.inputString = inputString;
        if (isEnd()) {
            operand = "";
            inputType = 0;
            return;
        }
        if (isSimplification()) {
            operand = inputString.substring(10, inputString.length());
            inputType = 1;
            return;
        }
        if (isDerivation()) {
            operand = inputString.substring(5, inputString.length());
            inputType = 2;
            return;
        }
        if (isExpression()) {
            operand = inputString;
            inputType = 3;
            return;
        }
        operand = "";
        inputType = 4;
    }

    private boolean matchPattern(String pattern) {                                      //��inputString����ָ��������ʽ��ƥ����
        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(inputString);
        return matcher.matches();
    }

    private boolean isEnd() {
        return matchPattern("^!End");
    }

    private boolean isSimplification() {
        return matchPattern("^!Simplify [\\w|\\s|=|.|\\-|+]*");
    }

    private boolean isDerivation() {
        return matchPattern("^!d/d\\s+[a-zA-Z]+\\s*$");
    }

    private boolean isExpression() {
        return matchPattern("[\\w|\\d|\\s|\\-|+|*|^|(|)]+");
    }
}
