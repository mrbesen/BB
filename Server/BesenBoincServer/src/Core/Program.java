package Core;

import java.nio.ByteBuffer;
import java.util.Arrays;

import Job.JobManager;
import Job.Result;
import Job.Result.PartialResult;

public abstract class Program{

	private JobManager jobmanager = new JobManager(this);
	
	public JobManager getJobManager() {
		return jobmanager;
	}
	
	/**
	 * Called by the JobManager or Server, to get the Program generating the next Job.
	 * The Program is allowed to sumbit more than one Program to the JobManager.
	 * returns false, when all jobs are submited
	 */
	public abstract boolean enquenextJob();
	
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
	
	protected byte[] inttoBytes(int i ) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}

	protected byte[] longtoBytes(long i ) {
		return ByteBuffer.allocate(8).putLong(i).array();
	}
	
	/**
	 * removes the zero terminating byte
	 * @param s
	 * @return
	 */
	protected byte[] stringtoBytes(String s) {
		byte[] tmp = s.getBytes();
		if(tmp[tmp.length-1] == 0) {
			tmp = Arrays.copyOf(tmp, tmp.length-1);
		}
		return tmp;
	}
	
	public abstract void HandlePartialResult(PartialResult pres);
}