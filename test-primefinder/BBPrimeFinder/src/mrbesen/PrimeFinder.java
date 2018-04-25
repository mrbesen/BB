package mrbesen;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Core.Program;
import Job.Job;
import Job.Result.PartialResult;

public class PrimeFinder extends Program {

	List<Double> primes = new ArrayList<Double>();
	double count = -1;//current prime
	File file = new File("primes");

	int save_count;//to save every 20 times
	int save_intervall = 100;

	public PrimeFinder() {
		//load primes	
		try {
			if(file.exists()) {
				FileReader fr = new FileReader(file);
				Scanner frs = new Scanner(fr);

				int i = 0;
				while(frs.hasNextLine()) {
					primes.add(Double.parseDouble(frs.nextLine()));
					i ++;
				}
				frs.close();
				System.out.println(i + " primes loaded. last prime: " + primes.get(primes.size()-1));
			} else {
				System.out.println("No Primes-file found, starting with 2");
				primes.add(2D);
			}
			for(int i = 0; i < 50; i++) {
				checkforprime();
			}
		} catch(IOException e) {}
	}

	public void checkforprime() {
		if(count == -1)
			count = primes.get(primes.size()-1)+1;

		String insert = PrimetoString(count);
		getJobManager().enque(new Job("import Job.Jobsrc;\nimport Job.Result;\nimport Job.Result.ResultType;\npublic class A" + insert + " extends Jobsrc{\n	double num = " + insert + ";\n	@Override\n	public Result run() {\n		Result r = new Result();\n		for(double test = num; test < num + 200; test = test +2) {\n			for(double i = 2; i * i <= test+2; i++) {\n				if(test%i == 0)\n					r.add(r.new PartialResult(ResultType.Value, test + \"|\" + false));\n			}\n			r.add(r.new PartialResult(ResultType.Value, test + \"|\" + true));\n		}\n		return r;\n	}\n}"), false);
		count+= 200;
	}

	public void save() throws Exception{
		save_count = 0;
		if(!file.exists())
			file.createNewFile();

		FileWriter fw = new FileWriter(file);
		for(Double doub : primes) {
			fw.write(PrimetoString(doub)+"\n");
		}
		fw.close();
		System.out.println("Primes saved");
	}

	private String PrimetoString(double prime) {
		return ((int) prime) / 100 + "";
	}

	@Override
	public void HandlePartialResult(PartialResult pres) {
		checkforprime();//enque next

		if(pres.obj instanceof String) {
			String return_ = (String) pres.obj;
			System.out.println("Recived Result: " + return_);
			String[] split = return_.split("\\|", 2);
			if(split[1].equalsIgnoreCase("true")) {
				System.out.println("Prime Found: " + split[0]);
				primes.add(Double.parseDouble(split[0]));
			}
		}

		save_count++;
		if(save_count >=save_intervall)
			try {
				save();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}


	@Override
	public boolean enquenextJob() {
		checkforprime();
		return true;
	}
}