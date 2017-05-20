package Comunication;

import java.io.Serializable;

import Job.Job;
import Job.Result;

public class Data implements Serializable{

	private static final long serialVersionUID = 8845895533697133L;
	
	public Object content;
	public ContentType type;
	
	public Data(Object o) {
		if(o instanceof String) {
			type = ContentType.Info;
		} else if( o instanceof Job) {
			type = ContentType.Job;
		} else if( o instanceof Result) {
			type = ContentType.Result;
		} else {
			throw new ClassCastException("The Object is not a String, Job or Result!");
		}
		content = o;
	}
	
	public Data(Object o, ContentType t) {
		content = o;
		type = t;
	}
	
	public enum ContentType {
		Job,
		Result,
		Info
	}
}