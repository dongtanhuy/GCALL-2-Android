package vn.gcall.gcall2.Helpers;


import android.util.Patterns;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by This PC on 28/04/2016.
 * Validate input that user enter before send to API
 */
public class Validation {
    private static Pattern pattern;
    private static Matcher matcher;


    /*
    * Validate email address with regex
    * @param email : String
    * @return TRUE for Valid Email, FALSE for Invalid Email
    * */
    public static boolean validate(String email){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
//        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        return email.matches(emailPattern);
    }

    /*
    * Check for null String object
    * @param text: String
    * @return TRUE for Not NUll, FALSE for Null String Object
    * */
    public static boolean isNotNull(String text){
        return text!=null && !text.isEmpty();
    }

    /*
    * Check length of text string
    * @param text: String
    * @param max_length: int
    * @param min_lenth: int
    * @return TRUE if text have valid length, ohterwise FALSE
    * */
    public static boolean checkLength(String text, int max_length, int min_length){
        if(text.length()>max_length || text.length()<min_length){
            return false;
        }
        return true;
    }

    public static boolean checkPhone(String phone){
        String phonePattern="\\d{10,11}";
        return phone.matches(phonePattern);
    }

    private static String convertedToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < data.length; i++) {
            int halfOfByte = (data[i] >>> 4) & 0x0F;
            int twoHalfBytes = 0;

            do {
                if ((0 <= halfOfByte) && (halfOfByte <= 9)) {
                    buf.append((char) ('0' + halfOfByte));
                } else {
                    buf.append((char) ('a' + (halfOfByte - 10)));
                }

                halfOfByte = data[i] & 0x0F;

            } while (twoHalfBytes++ < 1);
        }
        return buf.toString();
    }

    public static String hashMD5(String text) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] md5 = new byte[64];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        md5 = md.digest();
        return convertedToHex(md5);
    }
}
