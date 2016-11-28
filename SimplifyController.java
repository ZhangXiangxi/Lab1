/**
 * Created by Xiangxi on 2016/11/27.
 */
public class SimplifyController {
    public SimplifyController() {
    }
    public Expression compile(String input) throws Exception{
        Expression expression = new Expression();
        expression.compile(input);
        return expression;
    }
}
