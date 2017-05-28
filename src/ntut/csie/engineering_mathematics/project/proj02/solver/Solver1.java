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
public class Solver1 extends SolverAbstract {
    public final String R, L, C, I0, V0;

    public Solver1(MatlabEngine ml, String r, String l, String c, String i0, String v0) {
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

        SolODE("1", "R/L", "1/(L*C)", "1/L * Dy", I0, String.format("(0) = E(0) - R * %s - %s", I0, V0));
        ml.eval("i(t) = vpa(ySol, " + VPA_DOT + ");");
        ml.eval("syms v_R(t) v_L(t) v_C(t) i_R(t) i_L(t) i_C(t);");
        ml.eval("v_R(t) = (vpa(R * i, " + VPA_DOT + "));");
        ml.eval("v_L(t) = (vpa(L * diff(i), " + VPA_DOT + "));");
        ml.eval("v_C(t) = (vpa(E - v_R - v_L, " + VPA_DOT + "));");
        final String key[] = {"R", "L", "C"};

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