import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Xiangxi and Yuanze on 2016/9/19.
 */
public class Term {
    public Term(){
        powers = new ArrayList<>();
    }
    public Term(double coefficient, Collection<Integer> powers) {
        this.coefficient = coefficient;
        this.powers = new ArrayList<>();
        this.powers.addAll(powers);
    }

    public double coefficient;
    public ArrayList<Integer> powers;
}
