package ntut.csie.engineering_mathematics.project.proj02.solver;

import com.mathworks.engine.MatlabEngine;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static ntut.csie.engineering_mathematics.project.proj02.config.App.SIMPLIFY_LIMIT;
import static ntut.csie.engineering_mathematics.project.proj02.config.App.VPA_DOT;

/**
 * Created by s911415 on 2017/05/28.
 */
public class Solver2 extends SolverAbstract {
    public final String R, L, C, I0, V0;

    public Solver2(MatlabEngine ml, String r, String l, String c, String i0, String v0) {
        super(ml);
        R = r;
        L = l;
        C = c;
        I0 = i0;
        V0 = v0;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public LinkedList<Map<String, String>> solve() throws ExecutionException, InterruptedException {
        LinkedList<Map<String, String>> ret = new LinkedList<>();

        prepareVar("R", R);
        prepareVar("L", L);
        prepareVar("C", C);

        SolODE("1", "1/(R*C)", "1/(L*C)", "1/L * Dy", V0, String.format("(0) = (I(0) - %s / R - %s) / C", V0, I0));
        ml.eval("v(t) = vpa(ySol, " + VPA_DOT + ");");
        ml.eval("syms v_R(t) v_L(t) v_C(t) i_R(t) i_L(t) i_C(t);");
        ml.eval("i_R(t) = simplify(vpa(v / R, " + VPA_DOT + "), " + SIMPLIFY_LIMIT + ");");
        ml.eval("i_C(t) = simplify(vpa(C * diff(v), " + VPA_DOT + ")" + SIMPLIFY_LIMIT + ");");
        ml.eval("i_L(t) = simplify(vpa(I - i_R - i_C, " + VPA_DOT + ")" + SIMPLIFY_LIMIT + ");");
        final String key[] = {"R", "L", "C"};

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
