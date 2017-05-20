package de.mrbesen;

import Job.Jobsrc;
import Job.Result;
import Job.Result.ResultType;

public class Crack extends Jobsrc {

	private final int start = 0;
	private final int end = 0;
	private int modul = 1;
	private int publicKey = 0;
	
	@Override
	public Result run() {
		Result out = new Result(1);
		for(int i = start; i < end; i++) {
			out.add(out.new PartialResult(ResultType.Value, i + ":" + try_to_crack(i)));
		}
		return out;
	}

	public boolean try_to_crack(int i) {
		//encryption dependend. - for testing: modular multiplication
		return (publicKey * i) % modul == 1;
	}
}