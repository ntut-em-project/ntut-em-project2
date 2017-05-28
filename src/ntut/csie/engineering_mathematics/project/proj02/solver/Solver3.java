package ntut.csie.engineering_mathematics.project.proj02.solver;

import com.mathworks.engine.MatlabEngine;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static ntut.csie.engineering_mathematics.project.proj02.Main.getPowerEQ;

/**
 * Created by s911415 on 2017/05/28.
 */
public class Solver3 extends Solver1 {

    public Solver3(MatlabEngine ml, String l, String c, String i0, String v0) {
        super(ml, "0", l, c, i0, v0);
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public LinkedList<Map<String, String>> solve() throws ExecutionException, InterruptedException {
        getPowerEQ("E", "-1", "1", "0", "0");
        return super.solve();
    }
}
