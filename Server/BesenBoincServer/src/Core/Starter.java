package Core;

import java.util.Scanner;

import Comunication.Server;
import Job.JobManager;

public class Starter {

	/**
	 * 
	 * @param args c:[classname] p:[port]
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		System.out.println("Starting BesenBoincServer...");

		String classname = "Test";//load programm
		int port = BB.port;
		for(String arg : args) {
			if(arg.contains(":")) {
				String[] split = arg.split(":",2);
				if(split[0].equalsIgnoreCase("c")) {
					classname = split[1];
				} else if(split[0].equalsIgnoreCase("p")) {
					try {
						port = Integer.parseInt(split[1]);
						if(port < 0 | port > 65535) {
							throw new NumberFormatException();
						}
						if(port < 1024) {
							System.out.println("Root required to use this port.");
						}
					} catch(NumberFormatException e) {
						System.out.println("Thats not a valid port: " + split[1]);
					}
				}
			} else {
				System.out.println("Arguments: c:[classfile] p:[port]");
				System.exit(1);
			}
		}
		Program prog = null;
		prog = loadProgram(classname);

		Server server = (new Server()).setProgram(prog).open(port);//start server
		boolean run = true;
		Scanner s = new Scanner(System.in);
		while(run) {
			String in = s.nextLine();
			if(in.equalsIgnoreCase("stop")) {
				System.out.println("Stopping Server...");
				Server.getServer().close();
				run = false;
			} else if(in.equalsIgnoreCase("stats")) {
				JobManager jm = server.getProgram().jobmanager;
				System.out.println("\nStats:\nTasks done    : " + jm.jobs_done() + "\nTasks send    : " + jm.jobs_send() + "\nTasks compiled: " + jm.jobs_compiled() + "/" + jm.jobs_compiledtarget() + "\nCurrently Compiling: " + jm.isCompiling() + "\nTasks enqued  : " + jm.jobs_enqued() + "\n--------------------\ntotal         : " + jm.jobs_total()+ "\nConnections: " + server.getConnectionCount());
			} else {
				System.out.println("unknown Command.");
			}
		}
		s.close();
	}

	public static Program loadProgram(String classname) throws ClassNotFoundException{
		System.out.println("Try to load class: " + classname);
		Program prog = null;
		try {
			prog = (Program) Class.forName(classname).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(prog == null)
			throw new ClassNotFoundException("Could not load Programm called: " + classname); 
		return prog;
	}
}