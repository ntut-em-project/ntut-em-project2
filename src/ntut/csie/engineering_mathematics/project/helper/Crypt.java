package ntut.csie.engineering_mathematics.project.helper;

import com.sun.istack.internal.Nullable;
import ntut.csie.engineering_mathematics.project.proj02.config.App;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * Created by s911415 on 2017/03/21.
 */
public class Crypt {

    @Nullable
    public static String sha256(String sha256) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] array = md.digest(sha256.getBytes(App.ENCODING));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
