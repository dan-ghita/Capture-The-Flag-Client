package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class Client extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;

	private String nextServerID;

	public Client(String host) {
		super("Client side");
		serverIP = host;

		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendMessage(event.getActionCommand());
				userText.setText("");
			}
		});
		add(userText, BorderLayout.NORTH);

		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);

		setSize(300, 150);
		setVisible(true);
	}

	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			whileChatting();
		} catch (EOFException eofException) {
			showMessage("\n Client terminated the connection! ");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			closeCrap();
		}
	}

	private void connectToServer() throws IOException {
		connection = new Socket(InetAddress.getByName(serverIP), 8888);
	}

	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
	}

	private void switchServer(String newIp) {
		closeCrap();
		serverIP = newIp;
		try {
			connectToServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			setupStreams();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void whileChatting() throws IOException {
		ableToType(true);

		String flagToken;

		sendMessage("next_server");

		// step1
		try {
			message = (String) input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		do {
			// step2
			try {
				switchServer(message);

				sendMessage("who_are_you?");
				message = (String) input.readObject();
				showMessage("\nTalking with " + message);
				nextServerID = message;

				sendMessage("have_flag?");
				showMessage("\nhave_flag?");
				message = (String) input.readObject();
				if (message.equals("NO")) {
					showMessage("\nNO\n");
					sendMessage("next_server");
					message = (String) input.readObject();
					showMessage('\n' + message + '\n');
					Thread.sleep(500);
				} else {
					showMessage('\n' + message + '\n');
					flagToken = message.substring(4);
					sendMessage("capture_flag " + flagToken);

					message = (String) input.readObject();
					message = message.substring(5);
					showMessage("capture_flag " + flagToken + '\n');
					switchServer(InetAddress.getLocalHost().getHostAddress());
					sendMessage("hide_flag " + message);
					showMessage("\nGot the flag from " + nextServerID);

					Random rand = new Random();
					int sleepTime = Math.abs(rand.nextInt() % 10 + 1);
					showMessage("\nresting now for " + sleepTime + " seconds");
					Thread.sleep(sleepTime * 1000);

					sendMessage("next_server");
					// step1
					try {
						message = (String) input.readObject();

					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				}

			} catch (ClassNotFoundException classNotFoundException) {
				showMessage("\n Unrecognised input \n");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (!message.equals("SERVER - END"));
	}

	private void closeCrap() {
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private void sendMessage(String message) {
		try {
			output.writeObject(message);
			output.flush();
		} catch (IOException ioException) {
			chatWindow.append("\n ERROR \n");
		}
	}

	private void showMessage(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chatWindow.append(text);
			}
		});
	}

	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				userText.setEditable(tof);
			}
		});
	}

}
