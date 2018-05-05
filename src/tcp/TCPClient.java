package tcp;

import java.io.*;
import java.net.*;

public class TCPClient {

	private String ip;
	private int port;

	/**
	 *  Constructor
	 * @param ip
	 * @param port
	 */
	public TCPClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	/**
	 *  send
	 * @param msg
	 * @throws IOException
	 */
	public void send(String msg) throws IOException {

		try ( Socket clientSocket = new Socket(ip, port) ) {
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			outToServer.writeBytes(msg + "\n");
		}
	}

}
