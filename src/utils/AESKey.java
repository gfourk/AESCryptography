package utils;

import java.io.*;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

import model.StateHolder;

public class AESKey implements SecretKey {

	private static final Logger logger = Logger.getLogger(AESKey.class.getName());

	private byte[] key;
	private static final long serialVersionUID = 1L;

	/** 
	 * constructor
	 * copies the password to the key and fills with zeros if its less than size
	 * 
	 * @param size the size of the key
	 * @password the string of the user in bytes
	 * @throws NoSuchAlgorithmException
	 */
	public AESKey(int size, byte[] password) {
		size = size / 8;
		key = new byte[size];
		// if the password is too big
		// clip it down to size bytes
		if (password.length > size) {
			byte[] clippedPassword = new byte[size];
			for (int j = 0; j < size; j++)
				clippedPassword[j] = password[j];
			password = clippedPassword;
		}
		int i;
		for (i = 0; i < password.length; ++i) {
			key[i] = password[i];
		}
		for (; i < size; ++i) {
			key[i] = 0;
		}
	}

	/** overloaded constructor
	 * produces a random key using AES key generator
	 * @param size the size of the key
	 * @throws NoSuchAlgorithmException
	 */
	public AESKey(int size) throws NoSuchAlgorithmException {
		KeyGenerator aesKeygen = KeyGenerator.getInstance("AES");
		aesKeygen.init(size, new SecureRandom());
		SecretKey aesKey = aesKeygen.generateKey();
		key = aesKey.getEncoded();
	}

	/**
	 *  saves the key in bytes
	 * @param file
	 * @param state
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public void saveToFile(File file, StateHolder state)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException
	{

		// open the stream to write to
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {

			// encrypt the key using another the key for the key
			byte[] encrypted = work(state.getMode2(), state.getPadding2(), true, state.getKey2(), this.key);

			// first write the lenght of the (encrypted) key
			out.writeInt(encrypted.length);

			// and save it to the file
			for (int i = 0; i < encrypted.length; ++i)
				out.writeByte(encrypted[i]);

		}
	}

	/** 
	 * loads the key in bytes
	 * @param file
	 * @param state
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public void loadFromFile(File file, StateHolder state)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException
	{

		// open the stream
		try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {

			// read the length of the saved key
			int length = in.readInt();

			// allocate room for it
			byte[] encrypted = new byte[length];

			// and load it from the file
			for (int j = 0; j < length; j++)
				encrypted[j] = in.readByte();

			// now we have the key, decode it and save it in this.key
			try {
				this.key = work(state.getMode2(), state.getPadding2(), false, state.getKey2(), encrypted);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Cannot decrypt key from file", e);
			}
		}
	}

	/**
	 *  returns the security level of the password
	 * @param password
	 * @return
	 */
	public static int securityLevel(String password) {
		int digit = 0;
		int lower = 0;
		int upper = 0;
		int symbol = 0;
		int level = 0;
		char c;
		for (int i = 0; i < password.length(); ++i) {
			c = password.charAt(i);
			if (Character.isDigit(c)) {
				digit = 1;
			}
			if (Character.isLowerCase(c)) {
				lower = 1;
			}
			if (Character.isUpperCase(c)) {
				upper = 1;
			}
			if (c == '*' || c == '/' || c == '+' || c == '-' || c == '.' || c == ',' || c == '$' || c == '#' || c == '@'
					|| c == '%' || c == '^' || c == '&' || c == '(' || c == ')' || c == '[' || c == ']' || c == ';'
					|| c == ':' || c == '\'' || c == '\"' || c == '\\' || c == '?' || c == '<' || c == '>' || c == '|'
					|| c == '{' || c == '}' || c == '=' || c == '_' || c == '~' || c == '!') {
				symbol = 1;
			}
		}
		level = digit + lower + upper + symbol;
		return level;
	}

	/**
	 * 
	 * @param mode of operation of the algorithm e.g. ( "ECB" , "CBC" )
	 * @param padding mode ( NoPadding, PKCS7Padding, ZeroPadding )
	 * @param encrypt or decrypt mode
	 * @param key aes secret key
	 * @param text bytes of cleartext/ciphertext
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] work(String mode, String padding, boolean encrypt, SecretKey key, byte[] text)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{

		// make a Cipher object for AES with ECB mode and PKCS5 Padding,
		// using the Bouncycastle provider
		Cipher aesCipher = Cipher.getInstance("AES" + "/" + mode + "/" + padding, "BC");

		// init the cipher to encrypt/decrypt data using the AES key
		if (mode.equals("CBC")) {
			byte[] iv = new byte[16];
			for (int i = 0; i < 16; i++)
				iv[i] = key.getEncoded()[i];

			if (encrypt)
				aesCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
			else
				aesCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		} else {
			if (encrypt)
				aesCipher.init(Cipher.ENCRYPT_MODE, key);
			else
				aesCipher.init(Cipher.DECRYPT_MODE, key);
		}

		return aesCipher.doFinal(text);
	}

	@Override
	public String getAlgorithm() {
		return "AES";
	}

	@Override
	public byte[] getEncoded() {
		return key;
	}

	@Override
	public String getFormat() {
		return "RAW";
	}

}
