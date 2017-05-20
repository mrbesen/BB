package de.mrbesen;

import java.io.FileWriter;

import Core.Program;
import Job.Job;
import Job.Result.PartialResult;

public class CryptCrack extends Program {

	long testing = 1;
	long public_key = 14012539;//valid private key: 2597832237
	long mod = 4862802614L;
	//long public_key = 12600;//valid private key = 5042
	//long mod = 151621;
	int testsperrun = 5000000;
	
	@Override
	public void run() {
		requestnewjobs(100000);
	}

	@Override
	public void HandlePartialResult(PartialResult pres) {
		String in = (String) pres.obj;
		System.out.println(in);
		if(in.split(":",2)[1] == "true") {//match!
			String key = in.split(":",2)[0];
			//write to file
			try {
				FileWriter fw = new FileWriter("saved");
				fw.write(key);
				fw.close();
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("Error while saving progress");
			}

			System.out.println("KEY FOUND!\nkey = " + key);
			jobmanager.clear();//stop this job
		}
	}

	@Override
	public void requestnewjobs(int amount) {
		for(long i = testing; i < testing+(amount*testsperrun); i+= testsperrun) {
			String code = "import Job.Jobsrc;\nimport Job.Result;\nimport Job.Result.ResultType;\npublic class Crack" + i + " extends Jobsrc {\n	private final long start = " + i + "L;	private final long end = " + (i + testsperrun) + "L;	private long modul = " + mod + "L;	private long publicKey = " + public_key + "L;	\n	@Override\n	public Result run() {\n		Result out = new Result(" + (jobmanager.jobs_total() +1) + ");\n		for(long i = start; i < end; i++) {\n			if(try_to_crack(i))\n {out.add(out.new PartialResult(ResultType.Value, i + \":true\"));\n}\n		}\n		return out;\n	}\n	public boolean try_to_crack(long i) {\n	/*System.out.println(\"Testing\"+i);*/	return (publicKey * i) % modul == 1;\n	}\n}";
			jobmanager.enque(new Job(code));
		}
		testing += (amount*testsperrun);
	}
}