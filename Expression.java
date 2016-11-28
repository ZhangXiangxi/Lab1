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

    private String toString(ArrayList<Term> polynomial) {            // 将numericTerm形式存储的多项式转化为字符串输出
        if (polynomial.size() == 0)                                         // 若多项式不包含任何项，则输出"0"
            return "0";
        String result = transFromNumericTermToString(polynomial.get(0));
        for(int i = 1; i < polynomial.size(); i++) {
            String termString = transFromNumericTermToString(polynomial.get(i));
            if (!termString.startsWith("-"))                                // 若项前没有负号，则需要添加正号
                result += "+";
            result += termString;
        }
        return result;
    }
    private String blankStrip(String expression) {
        return expression.replaceAll("\\s+", "");                           // 先去除所有的空格
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
            for (String fragments : resultTermVariable.split("\\*"))               // 用乘号分隔开
                if (fragments.matches("[a-zA-Z]+") && !variableIndexToName.contains(fragments))
                    variableIndexToName.add(fragments);                     // 如果是未出现过的纯字母字串，则加入到变量名列表
        }
        for(int i = 0; i < variableIndexToName.size(); i++)
            variableList.put(variableIndexToName.get(i), i);                // 添加字串至词典
        variableNumber = variableIndexToName.size();
    }
    private void transformIntoNumeric() throws ExpressionCompileException{
        for(int i = 0; i < resultTermVariables.size(); i++) {
            ArrayList<Integer> powers = new ArrayList<>();
            for(int j = 0; j < variableNumber; j++)
                powers.add(0);                                               // 幂计数初始化
            double coefficient = 1.0d;                                          // 系数初始化，对于负项处理为-1
            if (resultTermSigns.get(i) == 0)
                coefficient *= -1;
            for(String fragments : resultTermVariables.get(i).split("\\*")) {                 // 对于乘号分隔开的每一部分
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
                if (formerTerm.powers.equals(latterTerm.powers)) {              // 若有相邻项幂指数相同, 选择合并
                    double coefficientSum = formerTerm.coefficient + latterTerm.coefficient;
                    if (Math.abs(coefficientSum) < numericError) {              // 若系数和为0，则删去两项，否则用一项代替原来的两项
                        inputCompileResults.remove(i + 1);
                        inputCompileResults.remove(i);
                    } else {
                        inputCompileResults.set(i, new Term(coefficientSum, latterTerm.powers));
                        inputCompileResults.remove(i + 1);
                    }
                    modified = true;                                            // 记录此次的合并
                    break;                                                      // 数组序列结构已被破坏，应进行下一轮排序和循环
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
            result = Long.toString(Math.round(term.coefficient));               // 当系数（近似）为整数时，以整数形式打印
        else
            result = Double.toString(term.coefficient);                         // 否则以小数形式打印
        for(int i = 0; i < variableNumber; i++) {
            int power = term.powers.get(i);                                     // 该位置的对应指数
            if (power > 0)
                result += "*" + variableIndexToName.get(i);                     // 指数非零则添加变量名
            if (power > 1)
                result += "^" + Integer.toString(power);                        // 指数大于1则添加幂次
        }
        if (result.startsWith("1*"))
            return result.substring(2);                                         // 若为1*a形式，则省略前面的1
        return result;
    }

    public boolean isCompiled() {
        return compiledMark;
    }
    public boolean hasVariable(String variable) {
        return variableIndexToName.contains(variable);                      // 即检测该变量是否存在于变量列表中
    }
    public String derivate(String variable) {
        int variableIndex = variableList.get(variable);                 // 待求导变量在变量列表中的标号
        ArrayList<Term> derivedResult = new ArrayList<>();       // 求导结果多项式的NumericTerm形式表达
        ArrayList<Integer> tempPowers = new ArrayList<>();              // 各变量幂指数暂存列表

        for (Term term : compileResults) {
            int variablePower = term.powers.get(variableIndex);         // 对原多项式每一项中待求导变量的幂次
            if (variablePower > 0 ) {                                   // 当幂次为正，可以求得导数项，存入result中
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
        for(String fragments : assignments.split("[=| ]")) {                // 用等号与空格进行分割
            if (fragments.equals(""))                                       // 若截取出的是空串，则不应进行任何处理
                continue;
            if (isVariable) {                                               // 若此处应为变量名
                if (variableArray.contains(fragments))
                    return "There're multiple variables " + fragments +"."; // 检测变量重复赋值错误
                variableArray.add(fragments);
                isVariable = false;
            } else {                                                        // 若此处应为数值
                try{
                    valueArray.add(Double.parseDouble(fragments));
                } catch (NumberFormatException e) {                         // 检测实数格式错误
                    return "The substring " + fragments + " cannot be parsed into a real number.";
                }
                isVariable = true;
            }
        }
        if (variableArray.size() != valueArray.size())                      // 检测变量个数与数值个数不匹配的错误
            return "The number of variables and values don't match. ";
        for (String variable : variableArray)                               // 检测变量不包含在原表达式的错误
            if (!variableIndexToName.contains(variable))
                return "No such variable in the former expression.";
        ArrayList<Term> simplifiedResult = new ArrayList<>();// 化简结果多项式的NumericTerm形式表达
        for(Term originalTerm : compileResults) {                    // 对于原多项式中的每一项
            ArrayList<Integer> tempPowers = new ArrayList<>(originalTerm.powers);
                                                                            // 各变量幂指数暂存列表
            double tempCoefficient = originalTerm.coefficient;              // 系数暂存
            for (int i = 0; i < variableArray.size(); i++) {                // 对于赋值列表中的每一组赋值
                int variableIndex = variableList.get(variableArray.get(i)); // 取得变量标号
                int variablePower = tempPowers.get(variableIndex);          // 取得变量幂次
                double variableValue = valueArray.get(i);                   // 取得变量被赋值
                tempCoefficient *= Math.pow(variableValue, variablePower);  // 计算新系数
                tempPowers.set(variableIndex, 0);                           // 消除原变量
            }
            if (Math.abs(tempCoefficient) >= numericError)                  // 系数为零的项自动消除
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

        private ArrayList<StringTerm> breakAtPlusAndSub(String exp) throws ExpressionCompileException{   // 对一个由加减连接各个项的式子进行处理
            ArrayList<StringTerm> rel = new ArrayList<>();
            ArrayList<Integer> notation = divideAtLowLevel(exp);
            if (exp.charAt(0) == '-')
                return breakAtPlusAndSub("0" + exp);
            int len = notation.size();
            if (divideWithCharacters(exp,"+-*").size() == 1 && exp.charAt(0) == '(')            // 如果整个表达式由一个括号包络
                return breakAtPlusAndSub(exp.substring(1, exp.length() - 1));                     // 就剥去括号再进行函数操作
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
            if (divideWithCharacters(exp,"+-*").size() == 1 && exp.charAt(0) == '(')               // 如果整个表达式由一个括号包络
                return breakAtMultiplification(exp.substring(1, exp.length() - 1));                // 就剥去括号再进行函数操作
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
            int depth = 0;                                          // 括号嵌套深度
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
        public class StringTerm {                                 // 不包含括号的联乘式
            public String content;
            public int sign;                              // '\0'表示负,'\1'表示正
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
            while (expression.contains("^")) {                                          // 当表达式中仍存在幂符号，进行处理
                int notionIndex = expression.indexOf("^");                              // 找到第一个幂符号的位置
                String stringAfterNotion = expression.substring(notionIndex+1);         // 幂符号后的子串
                if (!stringAfterNotion.matches("^[\\d]+.*"))                            // 如果其后的子串不是以数字开头，则表达式不合法
                    throw new ExpressionCompileException("A single natural number expected behind the power notion.");
                String powerString = stringAfterNotion.split("\\D")[0];
                int power = Integer.parseInt(powerString);                              // 获取这个幂符号后的幂次
                if (power < 1)                                                          // 如果其后的子串数字不是正整数，则表达式不合法
                    throw new ExpressionCompileException("A single natural number expected behind the power notion.");
                int startIndex;                                                         // 幂符号支配的部分的起始位置
                try {
                    startIndex = findRepeatPosition(expression.substring(0, notionIndex));
                } catch (ExpressionCompileException e) {
                    throw e;
                }
                String formerFragment = expression.substring(0, startIndex);            // 前部子串
                String middleFragment = expression.substring(startIndex, notionIndex);  // 中部子串，即需要重复的部分
                String latterFragment = expression.substring(notionIndex+1+powerString.length());
                // 幂符号位置右移一位再移除幂数字占位，得到后部子串
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
        // 找到子字符串中应当被幂符号重复的部分
        private int findRepeatPosition(String inputString) throws ExpressionCompileException {
            try{
                if (inputString.endsWith(")"))                                          // 若幂次前是括号
                    return findLastMatchBracket(inputString);
                if (inputString.substring(inputString.length()-1).matches("[a-zA-Z]"))       // 若幂次前是变量
                    return findLastMatchAlphabet(inputString);
                throw new ExpressionCompileException("Variables expected before power notion.");
            } catch(ExpressionCompileException e) {
                throw e;
            }
        }

        private int findLastMatchBracket(String inputString) throws ExpressionCompileException {
            int index = inputString.length()-1;                                         // 指标定位到最后一个括号处
            int depth = 0;                                                              // 括号嵌套深度此时为1
            do {
                if (index == -1)                                                        // 穷尽字符串而未能找到匹配位置，说明输入不合法
                    throw new ExpressionCompileException("Brackets not match.");
                if (inputString.charAt(index) == ')')
                    depth++;
                if (inputString.charAt(index) == '(')
                    depth--;
                index--;                                                                // 指针左移
            } while(depth != 0 || inputString.charAt(index+1) != '(');
            return index+1;
        }

        private int findLastMatchAlphabet(String inputString) {
            int index = inputString.length()-1;
            while (Character.isLetter(inputString.charAt(index))) {                     // 找到第一个非字母字符的位置
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
