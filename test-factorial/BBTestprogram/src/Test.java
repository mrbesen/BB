import Core.Program;
import Job.Job;
import Job.Result.PartialResult;

public class Test extends Program {

	@Override
	public void run() {
		for(int c = 5; c < 1000; c++) {
			String code = "import Job.Job;\nimport Job.Result;\nimport Job.Result.PartialResult;\nimport Job.Jobsrc;\npublic class A" + c + " extends Jobsrc {\n@Override\npublic Result run() {\ndouble i ="+c+";\nfor(int n = 2; n < " + c + ";n++){\n i *= n;\n}\n	Result r = new Result();\nr.OutputConsole(\"!"+c+"= \"+i);\n	return r;\n		}}";
			jobmanager.enque(new Job(code));
		}
	}

	@Override
	public void HandlePartialResult(PartialResult pres) {}//unused
}