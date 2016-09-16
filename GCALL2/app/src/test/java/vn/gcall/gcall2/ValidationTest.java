package vn.gcall.gcall2;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import vn.gcall.gcall2.Helpers.Validation;

/**
 * Created by This PC on 20/07/2016.
 */
public class ValidationTest {
    @Test
    public void testValidate(){
        assertTrue("Invalid mail",Validation.validate("ab.c@gmail.com"));
        assertFalse(Validation.validate("abc@gmail"));
        assertFalse(Validation.validate("abc"));
        assertFalse(Validation.validate(""));
    }

    @Test
    public void testIsNotNull(){
        assertTrue(Validation.isNotNull("abc"));

        assertFalse(Validation.isNotNull(""));
        assertFalse(Validation.isNotNull(null));
    }

    @Test
    public void testCheckLength(){
        assertTrue(Validation.checkLength("12345",10,5));
        assertTrue(Validation.checkLength("123456",10,5));
        assertTrue(Validation.checkLength("1234567890",10,5));
        assertTrue(Validation.checkLength("asffasgsdf",20,6));

        assertFalse(Validation.checkLength("12345678901",10,5));
        assertFalse(Validation.checkLength("",2,3));

    }

    @Test
    public void testHashMD5(){
        String actual1="";
        try {
            actual1=Validation.hashMD5("123456");
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String expected="E10ADC3949BA59ABBE56E057F20F883E".toLowerCase();
        assertEquals("Hash md5 of '123456'",expected,actual1);
        //////////////////////////////////////////////////////////////////
        String actual2="";
        try {
            actual2=Validation.hashMD5("yousport");
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String expected2="085227fb151d8f15c300137a93ee94a5".toLowerCase();
        assertEquals("Hash md5 of '123456'",expected2,actual2);
        ////////////////////////////////////////////////////////////////////
        String actual3="";
        try {
            actual3=Validation.hashMD5("KingL4dy");
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String expected3="abd11a45f07c7cabc44c1213872e430b".toLowerCase();
        assertEquals("Hash md5 of '123456'",expected3,actual3);
        ////////////////////////////////////////////////////////////////////
        String actual4="";
        try {
            actual4=Validation.hashMD5("JULIANDONG");
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String expected4="51fad9aa714290e39751375df41ec7da".toLowerCase();
        assertEquals("Hash md5 of '123456'",expected4,actual4);
        ////////////////////////////////////////////////////////////////////
        String actual5="";
        try {
            actual5=Validation.hashMD5("quocanhdeptrai");
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String expected5="79b35893bfd45c8c718b67b145cd3b19".toLowerCase();
        assertEquals("Hash md5 of '123456'",expected5,actual5);

    }

    @Test
    public void testCheckPhone(){
        assertTrue(Validation.checkPhone("01234567890"));
        assertTrue(Validation.checkPhone("1234567890"));
        assertFalse(Validation.checkPhone("+12345678900"));
        assertFalse(Validation.checkPhone("1234567890000000"));
    }
}
