package it.unipi.cds.federatedLearning;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
//	private static final String ANSI_BLUE = "\u001B[34m";
//	private static final String ANSI_PURPLE = "\u001B[35m";
//	private static final String ANSI_CYAN = "\u001B[36m";
//	private static final String ANSI_WHITE = "\u001B[37m";
    
	public static void debug(String module,String text) {
		System.out.println(" [ "+new SimpleDateFormat("HH.mm.ss").format(new Date())+" ] "+" [ "+module+" ] "+" [ " + ANSI_YELLOW + " DEBUG " + ANSI_RESET + "] "+text);
	}
	public static void info(String module,String text) {
		System.out.println(" [ "+new SimpleDateFormat("HH.mm.ss").format(new Date())+" ] "+ " [ "+module+" ] "+" [ " + ANSI_GREEN + " INFO " + ANSI_RESET + "] "+text);
	}
	public static void error(String module,String text) {
		System.out.println(" [ "+new SimpleDateFormat("HH.mm.ss").format(new Date())+" ] "+" [ "+module+" ] "+" [ " + ANSI_RED + " ERROR " + ANSI_RESET + "] "+text);
	}
}
