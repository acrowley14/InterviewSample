/*
 * Author: Alex Crowley
 * Date: 1/31/19
 * 
 * Notes: main method for testing is located at bottom of the program.
 */

package lineCounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LineCounter {
	
	// Count number of lines of Java code in a file
	
	public static int countLines(File file) {
		
		// Make sure file is a .java file
		
		if (notAJavaFile(file)) return 0;
		
		BufferedReader reader = null;
		int numLines = 0;
		
		try {

			reader = new BufferedReader(new FileReader(file));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			
			String line;
			
			// Boolean to indicate if current line is in a block comment 
			
			boolean in_block_comment = false;
			
			// Assess each line of file until reader indicates we are at the end
			
			while((line = reader.readLine()) != null) {
				
				LineStatus result = assessLineOfCode(line, in_block_comment);
				
				if (isLineOfJavaCode(result)) {
					numLines++;
				}
				
				/* If the line we just assessed started an unclosed block comment, or didn't close an
				 *  already open one, indicate that we're in a block comment */
				
				if (openCommentBlock(result)) {
					in_block_comment = true;
				} else {
					in_block_comment = false;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			try {
				
				reader.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return numLines;
	}
	
	private enum LineStatus {
		isJavaOpenComment, isNotJavaOpenComment, isJavaCloseComment, isNotJavaCloseComment;
	}
	
	// Checks to make sure signature of file is .java
	
	private static boolean notAJavaFile(File file) {
		String fname = file.getName();
		return !fname.endsWith(".java");
	}
	
	// Assess line of a file for Java code
	
	private static LineStatus assessLineOfCode(String line, boolean in_block_comment) {
		
		/* We'll iterate through line's characters, updating whether or not we've found Java code
		 * or entered a block comment by keeping track of whether or not last character was a * or /,
		 * as well as if we're in a String
		*/
		
		boolean inBlock = in_block_comment;
		boolean inString = false;
		boolean foundCode = false;
		boolean sawSlash = false;
		boolean sawStar = false;
		
		char[] chars = line.toCharArray();
		
		for (char c : chars) {
			if (!inBlock) {
				switch (c) {
					case '/': 
						if (sawSlash) {
							// Return if we find a // not in a block comment, since rest of line is commented out
							return getResultingStatus(foundCode, inBlock); 
						} else if (sawStar) {
							// We've terminated a block comment if we find a */ 
							inBlock = false;
						} else {
							sawSlash = true;
						}
					break;
					case '*': 
						if (sawSlash && inString == false) {
							// We've started a block comment if we find a /* that isn't in a string
							inBlock = true;
						} else {
							sawStar = true;
						}
					break;
					case '"':
						inString = !inString;
					break;
					default: 
						
						/* If we're not in a block comment, didn't see a * or /, and found an ASCII 
						* character greater than SPACE ---> then we found Java code! */
						
						if (c > 32) foundCode = true;
						sawStar = false;
						sawSlash = false;
					break;
				}
			} else {
				if (c == '/' && sawStar) inBlock = false;
				if (c == '*') {
					sawStar = true;
				} else {
					sawStar = false;
				}
			}
		}
		
		return getResultingStatus(foundCode, inBlock);
	}
	
	// A few helper methods to clean up LineStatus evaluations:
			
	private static boolean isLineOfJavaCode(LineStatus status) {
		return (status == LineStatus.isJavaOpenComment || status == LineStatus.isJavaCloseComment);
	}
	
	private static boolean openCommentBlock(LineStatus status) {
		return (status == LineStatus.isJavaOpenComment || status == LineStatus.isNotJavaOpenComment);
	}
	
	private static LineStatus getResultingStatus(boolean foundJavaCode, boolean in_block_comment) {
		if (foundJavaCode) {
			if (in_block_comment) {
				return LineStatus.isJavaOpenComment;
			} else {
				return LineStatus.isJavaCloseComment;
			}
		} else {
			if (in_block_comment) {
				return LineStatus.isNotJavaOpenComment;
			} else {
				return LineStatus.isNotJavaCloseComment;
			}
		}
	}
	
	public static void main(String[] args) {
		File testFile = new File("");
		System.out.println(countLines(testFile));
	}
}
