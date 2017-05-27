package ntut.csie.engineering_mathematics.project.proj02;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import com.sun.istack.internal.Nullable;
import ntut.csie.engineering_mathematics.project.proj02.web.server.WebServer;

import java.util.Map;
import java.util.concurrent.ExecutionException;


public class Main {
    private static MatlabEngine ml = null;

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
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
    }

    private static Object SolODE(String p, String q, String r, String f, String y0, String yd0)
            throws ExecutionException, InterruptedException {
        MatlabEngine ml = GetMatlab();
        ml.eval("clearvars");
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

        ml.eval("ySol(t) = dsolve(ode, conds)\n");
        ml.eval("ySol = simplify(ySol)\n");
        ml.eval("ySolChar = char(ySol);\n");

        return ml.getVariable("ySolChar");
        // return null;
    }

    private static Object GetFunctionPoints(String func, String start, String end, String step)
            throws ExecutionException, InterruptedException {
        MatlabEngine ml = GetMatlab();
        ml.eval("clearvars");
        ml.eval(String.format("F = @(t) (%s);", func));
        ml.eval(String.format("input = [%s:%s:%s]';", start, step, end));
        ml.eval("result = arrayfun(F, input);");
        ml.eval("out = [input result]");

        return ml.getVariable("out");
    }
}