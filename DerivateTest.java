/**
 * Created by Xiangxi on 2016/11/21.
 */
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class DerivateTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"0", "a-a", "a"},
                {"0", "a-a+b", "a"},
                {"1", "a", "a"}
        });
    }

    private String fExpcted;
    private String fExpression;
    private String fVariable;

    public DerivateTest(String expcted, String expression, String variable) {
        fExpcted = expcted;
        fExpression = expression;
        fVariable = variable;
    }

    @org.junit.Test
    public void TestCompile() throws Exception{
        Expression expression = new Expression();
        try {
            expression.compile(fExpression);
            assertEquals(fExpcted,expression.derivate(fVariable));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
