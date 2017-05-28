package ntut.csie.engineering_mathematics.project.proj02.solver;

import com.mathworks.engine.MatlabEngine;
import ntut.csie.engineering_mathematics.project.proj02.config.App;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static ntut.csie.engineering_mathematics.project.proj02.config.App.VPA_DOT;

/**
 * Created by s911415 on 2017/05/28.
 */
public class Solver4 extends SolverAbstract {
    public final String R, C, V0;

    public Solver4(MatlabEngine ml, String r, String c, String v0) {
        super(ml);
        R = r;
        C = c;
        V0 = v0;
    }

    @Override
    public int getType() {
        return 4;
    }

    @Override
    public LinkedList<Map<String, String>> solve() throws ExecutionException, InterruptedException {
        LinkedList<Map<String, String>> ret = new LinkedList<>();

        prepareVar("R", R);
        prepareVar("C", C);

        SolODE("C * R", "1", "E", V0);
        ml.eval("syms v_R(t) v_C(t) i(t);");
        ml.eval("v_C(t) = vpa(ySol, " + VPA_DOT + ");");
        ml.eval("v_R(t) = simplify(vpa(E - v_C, " + VPA_DOT + "), " + App.SIMPLIFY_LIMIT + ");");
        ml.eval("i(t) = simplify(vpa(v_R / R, " + VPA_DOT + "), " + App.SIMPLIFY_LIMIT + ");");
        final String key[] = {"R", "C"};

        prepareVar("i_ch", "char(i)");
        putRet(ret, "i", ml.getVariable("i_ch"));

        for (final String k : key) {
            final String pk = "v_" + k + "_c";
            prepareVar(pk, "char(v_" + k + ")");
            putRet(ret, "v_" + k, ml.getVariable(pk));
        }

        return ret;
    }
}
