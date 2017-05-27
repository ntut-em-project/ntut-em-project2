package ntut.csie.engineering_mathematics.project.proj02;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import com.sun.istack.internal.Nullable;
import ntut.csie.engineering_mathematics.project.proj02.web.server.WebServer;


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

        server.start();
    }
}
