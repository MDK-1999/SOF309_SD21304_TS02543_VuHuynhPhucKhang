package com.pcshop.util;
import at.favre.lib.crypto.bcrypt.BCrypt;
public class BCryptUtil {
    public static String hash(String plain){ return BCrypt.withDefaults().hashToString(10, plain.toCharArray()); }
    public static boolean verify(String plain, String hashed){ return BCrypt.verifyer().verify(plain.toCharArray(), hashed).verified; }
}
