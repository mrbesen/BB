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
import Utils.StringUtils;

public class JobManager implements Iterator<Job>{

	private List<Job> enqued = new ArrayList<Job>();//enqued jobs
	private List<Job> todo = new ArrayList<Job>();//compiled jobs
	private List<Job> done = new ArrayList<Job>();//done jobs

	int jobcount = 0;

	private boolean isCompiling = false;

	public void enque(Job newjob) {
		newjob.setId(jobcount);
		enqued.add(newjob);
		jobcount++;
		update();
	}
	
	public Thread getnewThread() {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				isCompiling = true;//doppelt hält besser
//				System.out.println("Compilingque started!");
				while(enqued.size() > 0 & todo.size() < 15) {
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

						/*BufferedReader br = new BufferedReader(new FileReader(classfile));//save the file to the String
						String line = null;
						StringBuilder strb = new StringBuilder();
						String linesep = System.getProperty("line.seperator");
						String out = "";

						while((line= br.readLine()) != null) {
							out += (line + linesep);
						}

						j.code = out;*/
						/*
						Scanner scanner = new Scanner(classfile);
						j.code = "";
						String out = "";
						while(scanner.hasNext()) {
							out = out + scanner.next();
						}
						scanner.close();
						j.code = out;
						System.out.println("File Readed size: " + j.code.length() + " " + out.length());
						*/
						
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
		done.add(todo.get(0));
		todo.remove(0);
		update();
		return done.get(done.size()-1);
	}

	@Override
	public void remove() {//unused

	}

	public void update() {//called from Server on new Client Connection 
		if(enqued.size() > 0 & !isCompiling & todo.size() < jobs_compiledtarget()) {//7 für jede connection vorrätig
			startCompile();
		}
	}
	
	public int jobs_done() {
		return done.size();
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
		if(w < 10)
			w = 10;
		return w;
	}

	private void startCompile() {
		if(!isCompiling) {
			isCompiling = true;
			getnewThread().start();
		}
	}
}