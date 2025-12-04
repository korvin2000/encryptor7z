package com.example.encryption;

import org.apache.commons.io.FilenameUtils;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Security;

public class Encryptor {

	static final int ALG_TYPE_BYTES = 1;
	static final int NAME_LENGTH_BYTES = 2;
	static final int FILE_LENGTH_BYTES = 4;
	static final int HEADER_BYTES = ALG_TYPE_BYTES  + FILE_LENGTH_BYTES;
	static final byte ALGORITHM_AES_128_ECB_NP = (byte)1;

	static final Charset utf8;
	public static final String ALGORITHM_TYPE = "AES_128/ECB/NoPadding";
	static String password;

	static Key pbeKey;
	static {
		utf8 = StandardCharsets.UTF_8;
		Security.setProperty("crypto.policy", "unlimited");
	}

	public static void setPassword(String pwd) {
		password = pwd;
	}

	public static Cipher getEncryptionCipher() {
		return 	getCipher(true, password);
	}

	public static Cipher getDecryptionCipher() {
		return 	getCipher(false, password);
	}

	private static Cipher getCipher(boolean mode, String pwd) {
		Cipher cipher = null;
		try {
			if (null == pwd) throw new IllegalArgumentException("Password is not set");
			cipher = Cipher.getInstance(ALGORITHM_TYPE);
			pbeKey = CipherKey.getPBEKey(pwd);
			cipher.init(mode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, pbeKey);
		} catch (Exception e) {
			System.out.println("Error initializing Cipher");
			e.printStackTrace();
		}
		return cipher;
	}

	private static ByteBuffer header(byte[] name) {
		ByteBuffer bb  = ByteBuffer.allocateDirect(NAME_LENGTH_BYTES + name.length);
		bb.put(ByteUtils.UIntToByte2(name.length));
		bb.put(name);
		bb.flip();
		return bb;
	}


	public static ByteBuffer encrypt(Cipher cipher, FileStruct struct) {
		ByteBuffer inputData = struct.data();
		if (inputData == null || !inputData.hasRemaining()) return inputData;
		try {
			byte[] encName = FilenameUtils.getName(struct.name()).getBytes(utf8);
			int fileSize = inputData.remaining();
			int inputSize = fileSize + NAME_LENGTH_BYTES + encName.length;

			int validSize = getValidSize(inputSize);
			if (inputSize != validSize) {
				encName = extend(encName, (validSize-inputSize));
				inputSize = validSize;
			}

			ByteBuffer output = ByteBuffer.allocate(HEADER_BYTES+cipher.getOutputSize(inputSize));
			output.putInt(fileSize);
			output.put(Encryptor.ALGORITHM_AES_128_ECB_NP);
			cipher.update(header(encName), output);
			cipher.doFinal(inputData, output);

			output.rewind();
			return output;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static FileStruct decrypt(Cipher cipher, ByteBuffer input) {
		if (input == null || !input.hasRemaining()) return new FileStruct(input, null);
		try {

			int fileSize = input.getInt();
			byte algType = input.get();
			int inputSize = input.remaining();

			ByteBuffer output = ByteBuffer.allocate(cipher.getOutputSize(inputSize));
			cipher.doFinal(input, output);

			output.flip();
			byte[] len = new byte[2];
			output.get(len, 0, 2);
			int length = ByteUtils.Byte2ToUInt(len);
			byte[] name = new byte[length];
			output.get(name, 0, length);

			output = output.slice();
			return new FileStruct(output, asString(name));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static class FileStruct {

		private String name;
		private ByteBuffer data;

		public FileStruct(ByteBuffer fData, String fName) {
			this.data = fData;
			this.name = fName;
		}

		public String name() {
			return this.name;
		}

		public ByteBuffer data() {
			return this.data;
		}

		public int size() {
			return data.remaining();
		}

	}

	static int getValidSize(int x) {
		return ((x+15) >> 4) << 4;
	}

	static byte[] extend(byte[] input, int increment) {
		byte[] arr = new byte[input.length+increment];
		System.arraycopy(input, 0, arr, 0, input.length);
		return arr;
	}

	static String asString(byte[] byteString) {
		int len;
		if (null != byteString && (len = byteString.length)!=0)
			while (len > 0) if (byteString[--len]!=0) {
				return new String(byteString, 0, (++len), utf8);
			}
		return null;
	}

}
