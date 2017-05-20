package Job;

import java.io.Serializable;

public class Job implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 54437263428201383L;
	
	private int id;
	public String code;//a class code extends Jobsrc
	public byte[] classfile;
	public boolean compiled = false;
	
	public String classname = ""; 
	
	public Job(String src) {
		code = src;
	}
	
	public void setId(int id) {
		this.id = id;
//		code.replace("[[Jobid]]",""+ id);
	}
	
	public int getId() {
		return id;
	}
}