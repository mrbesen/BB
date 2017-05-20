package Comunication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Comunication.Client.PacketHandler;
import Comunication.Data.ContentType;
import Core.Program;
import Job.Result;

public class Server implements PacketHandler {

	ServerSocket ssoc;
	List<Client> connections = new ArrayList<Client>();
	boolean accept_new_connections = false;
	Thread serverthread;
	Thread programthread;
	private static Server server;
	private ExecutorService execution = Executors.newCachedThreadPool();
	private Program prog;

	public Server() {
		server = this;
	}

	public static Server getServer() {
		return server;
	}

	public Server open(int port) {
		try {
			ssoc = new ServerSocket(port);
			serverthread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (accept_new_connections) {
						try {
							Socket soc = ssoc.accept();
							System.out.println("Connection established with: " + soc.getInetAddress());
							Client c = new Client(soc, Server.getServer());
							connections.add(c);
							execution.submit(c);
							c.send(new Data("welcomme"));
							prog.jobmanager.update();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}, "Serverthread");
			serverthread.start();
			accept_new_connections = true;
			System.out.println("Server is Listening on port " + port);
			
			programthread = new Thread(prog, "Programm Thread");
			programthread.start();
			System.out.println("Programm execution started");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public Server setProgram(Program prog) {
		this.prog = prog;
		return this;
	}
	
	public Program getProgram() {
		return prog;
	}
	
	@SuppressWarnings("deprecation")
	public Server cancelProgram() {
		programthread.stop();
		prog = null;
		return this;
	}
	
	@SuppressWarnings("deprecation")
	public void close() {
		accept_new_connections = false;
		serverthread.stop();
		//disconect all
		for(Client c : connections)
			c.disconnect();

		execution.shutdownNow();

		try {
			ssoc.close();//close server socket
			ssoc = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void HandleData(Data data, Client c) {
		//		System.out.println("Data Recived: " + data);
		if(data.type == ContentType.Info) {
			String info = (String) data.content;
			if(info.equalsIgnoreCase("ping")) {
				c.send(new Data("pingback"));
//				System.out.println("Pinged, pining back...");
			} else if(info.equalsIgnoreCase("nextplease")) {
				if(prog.jobmanager.hasNext()) {
					c.send(new Data(prog.jobmanager.next(),ContentType.Job));
				} else {
					System.out.println("Out of Tasks");
				}
			}
		} else if(data.type == ContentType.Result) {
			prog.HandleResult((Result) data.content);
		}else
			System.out.println("unhandled Data!");
	}
	
	public int getConnectionCount() {
		return connections.size();
	}
}