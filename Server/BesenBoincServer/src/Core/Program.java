package Core;

import Job.JobManager;
import Job.Result;
import Job.Result.PartialResult;

public abstract class Program implements Runnable{

	public JobManager jobmanager = new JobManager();
	
	public void HandleResult(Result r) {
		while(r.hasnext()) {
			PartialResult pres = r.next();
			switch(pres.type) {
			case Console: System.out.println((String) pres.obj);
			case Value: HandlePartialResult(pres);
			}
		}
	}
	
	public abstract void HandlePartialResult(PartialResult pres);
}