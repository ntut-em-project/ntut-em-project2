package ntut.csie.engineering_mathematics.project.proj02.solver;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by s911415 on 2017/05/28.
 */
public interface SolverInterface {
    int getType();
    LinkedList<Map<String, String>> solve()  throws ExecutionException, InterruptedException ;
}
