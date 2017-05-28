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
public class Solver7 extends SolverAbstract {
    public final String R, L, I0;

    public Solver7(MatlabEngine ml, String r, String l, String v0) {
        super(ml);
        R = r;
        L = l;
        I0 = v0;
    }

    @Override
    public int getType() {
        return 7;
    }

    @Override
    public LinkedList<Map<String, String>> solve() throws ExecutionException, InterruptedException {
        LinkedList<Map<String, String>> ret = new LinkedList<>();

        prepareVar("R", R);
        prepareVar("L", L);

        SolODE("L / R", "1", "I", I0);
        ml.eval("syms v_R(t) v_L(t) v(t);");
        ml.eval("i_L(t) = vpa(ySol, " + VPA_DOT + ");");
        ml.eval("i_R(t) = simplify(vpa(I - i_L, " + VPA_DOT + "), " + App.SIMPLIFY_LIMIT + ");");
        ml.eval("v(t) = simplify(vpa(i_R * R, " + VPA_DOT + "), " + App.SIMPLIFY_LIMIT + ");");
        final String key[] = {"R", "L"};

        prepareVar("v_ch", "char(v)");
        putRet(ret, "v", ml.getVariable("v_ch"));

        for (final String k : key) {
            final String pk = "i_" + k + "_c";
            prepareVar(pk, "char(i_" + k + ")");
            putRet(ret, "i_" + k, ml.getVariable(pk));
        }

        return ret;
    }
}
