
public class Log {

	public static boolean LogOpen = true;
	
	public static void i(String... arg){		
		if (LogOpen) {		
			for(String temp:arg){
				System.out.print(temp);
			}
			System.out.print("\n");
		}
	}
}
