package Utils;

public class StringUtils {
	
	public static String cutout(String source, String bevore, String after) {
		source = source.substring(source.indexOf(bevore) + bevore.length());
		source = source.substring(0,source.indexOf(after));
		
		return source;
	}
}