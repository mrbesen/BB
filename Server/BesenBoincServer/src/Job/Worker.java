package Job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Comunication.Client;
import Comunication.Client.PacketHandler;
import Comunication.Data;
import Comunication.Data.ContentType;

public class Worker implements PacketHandler, Runnable{

	//private Job current = null;
	//private Job next;
	private List<Job> jobs = new ArrayList<Job>();
	private Client client; 
	private long lastasked = System.currentTimeMillis();
	private boolean run = true;

	public Worker() {
		Thread workerthread = new Thread(this, "Worker");
		workerthread.start();
	}

	public void stop() {
		run = false;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1500);//just waiting for final initalisation
			while(!client.hasConnection()) {//waiting for connection
				Thread.sleep(500);
			}
		} catch(Exception e) {}
		while (run) {
			if(jobs.size()>0) {
				Result r = justrun(jobs.get(0));
				if(r != null) {
					client.send(new Data(r));
					jobs.remove(0);
					requestnewjob();//new job
				} else {
					System.err.println("Result ist null!");
				}
			} else {//derzeit kein job in petto
				try {
					Thread.sleep(200);
					requestnewjob();
				} catch(InterruptedException e) {}
			}
		}
	}

	private Result justrun(Job j) {
//		System.out.println("recived code:" + srccode);
		Result r = null;
		try {
			File classfile = new File(j.classname+".class");//remove class file
			if(classfile.exists())
				classfile.delete();					

			/*	System.out.println("Writing file size:" + j.code.length());
			FileWriter fw = new FileWriter(classfile);//file writing
			fw.write(j.code);
			fw.close();
		 	*/
			FileOutputStream fos = new FileOutputStream(classfile);
			fos.write(j.classfile);
			fos.close();
			
			r = ((Jobsrc) Class.forName(j.classname).newInstance()).run();//load & run
			//unload?
			
			if(classfile.exists())
				classfile.delete();

		} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return r;
	}

	@Override
	public void HandleData(Data data, Client c) {
		client = c;
//		if(data.type == ContentType.Info)
//			System.out.println("Data Recived: " + (String) data.content);
		if(data.type == ContentType.Job) {
			lastasked = 1; //den wait resetten, da er was bekommen hat.
			jobs.add((Job) data.content);
//			System.out.println("Recived job id: " + jobs.get(jobs.size()-1).getId());
//			System.out.println("new Job recived");
			if(jobs.size() < 4) 
				requestnewjob();//wenn buffer leer -> direct nächsten requesten.
		} //else 
//			System.err.println("unhandled Data!");
	}

	private void requestnewjob() {
		if((System.currentTimeMillis()-lastasked) > 1500 & run & client.hasConnection()) {//request wenn letster unerfolg reicher lange genug her ist (Server nicht nerven)
			client.send(new Data("nextplease"));
//			System.out.println("asked for next job");
			lastasked = System.currentTimeMillis();
		}
	}
}