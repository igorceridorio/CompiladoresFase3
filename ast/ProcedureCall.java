package ast;

import java.util.ArrayList;

public class ProcedureCall extends Stmt {

	public ProcedureCall(String pid){
		this.pid = pid;
	}
	
	public ProcedureCall(String pid, ArrayList<Expr> exprlist){
		this.pid = pid;
		this.exprlist = exprlist;
	}
	
	public void genC(PW pw){
		pw.print(pid + " (");
		
		for(int i=0;i<exprlist.size();i++){  
			exprlist.get(i).genC(pw);
			if(i != (exprlist.size()-1))
				pw.out.print(", ");
		} 

		pw.out.println(");");
	}

	private String pid;
	private ArrayList<Expr> exprlist;
}
