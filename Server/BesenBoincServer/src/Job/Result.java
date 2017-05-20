package Job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Result implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4086039173401754665L;
	
	private List<PartialResult> list = new ArrayList<PartialResult>();
	private int jobid;
	
	public Result(int job) {
		jobid = job;
	}
	
	public void OutputConsole(String out) {
		list.add(new PartialResult(ResultType.Console, out));
	}
	
	public Result add(PartialResult partres) {
		list.add(partres);
		return this;
	}
	
	public PartialResult next() {
		PartialResult r =  list.get(0);
		list.remove(0);
		return r;
	}
	
	public boolean hasnext() {
		return !list.isEmpty();
	}
	
	public int getJobId() {
		return jobid;
	}
	
	public enum ResultType{
		Console,//something to output in the console
		Value//something to compute with
	}
	
	public class PartialResult implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8517252929857625435L;
		
		public ResultType type;
		public Object obj;
		public PartialResult(ResultType t, Object o) {
			type = t; obj = o;
		}
	}
}