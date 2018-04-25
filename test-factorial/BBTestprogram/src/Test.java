import Core.Program;
import Job.Job;
import Job.Result.PartialResult;

public class Test extends Program {

	private int c = 0;
	
	@Override
	public boolean enquenextJob() {
		String code = "import Job.Job;\nimport Job.Result;\nimport Job.Result.PartialResult;\nimport Job.Jobsrc;\npublic class A" + c + " extends Jobsrc {\n@Override\npublic Result run() {\ndouble i ="+c+";\nfor(int n = 2; n < " + c + ";n++){\n i *= n;\n}\n	Result r = new Result(" + (getJobManager().jobs_total() +1) +");\nr.OutputConsole(\"!"+c+"= \"+i);\n	return r;\n		}}";
		getJobManager().enque(new Job(code), false);
		c++;
		return true;
	}

	@Override
	public void HandlePartialResult(PartialResult pres) {}//unused
}