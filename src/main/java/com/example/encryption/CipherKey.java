package com.example.encryption;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class CipherKey {

	public CipherKey() {}

	final static String pwd_salt = "test";
	final static int keySize = 16;

	public static Key getPBEKey(String password) {
		try {
			// Get the salt
			byte[] saltBytes = pwd_salt.getBytes("UTF-8"); // Convert bytes to string

			// Derive the key
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 1024, keySize * 8);

			return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}


}
