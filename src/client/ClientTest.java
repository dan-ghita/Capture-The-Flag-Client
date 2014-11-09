package client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;

public class ClientTest {
	
	public static void main(String[] args){
		
		Client charlie;
		InetAddress myIP;
		try {
			myIP = InetAddress.getLocalHost();
			charlie = new Client(myIP.getHostAddress());
			charlie.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			charlie.startRunning();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
