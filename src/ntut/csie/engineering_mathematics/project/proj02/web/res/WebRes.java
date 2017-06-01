package ntut.csie.engineering_mathematics.project.proj02.web.res;

import com.sun.istack.internal.Nullable;
import ntut.csie.engineering_mathematics.project.helper.Helper;
import ntut.csie.engineering_mathematics.project.proj02.config.App;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by s911415 on 2017/05/27.
 */
public class WebRes {
    public static InputStream GetResource(String path) {
        if(App.DEBUG){
            try {
                return new FileInputStream("D:\\xampp\\htdocs\\em_proj" + path);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return WebRes.class.getResourceAsStream(path);
    }

    @Nullable
    public static byte[] GetResourceAsByte(String path) {
        InputStream is = GetResource(path);
        if (is != null) {
            return Helper.streamToBytes(is);
        }
        return null;
    }

    @Nullable
    public static String GetResourceAsString(String path) {
        InputStream is = GetResource(path);
        if (is != null) {
            return Helper.streamToString(is);
        }
        return null;
    }
}
