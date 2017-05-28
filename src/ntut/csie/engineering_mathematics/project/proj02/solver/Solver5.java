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
public class Solver5 extends SolverAbstract {
    public final String R, C, V0;

    public Solver5(MatlabEngine ml, String r, String c, String v0) {
        super(ml);
        R = r;
        C = c;
        V0 = v0;
    }

    @Override
    public int getType() {
        return 5;
    }

    @Override
    public LinkedList<Map<String, String>> solve() throws ExecutionException, InterruptedException {
        LinkedList<Map<String, String>> ret = new LinkedList<>();

        prepareVar("R", R);
        prepareVar("C", C);

        SolODE("C", "1 / R", "I", V0);
        ml.eval("syms i_R(t) i_C(t) v(t);");
        ml.eval("v(t) = vpa(ySol, " + VPA_DOT + ");");
        ml.eval("i_R(t) = simplify(vpa(v / R, " + VPA_DOT + "), " + App.SIMPLIFY_LIMIT + ");");
        ml.eval("i_C(t) = simplify(vpa(I - i_R, " + VPA_DOT + "), " + App.SIMPLIFY_LIMIT + ");");
        final String key[] = {"R", "C"};

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
