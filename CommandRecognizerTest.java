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
public class CommandRecognizerTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"abc", InputType.Expression, "abc"},
                {"!end", InputType.End, ""},
                {"!d/d liyuanze", InputType.Derivation, "liyuanze"},
                {"!simplifY yuanze = gay", InputType.Simplification, "yuanze = gay"},
                {"!simplify yuanze = gay XX = streight", InputType.Simplification, "yuanze = gay XX = streight"},
                {" 1 + 2^3 * a * b * end - 7 + Drivation    ", InputType.Expression, " 1 + 2^3 * a * b * end - 7 + Drivation    "},
                {"$@%!Dgasdf!@+     @", InputType.Unrecognised, ""},
                {"2*(3+Yuanze)^5    ", InputType.Expression, "2*(3+Yuanze)^5    "}
        });
    }
    private String fInput;
    private InputType fTypeExpected;
    private String fOperandExpected;

    public CommandRecognizerTest(String input, InputType typeExpected, String operandExpected) {
        fInput = input;
        fTypeExpected = typeExpected;
        fOperandExpected = operandExpected;
    }
    @Test
    public void testRecognise() throws Exception {
        CommandRecognizer commandRecognizer = new CommandRecognizer();
        commandRecognizer.recognise(fInput);
        assertEquals(fTypeExpected, commandRecognizer.inputType);
        assertEquals(fOperandExpected, commandRecognizer.operand);
    }
}