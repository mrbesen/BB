package Core;

import java.io.IOException;

import Comunication.Client;
import Job.Worker;

public class Starter {

	public static void main(String args[]) {
		System.out.println("Starting BesenBoincClient...");
		
		//parsing Arguments
		String host = BB.host;
		int port = BB.port;
		if(args.length >= 1) {
			String[] split = args[0].split(":");
			if(split.length >= 1) {
				host = split[0];
				if(split.length>=2) {
					port = Integer.parseInt(split[1]);
				}
			}
		}
	
		//try to connect
		boolean run = true;
		int lost_counter = 0;//how often lost. - higher value - longer wait time until reconnect is tried.
		long lasttest = -1;
		while(run) {
			boolean tryagain = false;
			while(!tryagain) {
				if(lost_counter < 4)
					tryagain = true;
				if(lost_counter >= 4 & lost_counter < 10 & ((System.currentTimeMillis()-lasttest)/1000) > 10) {
					tryagain = true;
				}
				if(lost_counter >= 10 & lost_counter < 25 & ((System.currentTimeMillis()-lasttest)/1000) > 30) {
					tryagain = true;
				}
				if(lost_counter >= 25 & ((System.currentTimeMillis()-lasttest)/1000) > 120) {
					tryagain = true;
				}

				try {
					Thread.sleep(500);
				} catch(InterruptedException e) {}
			}
			
			//try to connect
			System.out.print("Connecting to " + host + " on port: " + port + " ");
			lasttest = System.currentTimeMillis();
			try {
				Worker worker = new Worker();
				Client c;
				try {
					c = new Client(host, port, worker);//throws IOException on failed connection
				} catch (IOException e) {
					System.out.println("Failed");
					throw new Exception(e);//do not print stack trace
				}
				
				worker.start();//start worker Thread, only if connection is established
				c.run();//manage connection
				
				if(lost_counter > 0) {
					int minus = (int) ((System.currentTimeMillis()-lasttest)/10000);//für alle 10 sekunden verbinung ein lost count weniger
					if(minus > lost_counter)
						lost_counter = 0;
					else
						lost_counter -= minus;
				}
				System.out.println("Connection Lost!");
				worker.stop();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(Exception e) {
				//failed
				lost_counter++;
			}
		}
	}
}