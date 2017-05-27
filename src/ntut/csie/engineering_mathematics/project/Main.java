package ntut.csie.engineering_mathematics.project;

import com.google.gson.Gson;
import com.sun.istack.internal.Nullable;
import ntut.csie.engineering_mathematics.project.helper.Helper;

import java.io.InputStream;

public class Main {

    public static void main(String[] args) {
        Gson gson = new Gson();
        int i = gson.fromJson("100", int.class);              //100
        // write your code here
        System.out.println(i);
    }
}
