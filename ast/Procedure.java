package ast;

import java.util.ArrayList;

public class Procedure extends Subhead{
	
	public Procedure(String pid, ArrayList<Dcl> args){
		this.pid = pid;
		this.args = args;
	}
	
	public void genC(PW pw){
		pw.print("void " + pid + " (");
		for(int i=0;i<args.size();i++){  
			args.get(i).genC(pw);
			if(i != (args.size()-1))
				pw.print(", ");
		} 
		
		pw.println(" ) {");
		pw.add();
	}
	
	private String pid;
	private ArrayList<Dcl> args;

}
