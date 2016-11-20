import static org.junit.Assert.*;

/**
 * Created by Xiangxi and Yuanze on 2016/9/19.
 */
public class BracketReducerTest {

    @org.junit.Test
    public void testGetResult() throws Exception {
        BracketReducer bracketReducer = new BracketReducer("(2.5+a)*3.3");
        assertEquals("1*2.5*3.3+1*a*3.3", bracketReducer.toString());
    }

}

//
//
//