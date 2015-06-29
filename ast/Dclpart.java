package ast;

import java.util.ArrayList;

public class Dclpart{
	
	public Dclpart(ArrayList<Dcl> dcls, ArrayList<Subdcl> subdcls){
		this.dcls = dcls;
		this.subdcls = subdcls;
	}
	
	public boolean hasSub(){
		if(subdcls.isEmpty())
			return false;
		return true;
	}
	
	public void genC(PW pw, boolean sub){
		
		if(!sub){
			for(Dcl d: dcls){
				d.genC(pw);
				pw.out.println(";");
			}
			pw.out.println();
		}
		
		if(sub){
			for(Subdcl s: subdcls)
				s.genC(pw);			
		}
	}
	
	private ArrayList<Dcl> dcls;
	private ArrayList<Subdcl> subdcls;
	
}