package ast;

public class ReturnStmt extends Stmt{
	
	public ReturnStmt(Expr expr){
		this.expr = expr;
	}
	
	public ReturnStmt(){
	}
	
	public void genC(PW pw){
		pw.print("return ");
		if(expr != null)
			expr.genC(pw);
		pw.out.println(";");
	}

	private Expr expr;
}
