package ntut.csie.engineering_mathematics.project.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ntut.csie.engineering_mathematics.project.proj02.config.App;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by s911415 on 2017/05/27.
 */
public class Helper {

    public static String toJSON(Object obj) {
        return toJSON(obj, obj.getClass());
    }

    public static <T> String toJSON(Object obj, Class<T> model) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(obj, model);
    }

    public static Object parseJSON(String jsonStr, Class c) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonStr, c.getClass());
    }


    public static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final int BUFFER_SIZE = 8192;
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        try {
            while ((count = is.read(data, 0, BUFFER_SIZE)) != -1)
                outStream.write(data, 0, count);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outStream.toByteArray();
    }

    public static String streamToString(InputStream is) {
        final byte[] data = streamToBytes(is);

        try {
            return new String(data, App.ENCODING);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
