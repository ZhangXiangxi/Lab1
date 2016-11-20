import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by Xiangxi and Yuanze on 2016/9/19.
 */
@RunWith(Parameterized.class)
public class ExpressionTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"a^2+2*a*b+b^2", "(a+b)^2"},
                {"a+b", "a+b"},
                {"a+3*b", "a+3b"},
                {"a*b*c+2*b*c*e+2*b*e*d","a*b*c+2b(c+d)e"},
                {"b^2*a*c+b^2*d+b*a*c^2+b*a+b*d*c+a*c","(b+c)(a+b(d+c*a))"},
                {"a^2*c*d-1*a*b*c*d-1*a*b","-a*(b+c*d(-a+b))"},
                {"-1*a^2-6*a*b-9*b^2","-(a+3b)^2"}
        });
    }

    private String fExpcted;
    private String fInput;

    public ExpressionTest(String expcted, String input) {
        fExpcted = expcted;
        fInput = input;
    }

    @org.junit.Test
    public void TestCompile() throws Exception{
        Expression expression = new Expression();
        try {
            expression.compile(fInput);
            assertEquals(fExpcted,expression.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}