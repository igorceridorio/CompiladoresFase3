package ast;

import java.util.ArrayList;

public class Function extends Subhead{
	
	public Function(String pid, ArrayList<Dcl> args, Type stdtype){
		this.pid = pid;
		this.args = args;
		this.stdtype = stdtype;
	}
	
	public Type getType(){
		return stdtype;
	}
	
	public void genC(PW pw){
		
		if (stdtype == Type.integerType)
			pw.print("int ");
		else if (stdtype == Type.realType)
			pw.print("float ");
		else if (stdtype == Type.charType)
			pw.print("char ");
		
		pw.out.print(pid + " (");
		
		for(int i=0;i<args.size();i++){  
			args.get(i).genC(pw);
			if(i != (args.size()-1))
				pw.out.print(", ");
		} 
		
		pw.out.println(" ) {");
		pw.add();
		
	}
	
	private String pid;
	private ArrayList<Dcl> args;
	private Type stdtype;

}
