package tcp;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPClient {

	private static final Logger logger = Logger.getLogger(TCPClient.class.getName());
	
	private String ip;
	private int port;

	// Constructor
	public TCPClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	// send
	public void send(String msg) {

		try (Socket clientSocket = new Socket(ip, port)) {

			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			outToServer.writeBytes(msg + "\n");

		} catch (IOException e) {

			logger.log(Level.SEVERE, e.getMessage(), e);
		}

	}

}
