import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
class Expression {
    private boolean compiledMark;
    private ArrayList<String> resultTermVariables;
    private ArrayList<Integer> resultTermSigns;
    //private ArrayList<StringTerm> resultStringTerms;
    private HashMap<String, Integer> variableList;
    private int variableNumber;
    private ArrayList<Term> compileResults;
    private ArrayList<String> variableIndexToName;
    public static final double numericError = 1e-6;

    public Expression() {
        variableList = new HashMap<>();
        compileResults = new ArrayList<>();
        compiledMark = false;
    }

    public void compile(String expression) throws ExpressionCompileException{
        String innerString;
        innerString = blankStrip(expression);
        innerString = completeMultiplication(innerString);
        try {
            innerString = replacePowerNotion(innerString);
            reduceBracket(innerString);
            generateVariableList();
            transformIntoNumeric();
            mergeResults();
        } catch(ExpressionCompileException e) {
            compiledMark = false;
            throw e;
        }
        compiledMark = true;
    }

    public String toString() {
        return toString(compileResults);
    }

    private String toString(ArrayList<Term> polynomial) {            // ��numericTerm��ʽ�洢�Ķ���ʽת��Ϊ�ַ������
        if (polynomial.size() == 0)                                         // ������ʽ�������κ�������"0"
            return "0";
        String result = transFromNumericTermToString(polynomial.get(0));
        for(int i = 1; i < polynomial.size(); i++) {
            String termString = transFromNumericTermToString(polynomial.get(i));
            if (!termString.startsWith("-"))                                // ����ǰû�и��ţ�����Ҫ�������
                result += "+";
            result += termString;
        }
        return result;
    }
    private String blankStrip(String expression) {
        return expression.replaceAll("\\s+", "");                           // ��ȥ�����еĿո�
    }
    private String completeMultiplication(String expression) {
        String innerString = expression.replaceAll("([\\)|\\d])([\\(|a-zA-Z])", "$1*$2");
        return innerString.replaceAll("([a-zA-Z])(\\()", "$1*$2");
    }
    private String replacePowerNotion(String expression) throws ExpressionCompileException{
        PowerNotationReplacer powerNotationReplacer = new PowerNotationReplacer(expression);
        return powerNotationReplacer.getResult();
    }
    private void reduceBracket(String expression) throws ExpressionCompileException {
        BracketReducer bracketReducer = new BracketReducer(expression);
        resultTermVariables = bracketReducer.resultTermVariables;
        resultTermSigns = bracketReducer.resultTermSigns;
    }
    private void generateVariableList() {
        variableIndexToName = new ArrayList<>();
        for (String resultTermVariable : resultTermVariables) {
            for (String fragments : resultTermVariable.split("\\*"))               // �ó˺ŷָ���
                if (fragments.matches("[a-zA-Z]+") && !variableIndexToName.contains(fragments))
                    variableIndexToName.add(fragments);                     // �����δ���ֹ��Ĵ���ĸ�ִ�������뵽�������б�
        }
        for(int i = 0; i < variableIndexToName.size(); i++)
            variableList.put(variableIndexToName.get(i), i);                // ����ִ����ʵ�
        variableNumber = variableIndexToName.size();
    }
    private void transformIntoNumeric() throws ExpressionCompileException{
        for(int i = 0; i < resultTermVariables.size(); i++) {
            ArrayList<Integer> powers = new ArrayList<>();
            for(int j = 0; j < variableNumber; j++)
                powers.add(0);                                               // �ݼ�����ʼ��
            double coefficient = 1.0d;                                          // ϵ����ʼ�������ڸ����Ϊ-1
            if (resultTermSigns.get(i) == 0)
                coefficient *= -1;
            for(String fragments : resultTermVariables.get(i).split("\\*")) {                 // ���ڳ˺ŷָ�����ÿһ����
                if (fragments.matches("[a-zA-Z]+")) {
                    int index = variableList.get(fragments);
                    powers.set(index, powers.get(index) + 1);
                }
                else
                    try {
                        coefficient *= Double.parseDouble(fragments);
                    } catch(Exception e) {
                        throw new ExpressionCompileException("Can not resolve this expression");
                    }
            }
            if (Math.abs(coefficient) > numericError)
                compileResults.add(new Term(coefficient, powers));
        }
    }

