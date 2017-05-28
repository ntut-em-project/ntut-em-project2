package ntut.csie.engineering_mathematics.project.proj02;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import com.sun.istack.internal.Nullable;
import ntut.csie.engineering_mathematics.project.proj02.config.App;
import ntut.csie.engineering_mathematics.project.proj02.solver.*;
import ntut.csie.engineering_mathematics.project.proj02.web.server.WebServer;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static ntut.csie.engineering_mathematics.project.proj02.config.App.ITER_COUNT;
import static ntut.csie.engineering_mathematics.project.proj02.config.App.VPA_DOT;


public class Main {
    private static MatlabEngine ml = null;
    private static String PreRunCmd = null;

    @Nullable
    private static MatlabEngine GetMatlab() throws InterruptedException, EngineException {
        if (ml != null) return ml;
        ml = MatlabEngine.startMatlab();

        return ml;
    }

    public static void main(String[] args) throws Exception {
        WebServer server = new WebServer();
        GetMatlab();
        appendRoutes(server);

        server.start();
    }

    static void appendRoutes(final WebServer server) {
        server.addRoute("/SolODE", new WebServer.WebServerResponse() {
            @Override
            protected String response() throws Exception {
                Gson gson = GetGson();
                final Map<String, String> params = this.getParameters();
                String p, q, r, f, y0, yd0;
                p = params.get("p");
                q = params.get("q");
                r = params.get("r");
                f = params.get("f");
                y0 = params.get("y0");
                yd0 = params.get("yd0");

                Object res = SolODE(p, q, r, f, y0, yd0);

                return gson.toJson(String.valueOf(res));
            }

            @Override
            protected String getExt() {
                return "json";
            }
        });

        server.addRoute("/GetPts", new WebServer.WebServerResponse() {
            @Override
            protected String response() throws Exception {
                Gson gson = GetGson();
                final Map<String, String> params = this.getParameters();
                String func, start, end, step;
                func = params.get("func");
                start = params.get("start");
                end = params.get("end");
                step = params.get("step");

                Object res = GetFunctionPoints(func, start, end, step);

                return gson.toJson(res);
            }

            @Override
            protected String getExt() {
                return "json";
            }
        });

        server.addRoute("/PrepareVars", new WebServer.WebServerResponse() {
            @Override
            protected String response() throws Exception {
                Gson gson = GetGson();
                StringBuilder sb = new StringBuilder();
                PreRunCmd = null;
                final Map<String, String> params = this.getParameters();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (entry.getKey().replaceAll("\\d+", "").length() == 0) {
                        sb.append(entry.getValue() + ";");
                    } else {
                        sb.append(String.format("%s = %s;", entry.getKey(), entry.getValue()));
                    }
                }
                PreRunCmd = sb.toString();

                return gson.toJson(true);
            }

            @Override
            protected String getExt() {
                return "json";
            }
        });

        server.addRoute("/getPowerEQ", new WebServer.WebServerResponse() {
            @Override
            protected String response() throws Exception {
                Gson gson = GetGson();
                final Map<String, String> params = this.getParameters();

                return gson.toJson(getPowerEQ(params.get("type"), params.get("kind"), params.get("f"), params.get("pp"), params.get("dc")));
            }

            @Override
            protected String getExt() {
                return "json";
            }
        });

