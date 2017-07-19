package com.qaprosoft.carina.core.foundation.utils.android.recorder.utils;

import java.util.List;

/**
 * Created by YP.
 * Date: 8/19/2014
 * Time: 12:52 AM
 */
public class CmdLine {

    public static String[] createPlatformDependentCommandLine(String ... command) {
/*    	if (Platform.IS_MAC_OS_X || Platform.IS_LINUX) {
			int newArraySize = command.length + 2;
			
	        String[] newCommands = new String[newArraySize];
	        newCommands[0] = "\"";
	        int i = 1;	        
	        for (String cmd : command) {
	            newCommands[i] = cmd;
	            i++;
	        }
	        newCommands[newArraySize - 1] = "\"";
	        
	        return mergeCommands(Platform.getCmd(), newCommands);
		}
    	//win
*/        return mergeCommands(Platform.getCmd(), command);
    }

 /*   public static String[] createPlatformDependentCommandLine(String[] executable, String[] command) {
        String[] execCmd = insertCommandsAfter(Platform.getCmd(), executable);
        return mergeCommands(execCmd, command);
    }*/

    public static String[] insertCommandsAfter(String[] originalCmd, String... extraCommands) {
        return mergeCommands(originalCmd, extraCommands);
    }

    public static String[] insertCommandsBefore(String[] originalCmd, String... extraCommands) {
        return mergeCommands(extraCommands, originalCmd);
    }

    public static String[] mergeCommands(String[] cmd1, String[] cmd2) {
        int newArraySize = cmd1.length + cmd2.length;
        String[] newCommands = new String[newArraySize];
        int i = 0;
        for (String cmd : cmd1) {
            newCommands[i++] = cmd;
        }
        for (String cmd : cmd2) {
            newCommands[i++] = cmd;
        }
        return newCommands;
    }

    public static String arrayToString(String[] params) {
        StringBuilder b = new StringBuilder();
        for (String s : params) {
            b.append(s);
            b.append(" ");
        }
        return b.toString();
    }

    public static String listToString(List<String> params) {
        StringBuilder b = new StringBuilder();
        for (String s : params) {
            b.append(s);
            b.append(" ");
        }
        return b.toString();
    }

}