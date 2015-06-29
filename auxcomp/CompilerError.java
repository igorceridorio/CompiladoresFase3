package auxcomp;

import java.io.PrintWriter;
import lexer.*;

public class CompilerError {

	public CompilerError(Lexer lexer, PrintWriter out) {
		this.lexer = lexer;
		this.out = out;
		thereWasAnError = false;
	}

	public void setLexer(Lexer lexer) {
		this.lexer = lexer;
	}

	public boolean wasAnErrorSignalled() {
		return thereWasAnError;
	}

	public void show(String strMessage) {
		show(strMessage, false);
	}

	public void show(String strMessage, boolean goPreviousToken) {
				
		if (goPreviousToken) {	
			System.out.println("Error at line " + lexer.getLineNumberBeforeLastToken() + ": ");
			System.out.println(lexer.getLineBeforeLastToken());
		} else {
			System.out.println("Error at line " + lexer.getLineNumber() + ": ");
			System.out.println(lexer.getCurrentLine());
		}
		
		//prints the error on screen
		System.out.println("Compilation error: " + strMessage);
		//prints the error in the out file
		out.println("Compilation error: " + strMessage);
		out.flush();
		if(out.checkError())
			System.out.println("Error in signaling an error");
		thereWasAnError = true;
	}
	
	public void signal(String strMessage) {
		show(strMessage);
		out.flush();
		thereWasAnError = true;
		throw new RuntimeException();
	}

	private Lexer lexer;
	private PrintWriter out;
	private boolean thereWasAnError;

}