        server.addRoute("/getAlleq", new WebServer.WebServerResponse() {
            @Override
            protected String response() throws Exception {
                Gson gson = GetGson();
                final Map<String, String> params = this.getParameters();
                SolverInterface sol = getAllEqInstance(params);

                if (sol != null) {
                    return gson.toJson(sol.solve());
                }

                return gson.toJson(sol);
            }

            @Override
            protected String getExt() {
                return "json";
            }
        });
    }

    public static Object SolODE(String p, String q, String r, String f, String y0, String yd0)
            throws ExecutionException, InterruptedException {
        MatlabEngine ml = GetMatlab();
        PrepareMatlab(ml);
        if (p == null || p.isEmpty()) p = "1";
        if (q == null || q.isEmpty()) q = "1";
        if (r == null || r.isEmpty()) r = "1";
        if (y0 == null || y0.isEmpty()) y0 = "0";
        if (yd0 == null || yd0.isEmpty()) yd0 = "0";

        String cond1, cond2;
        cond1 = String.format("y(0) == (%s);", y0);
        cond2 = String.format("Dy(0) == (%s);", yd0);
        if (y0.startsWith("(")) {
            cond1 = y0.replace("=", "==");
            cond1 = String.format("y%s", cond1);
        }

        if (yd0.startsWith("(")) {
            cond2 = yd0.replace("=", "==");
            cond2 = String.format("Dy%s", cond2);
        }

        StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();
        {
            sb1.append(String.format("cond1 = %s;\n", cond1));
            sb2.append("cond1 ");
        }
        if (!p.equals("0")) {
            sb1.append(String.format("cond2 = %s;\n", cond2));
            sb2.append("cond2 ");
        }

        ml.eval("syms y(t);\n");
        ml.eval("Dy = diff(y);\n");
        ml.eval(String.format("ode = (%s) * diff(y, t, 2) + (%s) * diff(y, t) + (%s) * y == (%s);\n", p, q, r, f));
        ml.eval(sb1.toString());
        ml.eval("conds = [" + sb2.toString() + "];\n");

        ml.eval("ySol(t) = dsolve(ode, conds);\n");
        ml.eval("ySol = vpa(ySol, " + VPA_DOT + ");\n");
        ml.eval("ySol = simplify(ySol, " + App.SIMPLIFY_LIMIT + ")\n");
        ml.eval("ySolChar = char(ySol);\n");

        return ml.getVariable("ySolChar");
        // return null;
    }

    private static Object GetFunctionPoints(String func, String start, String end, String step)
            throws ExecutionException, InterruptedException {
        MatlabEngine ml = GetMatlab();
        PrepareMatlab(ml);
        ml.eval(String.format("if exist (\"%s\")>0,F=matlabFunction(%s),else,F=@(t) (%s),end;", func, func, func));
        ml.eval(String.format("input = [%s:%s:%s]';", start, step, end));
        ml.eval("result = arrayfun(F, input);");
        ml.eval("out = [input result];");

        return ml.getVariable("out");
    }

    public static Object getPowerEQ(String type, String kind, String f, String vpp, String vdc) throws ExecutionException, InterruptedException {
        MatlabEngine ml = GetMatlab();
        PrepareMatlab(ml);
        ml.eval("clearvars");
        ml.eval("syms x k t E(t) I(t);");
        ml.eval(String.format("f = %s;", f));
        ml.eval(String.format("in_pp = %s;", vpp));
        ml.eval(String.format("in_dc = %s;", vdc));
        ml.eval("in_m = in_pp / 2;");
        switch (kind) {
            case "1"://正弦波
                ml.eval(type + "(t) = in_dc + in_m * symfun(sin(2*pi*f*t), t);");
                break;
            case "2"://方波
                ml.eval(type + "(t) = in_dc + in_m * 4/pi*symfun(symsum((sin((2*k-1)*2*pi*f*t))/(2*k-1), k, 1, " + String.valueOf(ITER_COUNT) + "), t);");
                break;
            case "3"://三角波
                ml.eval(type + "(t) = in_dc + in_m * 8/pi^2*symfun(symsum((-1)^k*(sin((2*k+1)*t*2*pi*f))/(2*k+1)^2, k, 0, " + String.valueOf(ITER_COUNT) + "), t);");
                break;
            default:
                ml.eval(type + "(t) = 0 * t;");
        }

        ml.eval(type + " = vpa(" + type + ", " + VPA_DOT + ");");
        ml.eval(type + "_char = char(" + type + ");");

        return ml.getVariable(type + "_char");
    }

    @Nullable
    private static SolverInterface getAllEqInstance(Map<String, String> otherParams) throws EngineException, InterruptedException {
        MatlabEngine ml = GetMatlab();
        String
                type = otherParams.get("type"),
                R = otherParams.get("R"),
                L = otherParams.get("L"),
                C = otherParams.get("C"),
                vc0 = otherParams.get("vc0"),
                il0 = otherParams.get("il0");

        switch (type) {
            case "1":
                return new Solver1(ml, R, L, C, il0, vc0);
            case "2":
                return new Solver2(ml, R, L, C, il0, vc0);
            case "3":
                return new Solver3(ml, L, C, il0, vc0);
            case "4":
                return new Solver4(ml, R, C, vc0);
            case "5":
                return new Solver5(ml, R, C, vc0);
            case "6":
                return new Solver6(ml, R, L, il0);
            case "7":
                return new Solver7(ml, R, L, il0);
        }

        return null;
    }

    static void PrepareMatlab(MatlabEngine ml) throws ExecutionException, InterruptedException {
        // ml.eval("clearvars");
        if (PreRunCmd != null) {
            ml.eval(PreRunCmd);
        }
        PreRunCmd = null;
    }

    private static Gson g = null;

    static Gson GetGson() {
        if (g != null) return g;
        g = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create();

        return g;
    }
}

/*
syms x k t
f = 2

正弦波:
E_Sin = symfun(sin(2*pi*f*t), t)
fplot(E_Sin, [0, 2])

方波:
E_Squ=4/pi*symfun(symsum((sin((2*k-1)*2*pi*f*t))/(2*k-1), k, 1, 50), t)
fplot(E_Squ, [0, 2])

三角波:
E_Tan=8/pi^2*symfun(symsum((-1)^k*(sin((2*k+1)*t*2*pi*f))/(2*k+1)^2, k, 0, 50), t)
fplot(E_Tan, [0, 2])
 */