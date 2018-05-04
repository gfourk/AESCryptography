package tcp;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import view.GUI;

public class TCPServer extends Thread {

	private static final Logger logger = Logger.getLogger(TCPServer.class.getName());

	private ServerSocket welcomeSocket;
	private GUI gui;

	// constructor need only port number to listen to
	public TCPServer(int port, GUI gui) throws IOException {
		welcomeSocket = new ServerSocket(port);
		this.gui = gui;
	}

	public void close() {
		if ( !this.welcomeSocket.isClosed() )
			try {
				this.welcomeSocket.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
	}

	// run method
	@Override
	public void run() {

		try {
			while (true) {
				try ( Socket connectionSocket = welcomeSocket.accept() ) {
					BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					String clientSentence = inFromClient.readLine();
					this.gui.receive(clientSentence);
				}
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
	}

}
