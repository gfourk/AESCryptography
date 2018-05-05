package model;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.AESKey;

public class StateHolder {

	private static final Logger logger = Logger.getLogger(StateHolder.class.getName());

	private String userName;
	private String otherName;

	// the port and ip outgoing packets go to
	private String remoteIp;
	private int remotePort;

	// the port our server listens to
	private int localPort;

	// ----- Encryption settings

	private String mode;
	private String mode2;
	private String padding;
	private String padding2;

	private AESKey key;
	private AESKey key2;
	private String password;
	private String password2;
	private int keyLength;
	private int keyLength2;

	// ----- random serial number
	private int serialNo;

	// the next expected serial
	private int lastRemoteSerial;

	/**
	 * constructor
	 */
	public StateHolder() {
		this.userName = "Me";
		this.otherName = "Other";

		this.remoteIp = "localhost";
		this.remotePort = 12345;
		this.localPort = 12345;

		this.mode = "ECB";
		this.mode2 = "ECB";
		this.padding = "PKCS5Padding";
		this.padding2 = "PKCS5Padding";
		this.keyLength = 128;
		this.keyLength2 = 128;

		// create new random key of 256 bits
		try {
			this.key = new AESKey(this.keyLength);
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		// create new random key for the key of 256 bits
		try {
			this.key2 = new AESKey(this.keyLength2);
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		this.password = null;
		this.password2 = null;

		// serial number, initialized to a random positive num
		// always a random number bellow 50000
		serialNo = (Math.abs(new Random().nextInt()))%50000;

		// the last received serial number is set to -1
		// and is set to the actual received on the first message
		this.lastRemoteSerial = -1;
	}

	/**
	 *  returns an html header for the Message in the text area
	 * @return
	 */
	public String getOutgoingMessageHead() {
		Date d = new Date();
		return "<p bgcolor=\"#4169E1\"><b>" + this.userName + " : " + d.toString() + " (SN: " + this.serialNo
				+ ")</p></b>";
	}

	/** returns an html header for the Message in the text area
	 *  with the date and time contained in the message itself
	 * @param date
	 * @param serial
	 * @return
	 */
	public String getIncommingMessageHead(String date, String serial) {
		return "<p bgcolor=\"#00FF00\"><b>" + this.otherName + " : " + date + " (SN: " + serial + ")</p></b>";
	}

	/** returns the next serial number
	 *  as a 5 char String
	 * @return
	 */
	public String getSerial() {
		String ret = Integer.toString(this.serialNo % 100000);
		while (ret.length() < 5)
			ret = " " + ret;
		return ret;
	}

	/** returns a new String with the current date and time
	 *  the length is 34 chars
	 * @return
	 */
	public String getDate() {
		String ret = new Date().toString();
		while (ret.length() != 34)
			ret = " " + ret;
		return ret;

	}

	/** returns the checksum of the message
	 *  as a 5 char String
	 * @param msg
	 * @return
	 */
	private String makeChecksum(String msg) {

		int checksum = 0;
		for (int i = 0; i < msg.length(); i++) {
			checksum += msg.charAt(i);
		}
		String ret = Integer.toString(checksum % 100000);
		while (ret.length() < 5)
			ret = " " + ret;
		return ret;

	}

	private int intMakeChecksum(String msg) {

		int checksum = 0;
		for (int i = 0; i < msg.length(); i++) {
			checksum += msg.charAt(i);
		}
		return checksum % 100000;
	}

	/**
	 *  creates a message
	 * @param userIn
	 * @return
	 */
	public String createMessage(String userIn) {
		String ret = "";

		// fisrt the serial number
		this.serialNo++;
		ret += this.getSerial();

		// now get the date and time
		ret += this.getDate();

		// now get the checksum
		ret += this.makeChecksum(userIn);

		// and finally append the actual message
		ret += userIn;

		// and now return
		return ret;
	}

	/** pasres a message and returns an array with its fields
	 * after it has checked all of the fields for correctness
	 * ret[0] = serial number
	 * ret[1] = date and time
	 * ret[2] = message
	 * @param receivedMsg
	 * @return
	 */
	public String[] parseMessage(String receivedMsg) {

		// the return array
		String[] ret = new String[3];

		// the first 5 chars are the serial number
		int serial;
		try {
			logger.info(receivedMsg);
			serial = Integer.parseInt(receivedMsg.substring(0, 5).trim());
		} catch (NumberFormatException n1) {
			return new String[0];
		}

		if (this.lastRemoteSerial == -1)
			this.lastRemoteSerial = serial;
		else if (serial != (this.lastRemoteSerial + 1)) {
			logger.info("Message rejected because the Serial number is wrong");
			return new String[0];
		} else
			this.lastRemoteSerial++;

		ret[0] = receivedMsg.substring(0, 5);

		// the next 34 chars are the date and time
		ret[1] = receivedMsg.substring(5, 39);

		int checksum;
		try {
			checksum = Integer.parseInt(receivedMsg.substring(39, 44).trim());
		} catch (NumberFormatException f) {
			return new String[0];
		}

		if (checksum != this.intMakeChecksum(receivedMsg.substring(44))) {
			logger.info("Message rejected because the CheckSum is wrong");
			return new String[0];
		}

		ret[2] = receivedMsg.substring(44);

		return ret;
	}

	public String getMode2() {
		return mode2;
	}

	public String getPadding2() {
		return padding2;
	}

	public AESKey getKey2() {
		return key2;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public int getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(int serialNo) {
		this.serialNo = serialNo;
	}

	public String getMode() {
		return mode;
	}

	public String getPadding() {
		return padding;
	}

	public AESKey getKey() {
		return key;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}

	public int getKeyLength() {
		return keyLength;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	public int getKeyLength2() {
		return keyLength2;
	}

	public void setKeyLength2(int keyLength2) {
		this.keyLength2 = keyLength2;
	}

	public void setPadding(String padding) {
		this.padding = padding;
	}

	public void setPadding2(String padding2) {
		this.padding2 = padding2;
	}

	public void setKey(AESKey key) {
		this.key = key;
	}

	public void setKey2(AESKey key2) {
		this.key2 = key2;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setMode2(String mode2) {
		this.mode2 = mode2;
	}

}
