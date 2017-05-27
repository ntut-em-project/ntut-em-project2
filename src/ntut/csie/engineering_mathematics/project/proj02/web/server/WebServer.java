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
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by s911415 on 2017/05/27.
 */
public class WebServer {
    public static final int PORT = 5731;
    public static final String INDEX = "index.html";
    static HashMap<String, String> MimeTypes = null;


    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new MyHandler());
        server.start();
        System.out.println(String.format("Server Start at http://127.0.0.1:%d/", PORT));
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

}
