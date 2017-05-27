package ntut.csie.engineering_mathematics.project.proj02.web.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ntut.csie.engineering_mathematics.project.helper.Helper;
import ntut.csie.engineering_mathematics.project.proj02.config.App;
import ntut.csie.engineering_mathematics.project.proj02.web.res.WebRes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by s911415 on 2017/05/27.
 */
public class WebServer {
    public static final int PORT = 5731;
    public static final String INDEX = "index.html";
    static HashMap<String, String> MimeTypes = null;

    private final HttpServer server;

    public WebServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new MyHandler());
    }

    public void start() {
        server.start();
        System.out.println(String.format("Server Start at http://127.0.0.1:%d/", PORT));
    }

    /**
     * Add custom route
     * <p>
     * Example:
     * <pre>
     * server.addRoute("/test", new WebServer.WebServerResponse() {
     *     public String response() {
     *         return "1234";
     *     }
     *
     *     public String getExt() {
     *       return "json";
     *     }
     * });
     * </pre>
     *
     * @param path path
     * @param resp resp
     */
    public void addRoute(String path, WebServerResponse resp) {
        server.createContext(path, resp);
    }

    static HashMap<String, String> getMimeTypes() {
        if (MimeTypes != null) return MimeTypes;

        MimeTypes = new HashMap<>();
        String s = Helper.streamToString(WebServer.class.getResourceAsStream("mime.types"));
        String lines[] = s.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#") || line.length() < 2) continue;

            String t[] = line.split("\\s+");
            final String mime = t[0];
            for (int i = 1; i < t.length; i++) {
                String ext = t[i].trim().toLowerCase();
                if (ext.isEmpty()) continue;

                MimeTypes.put(ext, mime);
            }
        }


        return MimeTypes;
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            final HashMap<String, String> MINE = getMimeTypes();
            String path = t.getRequestURI().getPath().substring(1);
            if (path.isEmpty()) path = INDEX;
            byte[] data = WebRes.GetResourceAsByte(path);
            int code = 200;
            Headers hs = t.getResponseHeaders();
            hs.add("Accept-Ranges", "none");
            hs.add("Connection", "close");
            hs.add("Pragma", "no-cache");

            if (data == null) {
                code = 404;
                data = "404".getBytes(App.ENCODING);
            } else {

                String[] tmp = path.split("\\.");
                String ext = tmp[tmp.length - 1].toLowerCase();
                code = 200;
                if (MINE.containsKey(ext)) {
                    String ct = MINE.get(ext);
                    if (ct.startsWith("text/")) {
                        ct += ";charset=" + App.ENCODING;
                    }

                    hs.add("Content-Type", ct);
                }
            }
            hs.add("Content-Length", String.valueOf(data.length));
            t.sendResponseHeaders(code, data.length);
            OutputStream os = t.getResponseBody();
            os.write(data);
            os.close();
        }
    }

    public abstract static class WebServerResponse implements HttpHandler {
        private static final String P_KEY = "__parameters";
        protected HttpExchange httpExchange = null;

        protected abstract String response() throws Exception;

        protected String getExt() {
            return "txt";
        }

        protected LinkedHashMap<String, String> getParameters() {
            LinkedHashMap<String, String> ret = (LinkedHashMap<String, String>) httpExchange.getAttribute(P_KEY);

            if (ret != null) {
                return ret;
            } else {
                ret = new LinkedHashMap<>();
            }

            switch (httpExchange.getRequestMethod().toUpperCase()) {
                case "GET":
                    parseGetParameters(ret);
                    break;
                case "POST":
                    parsePostParameters(ret);
                    break;
            }

            httpExchange.setAttribute(P_KEY, ret);

            return ret;
        }

        private void parseGetParameters(LinkedHashMap<String, String> ret) {
            String query = httpExchange.getRequestURI().getRawQuery();
            parseQuery(query, ret);
        }

        private void parsePostParameters(LinkedHashMap<String, String> ret) {
            String query = Helper.streamToString(httpExchange.getRequestBody());
            parseQuery(query, ret);
        }

        private void parseQuery(String query, LinkedHashMap<String, String> ret) {
            try {
                if (query != null) {
                    String pairs[] = query.split("[&]");

                    for (String pair : pairs) {
                        String param[] = pair.split("[=]");

                        String key = null;
                        String value = null;
                        if (param.length > 0) {
                            key = URLDecoder.decode(param[0], App.ENCODING);
                        }

                        if (param.length > 1) {
                            value = URLDecoder.decode(param[1], App.ENCODING);
                        }

                        ret.put(key, value);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            int code = 200;

            this.httpExchange = t;
            t.setAttribute(P_KEY, null);

            byte[] data;
            try {
                data = response().getBytes(App.ENCODING);
            } catch (Exception e) {
                e.printStackTrace();
                code = 500;
                data = e.getMessage().getBytes(App.ENCODING);
            }
            Headers hs = t.getResponseHeaders();
            hs.add("Accept-Ranges", "none");
            hs.add("Connection", "close");
            hs.add("Pragma", "no-cache");
            String ct = getMimeTypes().get(getExt());
            if (ct.startsWith("text/")) {
                ct += ";charset=" + App.ENCODING;
            }

            hs.add("Content-Type", ct);

            hs.add("Content-Length", String.valueOf(data.length));
            t.sendResponseHeaders(code, data.length);
            OutputStream os = t.getResponseBody();
            os.write(data);
            os.close();
        }
    }
}
