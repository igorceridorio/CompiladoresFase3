package ast;

public class Subdcl {
	
	public Subdcl(Subhead subhead, Body body){
		this.subhead = subhead;
		this.body = body;
	}

	public void genC(PW pw){
		subhead.genC(pw);
		body.genC(pw, false);
		pw.sub();
		pw.println("}");
		pw.out.println();
	}
	
	private Subhead subhead;
	private Body body;
}