    private void mergeResults() {
        mergeResults(compileResults);
    }

    private void mergeResults(ArrayList<Term> inputCompileResults) {
        boolean modified;
        do {
            modified = false;
            inputCompileResults = sortByHash(inputCompileResults);
            for(int i = 0; i < inputCompileResults.size()-1; i++) {
                Term formerTerm = inputCompileResults.get(i);
                Term latterTerm = inputCompileResults.get(i + 1);
                if (formerTerm.powers.equals(latterTerm.powers)) {              // ������������ָ����ͬ, ѡ��ϲ�
                    double coefficientSum = formerTerm.coefficient + latterTerm.coefficient;
                    if (Math.abs(coefficientSum) < numericError) {              // ��ϵ����Ϊ0����ɾȥ���������һ�����ԭ��������
                        inputCompileResults.remove(i + 1);
                        inputCompileResults.remove(i);
                    } else {
                        inputCompileResults.set(i, new Term(coefficientSum, latterTerm.powers));
                        inputCompileResults.remove(i + 1);
                    }
                    modified = true;                                            // ��¼�˴εĺϲ�
                    break;                                                      // �������нṹ�ѱ��ƻ���Ӧ������һ�������ѭ��
                }
            }
        } while(modified);
    }

    private ArrayList<Term> sortByHash(ArrayList<Term> list){
        boolean flag;
        do {
            flag = false;
            for(int i = 0; i < list.size()-1; i++) {
                if (getHashCode(list.get(i).powers) <
                        getHashCode(list.get(i+1).powers)) {
                    Object temp = list.get(i);
                    list.set(i, list.get(i+1));
                    list.set(i+1, (Term)temp);
                    flag = true;
                }
            }
        } while (flag);
        return list;
    }

    private String transFromNumericTermToString(Term term) {
        String result;
        if (Math.abs(term.coefficient-Math.round(term.coefficient)) < numericError)
            result = Long.toString(Math.round(term.coefficient));               // ��ϵ�������ƣ�Ϊ����ʱ����������ʽ��ӡ
        else
            result = Double.toString(term.coefficient);                         // ������С����ʽ��ӡ
        for(int i = 0; i < variableNumber; i++) {
            int power = term.powers.get(i);                                     // ��λ�õĶ�Ӧָ��
            if (power > 0)
                result += "*" + variableIndexToName.get(i);                     // ָ����������ӱ�����
            if (power > 1)
                result += "^" + Integer.toString(power);                        // ָ������1������ݴ�
        }
        if (result.startsWith("1*"))
            return result.substring(2);                                         // ��Ϊ1*a��ʽ����ʡ��ǰ���1
        return result;
    }

