package ast;

public class Body extends Prog{
	
	public Body(Dclpart dclpart, CompStmt compstmt){
		this.dclpart = dclpart;
		this.compstmt = compstmt;
	}
	
	public Body(CompStmt compstmt){
		this.compstmt = compstmt;
	}
	
	public boolean hasSub(){
		return dclpart.hasSub();
	}
	
	public void genC(PW pw, boolean sub){
		if(dclpart != null)
			dclpart.genC(pw,sub);
		
		if(!sub){
			compstmt.genC(pw);
		}
	}
	
	private Dclpart dclpart;
	private CompStmt compstmt;
	
}