package ntut.csie.engineering_mathematics.project.proj02.solver;

import com.mathworks.engine.MatlabEngine;
import ntut.csie.engineering_mathematics.project.proj02.Main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by s911415 on 2017/05/28.
 */
public abstract class SolverAbstract implements SolverInterface {
    protected final MatlabEngine ml;

    public SolverAbstract(MatlabEngine ml) {
        this.ml = ml;
    }

    protected static void SolODE(String p, String q, String r, String f, String y0, String yd0)
            throws ExecutionException, InterruptedException {
        Main.SolODE(p, q, r, f, y0, yd0);
    }

    protected void prepareVar(String var, String val) throws ExecutionException, InterruptedException {
        ml.eval(String.format("%s = %s;", var, val));
    }

    protected Map<String, String> putRet(LinkedList<Map<String, String>> ret, String k, Object v) {
        Map<String, String> r = new HashMap<>();
        r.put("key", k);
        r.put("value", v.toString());
        ret.add(r);
        return r;
    }
}
