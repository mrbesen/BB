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

	private int jobcount = 0;
	
	private Program prog;
	
	public JobManager(Program prog) {
		this.prog = prog;
	}

	private boolean isCompiling = false;

	public void enque(Job newjob) {
		newjob.setId(jobcount);
		enqued.add(newjob);
		jobcount++;
		update();
	}
	
	private Thread getnewThread() {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				isCompiling = true;//doppelt hält besser
//				System.out.println("Compilingque started!");
				while(enqued.size() > 0 & todo.size() < 15 & isCompiling) {
					Job j = enqued.get(0);
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

						//br.close();

						if(classfile.exists())//delete all files
							classfile.delete();
						if (file.exists())
							file.delete();

//						System.out.println("Compile done.");
					} catch(IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
				isCompiling = false;
//				System.out.println("Compiling que Stopped!");
			}
		}, "Compiler");
	}

	@Override
	public boolean hasNext() {
		return todo.size() != 0;
	}

	@Override
	public Job next() {
		send.add(todo.get(0));
		todo.remove(0);
		update();
		System.out.println("" + ((int) (((float) done.size()) / ((float)jobs_total()))*100) + "% Done");
		return send.get(send.size()-1);
	}

	@Override
	public void remove() {//unused

	}

	public void update() {//called from Server on new Client Connection 
		if(enqued.size() > 0 & !isCompiling & todo.size() < jobs_compiledtarget()) {//7 für jede connection vorrätig
			startCompile();
		} else if(enqued.size() < jobs_compiledtarget()) {
			System.out.println("All jobs done.");
			prog.requestnewjobs(jobs_compiledtarget() * 15);//mal nen par generieren
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
		//find yob
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
		} else {
			System.out.println("Job id " + jobId + " not found");
		}
	}
}