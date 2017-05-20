package Core;

import Job.JobManager;
import Job.Result;
import Job.Result.PartialResult;

public abstract class Program implements Runnable{

	public JobManager jobmanager = new JobManager(this);
	
	public void HandleResult(Result r) {
		jobmanager.setdone(r.getJobId());
		
		while(r.hasnext()) {
			PartialResult pres = r.next();
			switch(pres.type) {
			case Console: System.out.println((String) pres.obj); break;
			case Value: HandlePartialResult(pres);
			}
		}
	}
	
	public abstract void HandlePartialResult(PartialResult pres);

	public abstract void requestnewjobs(int amount);
}