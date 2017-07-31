package Comunication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import Comunication.Data.ContentType;
import Job.Job;

public class Client implements Runnable{
	private Socket soc;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	private PacketHandler handler;
	private boolean hold_connection;
	
	private List<Job> takenjobs = new LinkedList<Job>();

	public Client(Socket soc, PacketHandler hand) {//server side constructor
		hold_connection = true;
		handler = hand;
		this.soc = soc;
		getStreams();
	}

	public Client(String addr, int port, PacketHandler hand) throws IOException {//client side constructor
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
				if(data.type == ContentType.Job) {
					takenjobs.add((Job) data.content);
				}
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
			//re-enque all take jobs
			Server.getServer().getProgram().jobmanager.reenque(takenjobs);
			takenjobs.clear();
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
	
	void removetakenjob(int jobid) {
		for(int i = 0; i < takenjobs.size(); i++) {
			if(takenjobs.get(i).getId() == jobid) {
				takenjobs.remove(i);
				break;
			}
		}
	}

	public boolean hasConnection() {
		return soc.isConnected();
	}

	public interface PacketHandler {
		public void HandleData(Data data, Client c);
	}
}