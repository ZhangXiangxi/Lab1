/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
public class PowerNotationReplacer {
    public PowerNotationReplacer(String expression) {
        this.expression = expression;
        result = expression;
    }

    private String expression;
    public String result = "xx";

    public String getResult() throws ExpressionCompileException{
        while (expression.contains("^")) {                                          // �����ʽ���Դ����ݷ��ţ����д���
            int notionIndex = expression.indexOf("^");                              // �ҵ���һ���ݷ��ŵ�λ��
            String stringAfterNotion = expression.substring(notionIndex+1);         // �ݷ��ź���Ӵ�
            if (!stringAfterNotion.matches("^[\\d]+.*"))                            // ��������Ӵ����������ֿ�ͷ������ʽ���Ϸ�
                throw new ExpressionCompileException("A single natural number expected behind the power notion.");
            String powerString = stringAfterNotion.split("\\D")[0];
            int power = Integer.parseInt(powerString);                              // ��ȡ����ݷ��ź���ݴ�
            if (power < 1)                                                          // ��������Ӵ����ֲ���������������ʽ���Ϸ�
                throw new ExpressionCompileException("A single natural number expected behind the power notion.");
            int startIndex;                                                         // �ݷ���֧��Ĳ��ֵ���ʼλ��
            try {
                startIndex = findRepeatPosition(expression.substring(0, notionIndex));
            } catch (ExpressionCompileException e) {
                throw e;
            }
            String formerFragment = expression.substring(0, startIndex);            // ǰ���Ӵ�
            String middleFragment = expression.substring(startIndex, notionIndex);  // �в��Ӵ�������Ҫ�ظ��Ĳ���
            String latterFragment = expression.substring(notionIndex+1+powerString.length());
                                                                                    // �ݷ���λ������һλ���Ƴ�������ռλ���õ����Ӵ�
            result = formerFragment;
            while (power != 1) {
                result += middleFragment + "*";
                power--;
            }
            result += middleFragment;
            result += latterFragment;
            expression = result;
        }
        return result;
    }
                                                                                    // �ҵ����ַ�����Ӧ�����ݷ����ظ��Ĳ���
    private int findRepeatPosition(String inputString) throws ExpressionCompileException {
        try{
            if (inputString.endsWith(")"))                                          // ���ݴ�ǰ������
                return findLastMatchBracket(inputString);
            if (inputString.substring(inputString.length()-1).matches("[a-zA-Z]"))       // ���ݴ�ǰ�Ǳ���
                return findLastMatchAlphabet(inputString);
            throw new ExpressionCompileException("Variables expected before power notion.");
        } catch(ExpressionCompileException e) {
            throw e;
        }
    }

    private int findLastMatchBracket(String inputString) throws ExpressionCompileException {
        int index = inputString.length()-1;                                         // ָ�궨λ�����һ�����Ŵ�
        int depth = 0;                                                              // ����Ƕ����ȴ�ʱΪ1
        do {
            if (index == -1)                                                        // ��ַ�����δ���ҵ�ƥ��λ�ã�˵�����벻�Ϸ�
                throw new ExpressionCompileException("Brackets not match.");
            if (inputString.charAt(index) == ')')
                depth++;
            if (inputString.charAt(index) == '(')
                depth--;
            index--;                                                                // ָ������
        } while(depth != 0 || inputString.charAt(index+1) != '(');
        return index+1;
    }

    private int findLastMatchAlphabet(String inputString) {
        int index = inputString.length()-1;
        while (Character.isLetter(inputString.charAt(index))) {                     // �ҵ���һ������ĸ�ַ���λ��
            index--;
            if (index == -1)
                return 0;
        }
        return index+1;
    }
}
