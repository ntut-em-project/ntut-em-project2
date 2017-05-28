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

        server.addRoute("/PrepareVars", new WebServer.WebServerResponse() {
            @Override
            protected String response() throws Exception {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
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

        server.addRoute("/getEeq", new WebServer.WebServerResponse() {
            @Override
            protected String response() throws Exception {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                final Map<String, String> params = this.getParameters();

                return gson.toJson(getEeq(params.get("kind"), params.get("f"), params.get("vpp"), params.get("vdc")));
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

        ml.eval("ySol(t) = dsolve(ode, conds)\n");
        ml.eval("ySol = simplify(ySol)\n");
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
        ml.eval("out = [input result]");

        return ml.getVariable("out");
    }

    private static Object getEeq(String kind, String f, String vpp, String vdc) throws ExecutionException, InterruptedException {
        MatlabEngine ml = GetMatlab();
        PrepareMatlab(ml);
        ml.eval("syms x k t;");
        ml.eval(String.format("f = %s;", f));
        ml.eval(String.format("Vpp = %s;", vpp));
        ml.eval(String.format("Vdc = %s;", vdc));
        ml.eval("Vm = Vpp / 2;");
        switch (kind) {
            case "1"://正弦波
                ml.eval("E = Vdc + Vm * symfun(sin(2*pi*f*t), t);");
                break;
            case "2"://方波
                ml.eval("E = Vdc + Vm * 4/pi*symfun(symsum((sin((2*k-1)*2*pi*f*t))/(2*k-1), k, 1, 50), t);");
                break;
            case "3"://三角波
                ml.eval("E = Vdc + Vm * 8/pi^2*symfun(symsum((-1)^k*(sin((2*k+1)*t*2*pi*f))/(2*k+1)^2, k, 0, 50), t);");
                break;
            default:
                ml.eval("E = 0 * t;");
        }

        ml.eval("Ec = char(E);");

        return ml.getVariable("Ec");
    }

    static void PrepareMatlab(MatlabEngine ml) throws ExecutionException, InterruptedException {
        // ml.eval("clearvars");
        if (PreRunCmd != null) {
            ml.eval(PreRunCmd);
        }
        PreRunCmd = null;
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