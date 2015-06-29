package ast;

import java.util.ArrayList;

public class FunctionCall extends Expr{
	
	public FunctionCall(String pid, Type type, ArrayList<Expr> exprlist){
		this.pid = pid;
		this.type = type;
		this.exprlist = exprlist;
	}
	
	public Type getType(){
		return type;
	}
	
	public void genC(PW pw){
		pw.out.print(pid + " (");		
		for(int i=0;i<exprlist.size();i++){  
			exprlist.get(i).genC(pw);
			if(i != (exprlist.size()-1))
				pw.out.print(", ");
		} 
		
		pw.out.print(")");
	}

	private String pid;
	private ArrayList<Expr> exprlist;
	private Type type;

}
