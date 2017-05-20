package Core;

import Comunication.Client;
import Job.Worker;

public class Starter {

	public static void main(String args[]) {
		System.out.println("Starting BesenBoincClient...");

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
		boolean run = true;
		int lost_counter = 0;//ho often loast?
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
					Thread.yield();
					Thread.sleep(500);
				} catch(Exception e) {}
			}

			System.out.println("Connecting to " + host + " on port: " + port);
			lasttest = System.currentTimeMillis();
			try {
				Client c = new Client(host, port, new Worker());
				
				System.out.println("Connection Established.");
				
				c.run();
				
				if(lost_counter > 0) {
					int minus = (int) ((System.currentTimeMillis()-lasttest)/10000);//für alle 10 sekunden verbinung ein lost count weniger
					if(minus > lost_counter)
						lost_counter = 0;
					else
						lost_counter -= minus;
				}
			} catch(Exception e) {
				//failed
				lost_counter++;
			}
			System.out.println("Connection LOST!");
		}
	}
}