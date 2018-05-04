package tcp;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

import view.GUI;

public class TCPServer extends Thread {

	private static final Logger logger = Logger.getLogger(TCPServer.class.getName());
	
	ServerSocket welcomeSocket;
	String clientSentence;
	BufferedReader inFromClient;
	Socket connectionSocket;
	GUI gui;
	private boolean on = false;

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	// constructor need only port number to listen to
	public TCPServer(int port, GUI gui) throws IOException {
		welcomeSocket = new ServerSocket(port);
		this.gui = gui;
	}

	public void close() throws IOException {
		if (!this.welcomeSocket.isClosed())
			this.welcomeSocket.close();
		this.inFromClient.close();
	}

	// run method
	@Override
	public void run() {
		
		while (true) {
			
			try {
				connectionSocket = welcomeSocket.accept();
				inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				clientSentence = inFromClient.readLine();

				this.gui.receive(clientSentence);
				
			} catch (IOException e) {
				
				logger.info("IOEXCEPTION");
			}
			
		}
		
	}
	
}