import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
@RunWith(Parameterized.class)
public class PowerNotationReplacerTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"(a+b)*(a+b)*(a+b)", "(a+b)^3"},
                {"Gay*Gay*Gay", "Gay^3"},
                {"((a+b)*(a+b)*(a+b)+2a*a)*((a+b)*(a+b)*(a+b)+2a*a)", "((a+b)^3+2a^2)^2"}
        });
    }
    private String fInput;
    private String fExpected;

    public PowerNotationReplacerTest(String expected, String input) {
        fInput = input;
        fExpected = expected;
    }

    @Test
    public void testGetResult() throws Exception {
        PowerNotationReplacer powerNotationReplacer = new PowerNotationReplacer(fInput);
        String result = "";
        try {
            result = powerNotationReplacer.getResult();
        } catch (ExpressionCompileException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(fExpected, result);
    }
}