    public boolean isCompiled() {
        return compiledMark;
    }
    public boolean hasVariable(String variable) {
        return variableIndexToName.contains(variable);                      // �����ñ����Ƿ�����ڱ����б���
    }
    public String derivate(String variable) {
        int variableIndex = variableList.get(variable);                 // ���󵼱����ڱ����б��еı��
        ArrayList<Term> derivedResult = new ArrayList<>();       // �󵼽������ʽ��NumericTerm��ʽ���
        ArrayList<Integer> tempPowers = new ArrayList<>();              // ��������ָ���ݴ��б�

        for (Term term : compileResults) {
            int variablePower = term.powers.get(variableIndex);         // ��ԭ����ʽÿһ���д��󵼱������ݴ�
            if (variablePower > 0 ) {                                   // ���ݴ�Ϊ����������õ��������result��
                tempPowers.addAll(term.powers);
                tempPowers.set(variableIndex, variablePower-1);
                derivedResult.add(new Term(term.coefficient * variablePower, tempPowers));
                tempPowers.clear();
            }
        }
        return toString(derivedResult);
    }
    public String simplify(String assignments) {
        ArrayList<String> variableArray = new ArrayList<>();
        ArrayList<Double> valueArray = new ArrayList<>();
        boolean isVariable = true;
        for(String fragments : assignments.split("[=| ]")) {                // �õȺ���ո���зָ�
            if (fragments.equals(""))                                       // ����ȡ�����ǿմ�����Ӧ�����κδ���
                continue;
            if (isVariable) {                                               // ���˴�ӦΪ������
                if (variableArray.contains(fragments))
                    return "There're multiple variables " + fragments +"."; // �������ظ���ֵ����
                variableArray.add(fragments);
                isVariable = false;
            } else {                                                        // ���˴�ӦΪ��ֵ
                try{
                    valueArray.add(Double.parseDouble(fragments));
                } catch (NumberFormatException e) {                         // ���ʵ����ʽ����
                    return "The substring " + fragments + " cannot be parsed into a real number.";
                }
                isVariable = true;
            }
        }
        if (variableArray.size() != valueArray.size())                      // ��������������ֵ������ƥ��Ĵ���
            return "The number of variables and values don't match. ";
        for (String variable : variableArray)                               // ��������������ԭ���ʽ�Ĵ���
            if (!variableIndexToName.contains(variable))
                return "No such variable in the former expression.";
        ArrayList<Term> simplifiedResult = new ArrayList<>();// ����������ʽ��NumericTerm��ʽ���
        for(Term originalTerm : compileResults) {                    // ����ԭ����ʽ�е�ÿһ��
            ArrayList<Integer> tempPowers = new ArrayList<>(originalTerm.powers);
                                                                            // ��������ָ���ݴ��б�
            double tempCoefficient = originalTerm.coefficient;              // ϵ���ݴ�
            for (int i = 0; i < variableArray.size(); i++) {                // ���ڸ�ֵ�б��е�ÿһ�鸳ֵ
                int variableIndex = variableList.get(variableArray.get(i)); // ȡ�ñ������
                int variablePower = tempPowers.get(variableIndex);          // ȡ�ñ����ݴ�
                double variableValue = valueArray.get(i);                   // ȡ�ñ�������ֵ
                tempCoefficient *= Math.pow(variableValue, variablePower);  // ������ϵ��
                tempPowers.set(variableIndex, 0);                           // ����ԭ����
            }
            if (Math.abs(tempCoefficient) >= numericError)                  // ϵ��Ϊ������Զ�����
                simplifiedResult.add(new Term(tempCoefficient, tempPowers));
        }
        mergeResults(simplifiedResult);
        return toString(simplifiedResult);
    }

