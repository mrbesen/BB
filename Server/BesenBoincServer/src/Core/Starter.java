package Core;

import java.util.Scanner;

import Comunication.Server;
import Job.JobManager;

public class Starter {

	public static void main(String[] args) throws ClassNotFoundException {
		System.out.println("Starting BesenBoincServer...");

		String classname = "Test";//load programm
		if(args.length >= 1) {
			classname = args[0];
		}
		Program prog = null;
		prog = loadProgram(classname);

		Server server = (new Server()).setProgram(prog).open();//start server
		boolean run = true;
		Scanner s = new Scanner(System.in);
		while(run) {
			String in = s.nextLine();
			if(in.equalsIgnoreCase("stop")) {
				System.out.println("Stopping Server...");
				Server.getServer().close();
				run = false;
			} else if(in.equalsIgnoreCase("stats")) {
				JobManager jmanager = server.getProgram().jobmanager;
				System.out.println("\nStats:\nTasks done    : " + jmanager.jobs_done() + "\nTasks compiled: " + jmanager.jobs_compiled() + "/" + jmanager.jobs_compiledtarget() + "\nTasks enqued  : " + jmanager.jobs_enqued() + "\n--------------------\ntotal         : " + jmanager.jobs_total()+ "\n");
			}else {
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