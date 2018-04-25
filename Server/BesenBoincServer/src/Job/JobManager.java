package Job;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import Comunication.Server;
import Core.Program;
import Utils.StringUtils;

public class JobManager implements Iterator<Job>{

	private List<Job> enqued = new ArrayList<Job>();//enqued jobs
	private List<Job> todo = new ArrayList<Job>();//compiled jobs
	private List<Job> send = new ArrayList<Job>();//assigned jobs
	private List<Job> done = new ArrayList<Job>();//done jobs

	private Program prog;
	private int jobcount = 0;

	private boolean isCompiling = false;

	public JobManager(Program program) {
		prog = program;
	}

	public void reenque(List<Job> jobs) {
		todo.addAll(0, jobs);
		send.removeAll(jobs);
		update();
	}
	
	public void enque(Job newjob, boolean isPreCompiled) {
		jobcount++;
		newjob.setId(jobcount);
		if(isPreCompiled)
			todo.add(newjob);
		else
			enqued.add(newjob);
		update();
	}
	
	/**
	 * Creates a new Compiling Thread
	 * @return
	 */
	private Thread getnewThread() {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				isCompiling = true;//doppelt hält besser
				//compile them!
				while(enqued.size() > 0 && todo.size() < jobs_compiledtarget() && isCompiling) {
					Job j = enqued.get(0);
					if(j == null) {
						System.out.println("enqued.size(): " + enqued.size() + " but job.get(0) is null!");
						break;
					}
					//compile script
					try {
						//					System.out.println(j.code);
						String classname = StringUtils.cutout(j.code, "class ", " extends");//class [name] extends

						File file = new File(classname + ".java");//delete old & file creation
						if (file.exists())
							file.delete();
						file.createNewFile();

						File classfile = new File(classname+".class");//remove class file
						if(classfile.exists())
							classfile.delete();					

						FileWriter fw = new FileWriter(file);//file writing
						fw.write(j.code);
						fw.close();

						Process process = Runtime.getRuntime().exec("javac " + file.getAbsolutePath());//compile
						Scanner scan = new Scanner(process.getErrorStream());
						while(scan.hasNextLine()) {
							System.out.println("Compile Err: " + scan.nextLine());
						}
						scan.close();
						process.waitFor();

						
						RandomAccessFile f = new RandomAccessFile(classfile, "r");
						j.classfile = new byte[(int)f.length()];
						f.read(j.classfile);
						f.close();

						j.compiled = true;
						j.classname = classname;
						
						enqued.remove(0);//manage ques
						todo.add(j);

						if(classfile.exists())//delete all files
							classfile.delete();
						if (file.exists())
							file.delete();

					} catch(IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
				isCompiling = false;
//				System.out.println("Compiler stopped!");
			}
		}, "Compiler");
	}

	/**
	 * is a new Compiled job in the list
	 */
	@Override
	public boolean hasNext() {
		if(todo.size() == 0) {
			update();
			return false;
		}
		return true;
	}

	@Override
	public Job next() {
		//Warning! not checked if todo.get(0) exists!
		send.add(todo.get(0));
		todo.remove(0);
		update();
		System.out.println(((int) (((float) done.size()) / ((float)jobs_total()))*100) + "% Done");
		return send.get(send.size()-1);
	}

	/**
	 * Called, when the job-lists get modified or a new client connects.
	 * This method simply checks weather the compiler-thread should be started or not.
	 * If the Thread should be started, this method does it with no further instruction.
	 */
	public void update() {
		boolean hasnext = true;//variable zum testen, ob das Programm weitere Jobs hat
		while(enqued.size() + todo.size() < jobs_compiledtarget() && (hasnext = prog.enquenextJob()));//genarte new jobs!
		if(!hasnext) {
			System.out.println("Program has no new Jobs!");
			//TODO: somehow stop the compile thread after compiling (and disable it from re-opening) and set a flag, that after these jobs are done the server could shutdown.
		}
		if(enqued.size() > 0 && !isCompiling && todo.size() < jobs_compiledtarget()) {//7 jobs for each connection and at least 10
			startCompile();
		}
	}
	
	public int jobs_done() {
		return done.size();
	}
	
	public int jobs_send() {
		return send.size();
	}
	
	public int jobs_compiled() {
		return todo.size();
	}
	public int jobs_enqued() {
		return enqued.size();
	}
	public int jobs_total() {
		return jobcount;
	}
	
	/**
	 * How many Jobs should stored compiled?
	 * @return 7 Jobs for every Connection, and at least 10.
	 */
	public int jobs_compiledtarget() {
		int w = 7 * Server.getServer().getConnectionCount();
		return (w < 10) ? 10 : w;
	}

	private void startCompile() {
		if(!isCompiling) {
			isCompiling = true;
			getnewThread().start();
		}
	}

	/**
	 * Drop all jobs
	 */
	public void clear() {
		//posible bug: when the Compiler is running, an then the que is cleared, its posible that the compiler adds a job.
		//work around: add a wait, or save the Thread object and wait for it
		isCompiling = false;
		enqued.clear();
		done.clear();
		send.clear();
		todo.clear();
		jobcount = 0;
	}
	
	public boolean isCompiling() {
		return isCompiling;
	}

	
	/**
	 * Mark a job as done
	 * @param jobId
	 */
	public void setdone(int jobId) {
		//find job
		Job j = null;
		int pos = -1;
		for(int i = 0; i < send.size(); i++) {
			if(send.get(i).getId() == jobId) {
				j = send.get(i);
				pos = i;
				break;
			}
		}
		
		if(j != null) {
			send.remove(pos);
			done.add(j);
			//TODO: delete old done jobs, to preverse Memory?
		} else {
			System.out.println("Job id " + jobId + " not found");
		}
	}
}