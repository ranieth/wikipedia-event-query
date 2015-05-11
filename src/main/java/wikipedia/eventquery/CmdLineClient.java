package wikipedia.eventquery;

/**
 * Command line client for querying events that occurred on a specific day of month.  
 */
public class CmdLineClient {

	private static void printHelpAndExit() {
		System.err.printf("Usage: java %s <month> <day>\n", CmdLineClient.class.getName());
		System.exit(1);
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			printHelpAndExit();
		}
		try {
			int	month = Integer.parseInt(args[0]);
			int	day = Integer.parseInt(args[1]);
			for (Event event : new EventQuery().query(month, day)) {
				System.out.println(event);
			}
		} catch(NumberFormatException e) {
			System.err.println("Invalid argument");
			printHelpAndExit();
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
