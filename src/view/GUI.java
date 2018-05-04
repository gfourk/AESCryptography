package view;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import model.StateHolder;
import tcp.TCPClient;
import tcp.TCPServer;
import utils.AESKey;
import utils.Base64;

public class GUI extends JFrame implements MouseListener, KeyListener {

	private static final Logger logger = Logger.getLogger(GUI.class.getName());
	
	private static final long serialVersionUID = 1L;

	// the client and server classes
	transient TCPClient client;
	transient TCPServer server;
	transient StateHolder stateHolder;

	/*******************************************************************/
	// two string that hold the conversation history encrypted and not
	String history = "";
	String historyEn = "";

	// first comes the menu bar
	private JMenuBar menubar;
	// the menu bar contents

	private JMenu fileMenu;
	private JMenuItem savePasswordMenu;
	private JMenuItem loadPasswordMenu;

	private JMenu settingsMenu;
	private JMenuItem setIpPortMenu;
	private JMenuItem setPasswordMenu;
	private JMenuItem setPasswordMenu2;
	private JMenuItem setKeyLengthMenu;
	private JMenuItem setAESPaddingMenu;
	private JMenuItem setAESModeMenu;
	private JMenuItem setKeyLength2Menu;
	private JMenuItem setAESPadding2Menu;
	private JMenuItem setAESMode2Menu;

	private JMenu lookNfeelMenu;
	private JMenuItem setInputfieldBg;
	private JMenuItem setHistoryfieldBg;
	private JMenuItem setHistoryEnFieldBg;

	// separate the main window to two panels for ease of configuration
	// and separate them by (put them in ) a JSplitPane
	private JSplitPane panelsSeparator;
	private JPanel topPanel;
	private JPanel bottomPanel;

	// now the input text segment
	private JScrollPane textInputPaneScroll; // use a scroll pane
	private JEditorPane textInputPane;

	// the conversation text segment
	private JScrollPane convHistoryScroll;
	private JScrollPane convHistoryScrollEncrypted;
	private JEditorPane convHistory;
	private JEditorPane convHistoryEncrypted;

	// send button
	private JButton sendButton;

	// ------------------ setup network a
	private JFrame aSetupNetwork;
	private JTextArea aLocalPort;
	private JTextArea aRemotePort;
	private JTextArea aRemoteIp;
	private JButton aOk;
	private JButton aCancel;

	// --------------------- save / load password file
	private JFileChooser openFile;

