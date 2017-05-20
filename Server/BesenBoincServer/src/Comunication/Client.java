package Comunication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable{
	private Socket soc;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	private PacketHandler handler;
	private boolean hold_connection;

	public Client(Socket soc, PacketHandler hand) {//server side constructor
		hold_connection = true;
		handler = hand;
		this.soc = soc;
		getStreams();
	}

	public Client(String addr, int port, PacketHandler hand) throws UnknownHostException, IOException {//client side constructor
		hold_connection = true;
		handler = hand;
		soc = new Socket(InetAddress.getByName(addr), port);//connect
		getStreams();
		System.out.println("Connection established.");
	}

	public void getStreams() {
		try {
			out = new ObjectOutputStream(soc.getOutputStream());
			in = new ObjectInputStream(soc.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(Data data) {
		if(hold_connection & hasConnection()) {
			try {
				out.writeObject(data);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void disconnect() {
		hold_connection = false;
		try {
			in.close();
			out.close();
			soc.close();
			System.out.println("Disconnected!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(!soc.isClosed() & hold_connection) {
			//handle data
			try {
				handler.HandleData((Data) in.readObject(), this);
				//				System.out.println("Recived data!");
			} catch (Exception e) {
				e.printStackTrace();
				disconnect();
			}
		}
		disconnect();
	}

	public boolean hasConnection() {
		return soc.isConnected();
	}

	public interface PacketHandler {
		public void HandleData(Data data, Client c);
	}
}