    private long getHashCode(ArrayList<Integer> list) {
        if (list.size() == 0)
            return 0;
        long result = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            result = result*33 + list.get(i);
        }
        return result;
    }
    class BracketReducer {
        public BracketReducer(String expression) throws ExpressionCompileException {
            resultStringTerms = breakAtPlusAndSub(expression);
            generateResults();
        }
        public ArrayList<StringTerm> resultStringTerms;
        public ArrayList<String> resultTermVariables;
        public ArrayList<Integer> resultTermSigns;
        private void generateResults() {
            resultTermVariables = new ArrayList<>();
            resultTermSigns = new ArrayList<>();
            for(StringTerm resultStringTerm : resultStringTerms) {
                resultTermVariables.add(resultStringTerm.content);
                resultTermSigns.add(resultStringTerm.sign);
            }
        }
        public String toString(){
            String result = "";
            int len = resultStringTerms.size();
            for (int i = 0;i<len;i++) {
                if (i == 0 && resultStringTerms.get(i).sign == 1) {
                    result += resultStringTerms.get(i).content;
                }
                else {
                    if (resultStringTerms.get(i).sign == 1) result += "+";
                    else result += "-";
                    result += resultStringTerms.get(i).content;
                }
            }
            return result;
        }

        private ArrayList<StringTerm> breakAtPlusAndSub(String exp) throws ExpressionCompileException{   // ��һ���ɼӼ����Ӹ������ʽ�ӽ��д���
            ArrayList<StringTerm> rel = new ArrayList<>();
            ArrayList<Integer> notation = divideAtLowLevel(exp);
            if (exp.charAt(0) == '-')
                return breakAtPlusAndSub("0" + exp);
            int len = notation.size();
            if (divideWithCharacters(exp,"+-*").size() == 1 && exp.charAt(0) == '(')            // ����������ʽ��һ�����Ű���
                return breakAtPlusAndSub(exp.substring(1, exp.length() - 1));                     // �Ͱ�ȥ�����ٽ��к�������
            for (int i = 0;i<len;i++) {
                int head = notation.get(i);
                int tail;
                if (i == len -1) {
                    tail = exp.length();
                }
                else {
                    tail = notation.get(i + 1) -1;
                }
                String substr = exp.substring(head,tail);
                ArrayList<StringTerm> temp = new ArrayList<>();
                if (substr.contains("(")) {
                    temp = breakAtMultiplification(substr);
                }
                else {
                    temp.add(new StringTerm(substr,'\1'));
                }
                if (i != 0 && exp.charAt(notation.get(i) - 1) == '-') {
                    for (StringTerm aTemp : temp) {
                        aTemp.sign = 1 - aTemp.sign;
                    }
                }
                rel.addAll(temp);
            }
            return rel;
        }
        private ArrayList<StringTerm> breakAtMultiplification(String exp) throws ExpressionCompileException{
            ArrayList<Integer> Splits = divideAtHighLevel(exp);
            int len = Splits.size();
            if (divideWithCharacters(exp,"+-*").size() == 1 && exp.charAt(0) == '(')               // ����������ʽ��һ�����Ű���
                return breakAtMultiplification(exp.substring(1, exp.length() - 1));                // �Ͱ�ȥ�����ٽ��к�������
            ArrayList<StringTerm> a = new ArrayList<>();
            ArrayList<StringTerm> b;
            a.add(new StringTerm("1",'\1'));
            String subStr;
            for (int i = 0;i<len;i++) {
                int head = Splits.get(i);
                int tail;
                if (i == len -1) {
                    tail = exp.length();
                }
                else {
                    tail = Splits.get(i + 1) -1;
                }
                subStr = exp.substring(head,tail);
                b = breakAtPlusAndSub(subStr);
                a = multiplyTerm(a, b);
            }
            return a;

        }
        private ArrayList<StringTerm> multiplyTerm(ArrayList<StringTerm> a, ArrayList<StringTerm> b) {
            ArrayList<StringTerm> rel = new ArrayList<>();
            StringTerm temp;
            for (StringTerm anA : a) {
                for (StringTerm aB : b) {
                    temp = new StringTerm();
                    temp.content = anA.content + "*" + aB.content;
                    if (anA.sign != aB.sign) {
                        temp.sign = '\0';
                    } else {
                        temp.sign = '\1';
                    }
                    rel.add(temp);
                }
            }
            return rel;
        }
        private ArrayList<Integer> divideAtLowLevel(String inputString) throws ExpressionCompileException {
            return divideWithCharacters(inputString, "+-");
        }
        private ArrayList<Integer> divideAtHighLevel(String inputString) throws ExpressionCompileException {
            return divideWithCharacters(inputString, "*");
        }
        private ArrayList<Integer> divideWithCharacters(String inputString, String characters) throws ExpressionCompileException {
            int depth = 0;                                          // ����Ƕ�����
            int index = 0;
            ArrayList<Integer> result = new ArrayList<>();
            result.add(0);
            while (index < inputString.length()) {
                if (characters.contains(inputString.substring(index, index + 1)) && depth == 0) {
                    if (index != 0)
                        result.add(index+1);
                }
                if (inputString.charAt(index) == '(')
                    depth++;
                if (inputString.charAt(index) == ')')
                    depth--;
                if (depth < 0)
                    throw new ExpressionCompileException("Brackets not match.");
                index++;
            }
            if (depth != 0)
                throw new ExpressionCompileException("Brackets not match.");
            return result;
        }
        public class StringTerm {                                 // ���������ŵ�����ʽ
            public String content;
            public int sign;                              // '\0'��ʾ��,'\1'��ʾ��
            StringTerm(String content, char sign) {
                this.content = content;
                this.sign = sign;
            }
            StringTerm() {
                this.content = "";
                this.sign = '\0';
            }
        }

    }
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
    public class ExpressionCompileException extends Exception{
        public ExpressionCompileException() {}
        public ExpressionCompileException(String msg) {
            super(msg);
        }
    }
}