	/********************************************************************************/
	/* default constructor */
	/********************************************************************************/
	public GUI(StateHolder stateHolder) {

		// super class constructor to create the window
		super("CSD.UOC HY457 Fall 2010-- Assign 1");

		this.stateHolder = stateHolder;

		// -------------------------- create sub components ------------------------
		// first the content pane
		this.getContentPane().setLayout(new GridLayout(2, 1));

		// create the menus of the menu bar
		this.savePasswordMenu = new JMenuItem("Save Key");
		this.loadPasswordMenu = new JMenuItem("Load Key");
		this.fileMenu = new JMenu("File               ");
		this.fileMenu.add(this.savePasswordMenu);
		this.savePasswordMenu.addMouseListener(this);
		this.fileMenu.add(this.loadPasswordMenu);
		this.loadPasswordMenu.addMouseListener(this);

		this.setIpPortMenu = new JMenuItem("Setup Network");
		this.setIpPortMenu.addMouseListener(this);
		this.setPasswordMenu = new JMenuItem("Set Password");
		this.setPasswordMenu.addMouseListener(this);
		this.setPasswordMenu2 = new JMenuItem("Set Password 2");
		this.setPasswordMenu2.addMouseListener(this);
		this.setKeyLengthMenu = new JMenuItem("Set Key Length");
		this.setKeyLengthMenu.addMouseListener(this);
		this.setKeyLength2Menu = new JMenuItem("Set Key Length 2");
		this.setKeyLength2Menu.addMouseListener(this);
		this.setAESPaddingMenu = new JMenuItem("Set AES Padding");
		this.setAESPaddingMenu.addMouseListener(this);
		this.setAESPadding2Menu = new JMenuItem("Set AES Padding 2");
		this.setAESPadding2Menu.addMouseListener(this);
		this.setAESModeMenu = new JMenuItem("Set AES Mode");
		this.setAESModeMenu.addMouseListener(this);
		this.setAESMode2Menu = new JMenuItem("Set AES Mode 2");
		this.setAESMode2Menu.addMouseListener(this);
		this.settingsMenu = new JMenu("Settings                      ");

		this.settingsMenu.add(this.setIpPortMenu);
		this.settingsMenu.add(this.setPasswordMenu);
		this.settingsMenu.add(this.setPasswordMenu2);
		this.settingsMenu.add(this.setKeyLengthMenu);
		this.settingsMenu.add(this.setKeyLength2Menu);
		this.settingsMenu.add(this.setAESPaddingMenu);
		this.settingsMenu.add(this.setAESPadding2Menu);
		this.settingsMenu.add(this.setAESModeMenu);
		this.settingsMenu.add(this.setAESMode2Menu);

		this.lookNfeelMenu = new JMenu("Look and Feel                                 ");
		this.setInputfieldBg = new JMenuItem("Set Input Bg Color");
		this.setInputfieldBg.addMouseListener(this);
		this.setHistoryfieldBg = new JMenuItem("Set History Bg Color");
		this.setHistoryfieldBg.addMouseListener(this);
		this.setHistoryEnFieldBg = new JMenuItem("Set Encrypted History Bg Color");
		this.setHistoryEnFieldBg.addMouseListener(this);

		this.lookNfeelMenu.add(this.setHistoryEnFieldBg);
		this.lookNfeelMenu.add(this.setHistoryfieldBg);
		this.lookNfeelMenu.add(this.setInputfieldBg);

		this.menubar = new JMenuBar();
		this.menubar.add(this.fileMenu);
		this.menubar.add(this.settingsMenu);
		this.menubar.add(this.lookNfeelMenu);

		// create the top and bottom panels
		this.topPanel = new JPanel();
		/*   */ this.topPanel.setLayout(new GridLayout());
		this.bottomPanel = new JPanel();
		this.bottomPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		this.bottomPanel.setLayout(new GridLayout(2, 1));
		this.topPanel.setLayout(new GridLayout(2, 1));

		// create separator and put the panels inside
		this.panelsSeparator = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.topPanel, this.bottomPanel);
		this.panelsSeparator.setDividerLocation(500);

