import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by Xiangxi on 2016/11/21.
 */
@RunWith(Parameterized.class)
public class SimplifyTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"d+7", "a+b+c+(d+a)", "a=1 b=2 c=3"},
                {"6*a+b", "a+b+3*a+2*a", ""},
                {"b-c+1.5", "a+b-c-2*a", "a=1.5"},
                {"14.4","(a+b+a*a+c)*b","a=1 b=2 c=3.2"},
                {"No such variable in the former expression.","a+b+c","a=1-b=2-c=3"}
        });
    }

    private String fExpcted;
    private String fExpression;
    private String fAssignments;

    public SimplifyTest(String expcted, String expression, String assignments) {
        fExpcted = expcted;
        fExpression = expression;
        fAssignments = assignments;
    }

    @org.junit.Test
    public void TestCompile() throws Exception{
        Expression expression = new Expression();
        try {
            expression.compile(fExpression);
            assertEquals(fExpcted,expression.derivate(fAssignments));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}