		// create input text pane
		this.textInputPane = new JEditorPane();
		this.textInputPane.setAlignmentX(LEFT_ALIGNMENT);
		this.textInputPane.addKeyListener(this);
		this.textInputPane.setBackground(new Color(128, 128, 255));
		this.textInputPaneScroll = new JScrollPane(this.textInputPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.bottomPanel.add(this.textInputPaneScroll);

		// create send button
		this.sendButton = new JButton("Send");
		this.sendButton.setSize(10, 10);
		this.sendButton.addMouseListener(this);
		this.bottomPanel.add(this.sendButton);

		// ----- create normal conversation history text area
		this.convHistory = new JEditorPane();
		this.convHistory.setAlignmentX(LEFT_ALIGNMENT);
		this.convHistory.setAlignmentY(BOTTOM_ALIGNMENT);
		this.convHistoryScroll = new JScrollPane(this.convHistory, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// cannot edit history pane
		this.convHistory.setEditable(false);
		// set content type to html
		this.convHistory.setContentType("text/html");
		this.convHistory.setBackground(new Color(198, 226, 255));

		// ------ create conversation history area for the encrypted text
		this.convHistoryEncrypted = new JEditorPane();
		this.convHistoryEncrypted.setAlignmentX(LEFT_ALIGNMENT);
		this.convHistoryEncrypted.setAlignmentY(BOTTOM_ALIGNMENT);
		this.convHistoryScrollEncrypted = new JScrollPane(this.convHistoryEncrypted,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// cannot edit history pane
		this.convHistoryEncrypted.setEditable(false);
		// set content type to html
		this.convHistoryEncrypted.setContentType("text/html");
		this.convHistoryEncrypted.setBackground(new Color(230, 230, 230));

		// ----- add both history editor fields to the topPanel
		this.topPanel.add(this.convHistoryScrollEncrypted);
		this.topPanel.add(this.convHistoryScroll);

		// --------------------- save / load password file
		this.openFile = new JFileChooser("Save Password");

		// --------------- integrate sub components to main window -------------------
		this.getContentPane().add(this.menubar);
		this.getContentPane().add(this.panelsSeparator);

		// ---------------------------- window specific code -------------------------
		// set the layout
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		// set the window top left icon
		this.setIconImage(new ImageIcon("457LogoSmall2.JPG").getImage());
		// handle the close button action
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		// set starting dimensions
		this.pack();
		this.setSize(400, 650);
		// set to visible
		this.setVisible(true);

		// show a message with the local ip
		try {
			JOptionPane.showMessageDialog(this, "Your ip is: " + InetAddress.getLocalHost().toString(), "Network info.",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) { // do nothing in case of failure
		}
	}

	/****************************************************************************/
	// sets a server thread listening to port number port provided by the user
	// returns true only after the server is up and running
	/****************************************************************************/
	private boolean setServer(int port) {
		
		try {
			this.server.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
		try {
			this.server = new TCPServer(port, this);
			server.start();
		} catch (IOException e) {
			this.server.setOn(false); // set the server state
			return false;
		}
		this.server.setOn(true);
		return true;
	}

	// sets a client class to send packets to ip and port
	private void setClient(String ip, int port) {
		this.client = new TCPClient(ip, port);
	}

	/********************************************************************************/
	// network setup dialog
	/********************************************************************************/
	private void setupNetwork() {
		// setup window
		this.aSetupNetwork = new JFrame("Setup Network");
		this.aSetupNetwork.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.aSetupNetwork.getContentPane().setLayout(null);
		this.aSetupNetwork.setSize(240, 240);
		this.aSetupNetwork.setResizable(false);

		// setup main panel
		JPanel aPanel = new JPanel();
		aPanel.setLayout(null);
		aPanel.setBounds(0, 0, 240, 240);

		// setup all components
		JLabel aLocalPortLabel = new JLabel("Local Port No: ");
		this.aLocalPort = new JTextArea(Integer.toString(stateHolder.getLocalPort()));
		JLabel aRemotePortLabel = new JLabel("Remote Port No: ");
		this.aRemotePort = new JTextArea(Integer.toString(stateHolder.getRemotePort()));
		JLabel aRemoteIpLabel = new JLabel("Remote IP : ");
		this.aRemoteIp = new JTextArea(stateHolder.getRemoteIp());
		this.aOk = new JButton("OK");
		this.aCancel = new JButton("CANCEL");
		this.aOk.addMouseListener(this);
		this.aCancel.addMouseListener(this);

		aPanel.add(aLocalPortLabel);
		aPanel.add(this.aLocalPort);
		aPanel.add(aRemotePortLabel);
		aPanel.add(this.aRemotePort);
		aPanel.add(aRemoteIpLabel);
		aPanel.add(this.aRemoteIp);
		aPanel.add(this.aOk);
		aPanel.add(this.aCancel);

		aLocalPortLabel.setBounds(10, 10, 100, 20);
		this.aLocalPort.setBounds(120, 10, 100, 20);

		aRemotePortLabel.setBounds(10, 60, 100, 20);
		this.aRemotePort.setBounds(120, 60, 100, 20);

		aRemoteIpLabel.setBounds(10, 110, 100, 20);
		this.aRemoteIp.setBounds(120, 110, 100, 20);

		this.aOk.setBounds(10, 160, 100, 20);
		this.aCancel.setBounds(120, 160, 100, 20);

		this.aSetupNetwork.getContentPane().add(aPanel);

		this.aSetupNetwork.setVisible(true);

		// disable the main window while dialog is up
		this.setEnabled(false);
	}

	/******************************************************************************/
	// this function is called when the accept / connect button is pressed
	// in the network setup dialog
	/******************************************************************************/
	private void changeNetworkSettings() {
		// -------setup the client first
		// first set the remote ip and port number
		// in the defaults object as well as in the local client object
		boolean clientOk = true;

		int remotePort = 0;
		String remoteIp = null;
		try {
			remoteIp = this.aRemoteIp.getText();
			remotePort = Integer.parseInt(this.aRemotePort.getText());
			this.setClient(remoteIp, remotePort);
		} catch (Exception e) {
			clientOk = false;
		}

		// if anything went wrong
		if (!clientOk) {
			this.aRemoteIp.setBackground(Color.red);
			this.aRemotePort.setBackground(Color.red);
		} else {
			this.stateHolder.setRemoteIp(remoteIp);
			this.stateHolder.setRemotePort(remotePort);
			this.aRemoteIp.setBackground(Color.white);
			this.aRemotePort.setBackground(Color.white);
		}

		// -------- now try to setup the server
		int localPort;
		try {
			// try to read the input
			localPort = Integer.parseInt(this.aLocalPort.getText());
			if (this.setServer(localPort)) { // if successful
				this.aLocalPort.setBackground(Color.white); // set the colour back to white
				this.aSetupNetwork.dispose();
				this.stateHolder.setLocalPort(localPort); // store the new setting
				this.setEnabled(true); // enable the main window
			}
		} catch (NumberFormatException g) {
			this.aLocalPort.setBackground(Color.red);
		}
	}

	/****************************************************************************/
	// send message
	// this is called whenever a user wants to send a message
	/****************************************************************************/
	void sendMessage() {

		// if the client ip and port number are not set
		// show a message and popup the network setup dialog
		if (this.client == null) { // short circuit boolean evaluation, thanks java
			JOptionPane.showMessageDialog(this,
					"You must fisrt set the Network Options \n" + "from the \"Settings --> Setup Network\" menu",
					"ERROR Sending message", JOptionPane.ERROR_MESSAGE);
			this.mousePressed(new MouseEvent(this.setIpPortMenu, 0, 0, 0, 0, 0, 0, false));
			return;
		}

		// get the text the user typed
		String userInput = this.textInputPane.getText();

		// in case the button was pressed with no text
		if (userInput.equals("") || userInput.equals("\n"))
			return;
		// first try to encrypt the text

		String message = stateHolder.createMessage(userInput);

		// String in base 64 format, holds the encrypted text
		String encrypted = null;
		try {
			encrypted = new String(
					Base64.encode(AESKey.work(stateHolder.getMode(), stateHolder.getPadding(), true, stateHolder.getKey(), message.getBytes())));
		} catch (Exception e1) {
			// if anything goes wrong with the encryption
			// warn the user and return doing nothing
			JOptionPane.showMessageDialog(this,
					"Sorry could not encrypt the message\n" + "please check your AES settings .",
					"ERROR Encrypting Message", JOptionPane.ERROR_MESSAGE);

			// also must decrease the serial number that was
			// falsely incremented
			this.stateHolder.setSerialNo(this.stateHolder.getSerialNo()-1);
			return;
		}
		// now try to send to the other end the encrypted message
		this.client.send(encrypted);
		// append the right things to the history string
		this.history += stateHolder.getOutgoingMessageHead();// add the header of the html
		this.history += userInput; // add the text the user typed
		this.history += "<br>"; // add a new line in html

		// do the same for the encrypted
		this.historyEn += stateHolder.getOutgoingMessageHead();// add the header of the html
		this.historyEn += encrypted;// add the text the user typed but encrypted this time
		this.historyEn += "<br>"; // add a new line in html

		// update the history
		this.convHistory.setText(this.history);
		this.convHistoryEncrypted.setText(this.historyEn);

		// lastly erase the text the user typed
		this.textInputPane.setText("");
	}

	/****************************************************************************/
	// receive message
	// the server thread calls this function to deliver a message to the gui
	/****************************************************************************/
	public void receive(String encrypted) {

		String decrypted = null;
		// now decrypt
		try {
			decrypted = new String(
					AESKey.work(stateHolder.getMode(), stateHolder.getPadding(), false, stateHolder.getKey(), Base64.decode(encrypted.toCharArray())));
		} catch (Exception e) {
			decrypted = "Sorry failed to decrypt ciphertext";
		}
		String[] messageParts = this.stateHolder.parseMessage(decrypted);
		if (messageParts == null) {
			messageParts = new String[3];
			messageParts[0] = "-----";
			messageParts[1] = new Date().toString();
			messageParts[2] = "SORRY MESSAGE NOT ACCEPTED";
		}

		// append the right things to the history string
		this.history += stateHolder.getIncommingMessageHead(messageParts[1], messageParts[0]);// add the header of the html
		this.history += "<p>";
		this.history += messageParts[2]; // add the text the user typed
		this.history += "</p>";
		// this.history+="<br>"; // add a new line in html

		// do the same for the encrypted
		this.historyEn += stateHolder.getIncommingMessageHead(messageParts[1], messageParts[0]);// add the header of the html
		this.historyEn += encrypted;// add the text the user typed but encrypted this time
		// this.history_en+="<br>"; // add a new line in html

		// update the history
		this.convHistory.setText(this.history);
		this.convHistoryEncrypted.setText(this.historyEn);
	}
	/****************************************************************************/
	/* EVENTS */
	/****************************************************************************/

	/****************************************************************************/
	// ACTION MOUSE PRESSED
	/****************************************************************************/
	@Override
	public void mousePressed(MouseEvent arg0) {

		// -------------- send button in main window
		if (arg0.getSource().equals(this.sendButton)) {
			this.sendMessage();
		}
		// ----------------- set ip and port number menu network setup dialog
		if (arg0.getSource().equals(this.setIpPortMenu)) {
			this.setupNetwork();
		}
		// ---------------- Ok button in network setup dialog pressed
		if (arg0.getSource().equals(this.aOk)) {
			this.changeNetworkSettings();
		}
		// ---------------- cancel button in network setup dialog pressed
		if (arg0.getSource().equals(this.aCancel)) {
			this.aSetupNetwork.dispose();
			this.setEnabled(true); // enable the main window
		}
		// ----------------------------- if user wants to save the key
		if (arg0.getSource() == this.savePasswordMenu) {
			this.setEnabled(false); // disable the main window
			int returnVal = this.openFile.showDialog(this, "Save Key");

			// if the user pressed the accept in the save file dialog
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = this.openFile.getSelectedFile(); // get the file
				try {
					this.stateHolder.getKey().saveToFile(file, this.stateHolder);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "ERROR saving file.", "Error saving key",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			this.setEnabled(true);
		}
		// ----------------------------- if user wants to load a key from a file
		if (arg0.getSource() == this.loadPasswordMenu) {
			this.setEnabled(false); // disable the main window
			int returnVal = this.openFile.showDialog(this, "Load Key");

			// if the user pressed the accept file
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = this.openFile.getSelectedFile(); // get the file
				try {
					this.stateHolder.getKey().loadFromFile(file, this.stateHolder);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "ERROR Loading file.", "Error Loading key",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			this.setEnabled(true);
		}
		// ----------------------------- if the user pressed the submenu set pasword
		if (arg0.getSource().equals(this.setPasswordMenu)) {
			this.setEnabled(false); // disable the main window
			String s = null;
			boolean newPasswordNotOk = true;

			while (newPasswordNotOk) {
				// ask for password
				s = (String) JOptionPane.showInputDialog(this, "Set password", "Set Password",
						JOptionPane.PLAIN_MESSAGE, null, null, null);
				// if the user pressed the cancel button just return
				if (s == null) {
					this.setEnabled(true);
					return;
				}
				// else if the user provided a password
				// check how strong the password is
				// and if not strong enough then warn the user and resset the
				if (AESKey.securityLevel(s) >= 3) {
					newPasswordNotOk = false; // set the flag to
				} else {
					JOptionPane.showMessageDialog(this,
							"Sorry the password is not strong enough\n"
									+ "please Re-enter or cancel to set a random key.",
									"ERROR creating new key", JOptionPane.ERROR_MESSAGE);
				}
			}
			// store the new password
			this.stateHolder.setPassword(s);
			// create a new key with the password
			this.stateHolder.setKey(new AESKey(this.stateHolder.getKeyLength(), s.getBytes()));
			this.setEnabled(true);
		}
		// ----------------------------- if the user pressed the submenu set password 2
		if (arg0.getSource().equals(this.setPasswordMenu2)) {
			this.setEnabled(false); // disable the main window
			String s = null;
			boolean newPasswordNotOk = true;

			while (newPasswordNotOk) {
				// ask for password
				s = (String) JOptionPane.showInputDialog(this, "Set password #2", "Set Password #2",
						JOptionPane.PLAIN_MESSAGE, null, null, null);
				// if the user pressed the cancel button just return
				if (s == null) {
					this.setEnabled(true);
					return;
				}
				// else if the user provided a password
				// check how strong the password is
				// and if not strong enough then warn the user and resset the
				if (AESKey.securityLevel(s) >= 3) {
					newPasswordNotOk = false; // set the flag to
				} else {
					JOptionPane.showMessageDialog(this,
							"Sorry the password #2 is not strong enough\n"
									+ "please Re-enter or cancel to set a random key.",
									"ERROR creating new key", JOptionPane.ERROR_MESSAGE);
				}
			}
			// store the new password
			this.stateHolder.setPassword2(s);
			// create a new key for the key with the password #2
			this.stateHolder.setKey2(new AESKey(this.stateHolder.getKeyLength(), s.getBytes()));
			this.setEnabled(true);
		}

		// ----------------------------- if the user pressed the submenu set key lenght
		if (arg0.getSource().equals(this.setKeyLengthMenu)) {
			this.setEnabled(false); // disable the main window

			// ask for some input
			Object[] possibilities = { "128", "192", "256" };
			String s = (String) JOptionPane.showInputDialog(this, "Select Key lenght in bits", "Select Key lenght",
					JOptionPane.PLAIN_MESSAGE, null, possibilities, "" + this.stateHolder.getKeyLength());
			if (s == null) {
				this.setEnabled(true);
				return;
			}
			// store the new key lenght and change the key only if the lenght has changed
			int newLength = Integer.parseInt(s);
			// if the new length is different from the old one
			if (this.stateHolder.getKeyLength() != newLength) {
				this.stateHolder.setKeyLength(newLength);
				// create a new key with the new length
				if (this.stateHolder.getPassword() != null) // if the user has provided a password
					this.stateHolder.setKey(new AESKey(this.stateHolder.getKeyLength(), this.stateHolder.getPassword().getBytes()));
				else {
					try {
						this.stateHolder.setKey(new AESKey(this.stateHolder.getKeyLength()));
					} catch (NoSuchAlgorithmException e) {
						JOptionPane.showMessageDialog(this, "ERROR creating new key.",
								"Error Creating a new key with lenght" + newLength, JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			// always enable the main window when we are done
			this.setEnabled(true);
		}
		// ----------------------------- if the user pressed the submenu set key lenght
		// 2
		if (arg0.getSource().equals(this.setKeyLength2Menu)) {
			this.setEnabled(false); // disable the main window

			// ask for some input
			Object[] possibilities = { "128", "192", "256" };
			String s = (String) JOptionPane.showInputDialog(this, "Select Key lenght #2 in bits",
					"Select Key lenght #2", JOptionPane.PLAIN_MESSAGE, null, possibilities, "" + this.stateHolder.getKeyLength2());
			if (s == null) {
				this.setEnabled(true);
				return;
			}
			// store the new key lenght #2 and change the key only if the lenght has changed
			int newLength = Integer.parseInt(s);
			// if the new length is different from the old one
			if (this.stateHolder.getKeyLength2() != newLength) {
				this.stateHolder.setKeyLength2(newLength);
				// create a new key with the new length
				if (this.stateHolder.getPassword2() != null) // if the user has provided a password
					this.stateHolder.setKey2(new AESKey(this.stateHolder.getKeyLength2(), this.stateHolder.getPassword2().getBytes()));
				else {
					try {
						this.stateHolder.setKey2(new AESKey(this.stateHolder.getKeyLength2()));
					} catch (NoSuchAlgorithmException e) {
						JOptionPane.showMessageDialog(this, "ERROR creating new key #2.",
								"Error Creating a new key #2 with lenght" + newLength, JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			// always enable the main window when we are done
			this.setEnabled(true);
		}
		// ----------------------------- if the user pressed the submenu set AES padding
		if (arg0.getSource().equals(this.setAESPaddingMenu)) {
			this.setEnabled(false); // disable the main window

			// ask for some input
			Object[] possibilities = { "NoPadding", "PKCS5Padding", "PKCS7Padding", "ZeroPadding" };
			String s = (String) JOptionPane.showInputDialog(this, "Select AES Padding", "Select AES Padding",
					JOptionPane.PLAIN_MESSAGE, null, possibilities, this.stateHolder.getPadding());
			if (s == null) {
				this.setEnabled(true);
				return;
			}
			// store the new padding at the defaults class
			this.stateHolder.setPadding(s);
			// always enable the main window when we are done
			this.setEnabled(true);
		}
		// ----------------------------- if the user pressed the submenu set AES padding
		// 2
		if (arg0.getSource().equals(this.setAESPadding2Menu)) {
			this.setEnabled(false); // disable the main window

			// ask for some input
			Object[] possibilities = { "NoPadding", "PKCS5Padding", "PKCS7Padding", "ZeroPadding" };
			String s = (String) JOptionPane.showInputDialog(this, "Select AES Padding #2", "Select AES Padding #2",
					JOptionPane.PLAIN_MESSAGE, null, possibilities, this.stateHolder.getPadding2());
			if (s == null) {
				this.setEnabled(true);
				return;
			}
			// store the new padding at the defaults class
			this.stateHolder.setPadding2(s);
			// always enable the main window when we are done
			this.setEnabled(true);
		}

		// ----------------------------- if the user pressed the submenu set AES mode
		if (arg0.getSource().equals(this.setAESModeMenu)) {
			this.setEnabled(false); // disable the main window

			// ask for some input
			Object[] possibilities = { "ECB", "CBC" };
			String s = (String) JOptionPane.showInputDialog(this, "Select AES Mode", "Select AES Mode",
					JOptionPane.PLAIN_MESSAGE, null, possibilities, this.stateHolder.getMode());
			if (s == null) {
				this.setEnabled(true);
				return;
			}
			// store the new padding at the defaults class
			this.stateHolder.setMode(s);
			// always enable the main window when we are done
			this.setEnabled(true);
		}
		// ----------------------------- if the user pressed the submenu set AES mode 2
		if (arg0.getSource().equals(this.setAESMode2Menu)) {
			this.setEnabled(false); // disable the main window

			// ask for some input
			Object[] possibilities = { "ECB", "CBC" };
			String s = (String) JOptionPane.showInputDialog(this, "Select AES Mode #2", "Select AES Mode #2",
					JOptionPane.PLAIN_MESSAGE, null, possibilities, this.stateHolder.getMode2());
			if (s == null) {
				this.setEnabled(true);
				return;
			}
			// store the new padding at the defaults class
			this.stateHolder.setMode2(s);
			// always enable the main window when we are done
			this.setEnabled(true);
		}
		// -------------------- if the user clicked the set input bg color menu
		if (arg0.getSource().equals(this.setInputfieldBg)) {
			this.setEnabled(false); // disable the main window

			Color newColor = null;
			newColor = JColorChooser.showDialog(this.topPanel, "Choose Input Background Color",
					new Color(128, 128, 255));

			if (newColor != null)
				this.textInputPane.setBackground(newColor);
			this.setEnabled(true);
		}
		// -------------------- if the user clicked the set history bg color menu
		if (arg0.getSource().equals(this.setHistoryfieldBg)) {
			this.setEnabled(false); // disable the main window

			Color newColor = null;
			newColor = JColorChooser.showDialog(this.topPanel, "Choose History Background Color",
					new Color(192, 192, 192));

			if (newColor != null)
				this.convHistory.setBackground(newColor);
			this.setEnabled(true);
		}
		// -------------------- if the user clicked the set history encrypted bg color
		// menu
		if (arg0.getSource().equals(this.setHistoryEnFieldBg)) {
			this.setEnabled(false); // disable the main window

			Color newColor = null;
			newColor = JColorChooser.showDialog(this.topPanel, "Choose encrypted History Background Color",
					new Color(185, 255, 200));

			if (newColor != null)
				this.convHistoryEncrypted.setBackground(newColor);
			this.setEnabled(true);
		}

		return;
	}

	/****************************************************************************/
	// ACTION -- key pressed
	/****************************************************************************/
	public void keyPressed(KeyEvent arg0) {

		// if there was some text typed in the input text pane
		if (arg0.getSource().equals(this.textInputPane)

				// if the key pressed was the ENTER KEY
				&& arg0.getKeyCode() == KeyEvent.VK_ENTER) {

			this.sendMessage();

		}
	}

	/****************************** key typed **************************/
	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
	}

	/*********************** NOT USED ***************************/
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
	}

}
