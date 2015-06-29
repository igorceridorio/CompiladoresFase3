package compiler;

import java.io.PrintWriter;
import java.util.ArrayList;

import lexer.*;
import ast.*;
import auxcomp.*;

public class Compiler {

	public Program compile(char[] input, PrintWriter outError) {
		Program p = null;
		
		symbolTable = new SymbolTable();
		main = true;
		isfunction = false;
		error = new CompilerError(lexer, new PrintWriter(outError));
		lexer = new Lexer(input, error);
		error.setLexer(lexer);
		lexer.nextToken();
		
		try {
			p = prog();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(error.wasAnErrorSignalled())
			return null;
		
		return p;
	}

	// prog ::= PROGRAM pid ';' body '.'
	private Program prog() {
		Body body = null;
		String pid = "";
		
		if (lexer.token == Symbol.PROGRAM) {
			
			lexer.nextToken();
			pid = pid();
			
			if (lexer.token == Symbol.SEMICOLON) {
				
				lexer.nextToken();
				body = body();
				
				if (lexer.token != Symbol.POINT)
					error.signal("Expecting '.'");
				lexer.nextToken();
			} else
				error.signal("Expecting ';'");
		} else
			error.signal("Expecting 'PROGRAM'");

		return new Program(pid, body);
	}

	// body::= [dclpart] compstmt
	private Body body() {
		Dclpart dclpart = null;
		CompStmt compstmt = null;

		if (lexer.token == Symbol.VAR || lexer.token == Symbol.FUNCTION || lexer.token == Symbol.PROCEDURE)
			dclpart = dclpart();
		compstmt = compstmt();

		if (dclpart != null){
			if(!main) symbolTable.removeLocalIdent();
			return new Body(dclpart, compstmt);
		}

		if(!main) symbolTable.removeLocalIdent();
		return new Body(compstmt);
	}

	// dclpart ::= VAR dcls [subdcls] | subdcls
	private Dclpart dclpart() {
		ArrayList<Dcl> dcls = new ArrayList<Dcl>();
		ArrayList<Subdcl> subdcls = new ArrayList<Subdcl>();
		
		if(lexer.token == Symbol.VAR){
			lexer.nextToken();
			dcls = dcls();
		}
		
		if((lexer.token == Symbol.FUNCTION || lexer.token == Symbol.PROCEDURE) && !isfunction )
			subdcls = subdcls();
		
		if((lexer.token == Symbol.FUNCTION || lexer.token == Symbol.PROCEDURE) && isfunction )
			error.signal("Nested FUNCTION or PROCEDURE.");

		return new Dclpart(dcls, subdcls);
	}

	// dcls ::= dcl {dcl}
	private ArrayList<Dcl> dcls() {
		ArrayList<Dcl> dcls = new ArrayList<Dcl>();

		dcls.add(dcl());
		while (lexer.token != Symbol.BEGIN && lexer.token != Symbol.FUNCTION && lexer.token != Symbol.PROCEDURE && lexer.token != Symbol.RIGHTPAR)
			dcls.add(dcl());

		return dcls;
	}

	// dcl ::= idlist ':' type ';'
	private Dcl dcl() {
		ArrayList<Variable> idlist = new ArrayList<Variable>();
		Type type = null;
		ArrayType arraytype = null;

		idlist = idlist();
		if (lexer.token == Symbol.COLON) {

			lexer.nextToken();
			if (lexer.token == Symbol.ARRAY) {
				arraytype = arraytype();

			} else {
				type = stdtype();
			}

			if (lexer.token != Symbol.SEMICOLON)
				error.signal("Expecting ';'");
			lexer.nextToken();

		} else {
			error.signal("Expecting ':'");
		}

		if (arraytype != null) {

			for (Variable v : idlist)
				v.setType(arraytype.getType());

			return new Dcl(idlist, arraytype);
		}

		for (Variable v : idlist)
			v.setType(type);

		return new Dcl(idlist, type);
	}

	// idlist ::= id {',' id}
	private ArrayList<Variable> idlist() {
		ArrayList<Variable> idlist = new ArrayList<Variable>();

		if (lexer.token != Symbol.IDPID)
			error.signal("Expecting identifier");

		String name = id();
		if(main){
			if (symbolTable.getInMain(name) != null)
				error.signal("Variable " + name + " has already been declared");
		}else{
			if (symbolTable.getInLocal(name) != null)
				error.signal("Variable " + name + " has already been declared in scope");
		} 
		
		Variable v = new Variable(name);
		if(main) symbolTable.putInMain(name, v); 
		else symbolTable.putInLocal(name, v);
		idlist.add(v);

		while (lexer.token == Symbol.COMMA) {
			lexer.nextToken();

			if (lexer.token != Symbol.IDPID)
				error.signal("Expecting identifier2");

			name = id();
			if(main){
				if (symbolTable.getInMain(name) != null)
					error.signal("Variable " + name + " has already been declared");
			}else{
				if (symbolTable.getInLocal(name) != null)
					error.signal("Variable " + name + " has already been declared in scope");
			}

			v = new Variable(name);
			if(main) symbolTable.putInMain(name, v); 
			else symbolTable.putInLocal(name, v);
			idlist.add(v);
		}

		return idlist;
	}

	// stdtype ::= INTEGER | REAL | CHAR | STRING
	private Type stdtype() {
		if (lexer.token == Symbol.INTEGER) {
			lexer.nextToken();
			return Type.integerType;
		} else if (lexer.token == Symbol.REAL) {
			lexer.nextToken();
			return Type.realType;
		} else if (lexer.token == Symbol.CHAR) {
			lexer.nextToken();
			return Type.charType;
		} else if (lexer.token == Symbol.STRING) {
			lexer.nextToken();
			return Type.stringType;
		} else
			error.signal("Expecting type.");
		return null;
	}

	// arraytype ::= ARRAY '[' intnum '..' intnum ']' OF stdtype
	private ArrayType arraytype() {
		int intnum = 0, intnum2 = 0;
		Type stdtype = null;

		lexer.nextToken();
		if (lexer.token == Symbol.LEFTBRA) {
			lexer.nextToken();
			if (lexer.token == Symbol.INTNUM) {
				intnum = lexer.getNumberValue();
				lexer.nextToken();
				if (lexer.token == Symbol.POINT) {
					lexer.nextToken();
					if (lexer.token == Symbol.POINT) {
						lexer.nextToken();
						if (lexer.token == Symbol.INTNUM) {
							intnum2 = lexer.getNumberValue();
							lexer.nextToken();
							if (lexer.token == Symbol.RIGHTBRA) {
								lexer.nextToken();
								if (lexer.token == Symbol.OF) {
									lexer.nextToken();
									stdtype = stdtype();
								} else
									error.signal("Expecting 'OF'");
							} else
								error.signal("Expecting ']'");
						} else
							error.signal("Expecting final index of array");
					} else
						error.signal("Expecting '.'");
				} else
					error.signal("Expecting '.'");
			} else
				error.signal("Expecting initial index of array");
		} else
			error.signal("Expecting '['");

		return new ArrayType(intnum, intnum2, stdtype);
	}
	
	// subdcls ::= subdcl {subdcl}
	private ArrayList<Subdcl> subdcls() {
		ArrayList<Subdcl> subdcls = new ArrayList<Subdcl>();

		subdcls.add(subdcl());
		while (lexer.token == Symbol.FUNCTION || lexer.token == Symbol.PROCEDURE)
			subdcls.add(subdcl());

		return subdcls;
	}
	
	// subdcl ::= subhead ';' body ';'
	private Subdcl subdcl() {
		main = false;
		Subhead subhead = subhead();
		Body body = null;
		
		if(lexer.token != Symbol.SEMICOLON)
			error.signal("Expecting ';'");
		else{
			lexer.nextToken();			
			body = body();
			isfunction = false;
			
			if(lexer.token != Symbol.SEMICOLON)
				error.signal("Expecting ';'");
			else {
				lexer.nextToken();
			}
		}
		main = true;
		
		return new Subdcl(subhead, body);
	}
	
	// subhead ::= FUNCTION pid args �:� stdtype | PROCEDURE pid args
	private Subhead subhead() {
		String pid = null;
		ArrayList<Dcl> args = new ArrayList<Dcl>();
		Type stdtype = null;
		
		if(lexer.token == Symbol.FUNCTION)
			isfunction = true;
		else if (lexer.token == Symbol.PROCEDURE)
			isfunction = false;
		else
			error.signal("Expecting 'FUNCTION' or 'PROCEDURE' statement");
			
		lexer.nextToken();
		pid = pid();
		if(symbolTable.getInGlobal(pid) != null)
			error.signal("FUNCTION or PROCEDURE "+ pid +" has already been declared");		
		args = args();
			
		if(lexer.token == Symbol.COLON && isfunction){
			lexer.nextToken();
			stdtype = stdtype();
		} else if(lexer.token != Symbol.COLON && isfunction){
			error.signal("Expecting ':'");
		}
		
		if(isfunction){
			Function f = new Function(pid, args, stdtype);
			symbolTable.putInGlobal(pid, f);
			return f;
		}else{
			Procedure p = new Procedure(pid, args);
			symbolTable.putInGlobal(pid, p);
			return p;
		}
	}
	
	// args ::= �(� [dcls] �)�
	private ArrayList<Dcl> args() {		
		ArrayList<Dcl> dcls = new ArrayList<Dcl>();
		
		if(lexer.token != Symbol.LEFTPAR)
			error.signal("Expecting '('");
		else {
			lexer.nextToken();
			if(lexer.token == Symbol.IDPID)
				dcls = dcls();
			
			if(lexer.token != Symbol.RIGHTPAR)
				error.signal("Expecting ')'");
			else
				lexer.nextToken();
		}
		
		return dcls;
	}

	// compstmt ::= BEGIN stmts END
	private CompStmt compstmt() {
		ArrayList<Stmt> stmts = new ArrayList<Stmt>();

		if (lexer.token == Symbol.BEGIN) {
			lexer.nextToken();
			stmts = stmts();
			if (lexer.token == Symbol.END)
				lexer.nextToken();
			else
				error.signal("Expecting 'END'");
		} else
			error.signal("Expecting 'BEGIN'");

		return new CompStmt(stmts);
	}

	// stmts ::= stmt {';' stmt} ';'
	private ArrayList<Stmt> stmts() {
		ArrayList<Stmt> stmts = new ArrayList<Stmt>();

		stmts.add(stmt());

		if (lexer.token == Symbol.SEMICOLON) {
			lexer.nextToken();
			while (lexer.token != Symbol.END && lexer.token != Symbol.ENDIF
					&& lexer.token != Symbol.ENDWHILE
					&& lexer.token != Symbol.ELSE) {
				stmts.add(stmt());
				if (lexer.token != Symbol.SEMICOLON){
					error.signal("Expecting ';'");
				}else if (lexer.token == Symbol.SEMICOLON)
					lexer.nextToken();
			}
		} else {
			System.out.println(lexer.getStringValue());
			error.signal("Expecting ';'");
		}

		return stmts;
	}

	// stmt ::= ifstmt | whilestmt | assignstmt | compstmt | readstmt |
	// writestmt | writelnstmt | returnstmt | procfuncstmt
	private Stmt stmt() {
		switch (lexer.token) {
		case IF:
			return ifstmt();
		case WHILE:
			return whilestmt();
		case IDPID:
			if (symbolTable.get(lexer.getStringValue()) instanceof Procedure){
				return (ProcedureCall) procfuncstmt();
			}else if (symbolTable.get(lexer.getStringValue()) instanceof Procedure)
				error.signal("Function's return is not being used.");
			else
				return assignstmt();
			break;
		case BEGIN:
			return compstmt();
		case READ:
			return readstmt();
		case WRITE:
			return writestmt();
		case WRITELN:
			return writelnstmt();
		case RETURN:
			return returnstmt();
		default:
			error.signal("Statement expected");
		}
		return null;
	}

	// ifstmt ::= IF expr THEN stmts [ELSE stmts] ENDIF
	private IfStmt ifstmt() {
		Expr expr;
		ArrayList<Stmt> thenPart = new ArrayList<Stmt>();
		ArrayList<Stmt> elsePart = new ArrayList<Stmt>();
		StmtList thenPartList, elsePartList;

		lexer.nextToken();
		expr = expr();

		// verifies if the expression is of boolean type
		if (expr.getType() != Type.booleanType)
			error.signal("Boolean type expected in if expression");

		// iterates the token to the THEN part
		if (lexer.token != Symbol.THEN)
			error.signal("Expecting 'THEN'");
		lexer.nextToken();

		thenPart = stmts();

		// iterates the token to the ELSE part (if exists)
		if (lexer.token == Symbol.ELSE) {
			lexer.nextToken();
			elsePart = stmts();
		}

		// iterates to the ENDIF part
		if (lexer.token != Symbol.ENDIF)
			error.signal("Expecting 'ENDIF'");
		lexer.nextToken();

		thenPartList = new StmtList(thenPart);
		elsePartList = new StmtList(elsePart);

		return new IfStmt(expr, thenPartList, elsePartList);
	}

	// whilestmt ::= WHILE expr DO stmts ENDWHILE
	private WhileStmt whilestmt() {
		Expr expr;
		ArrayList<Stmt> whilePart = new ArrayList<Stmt>();
		StmtList whilePartList;

		lexer.nextToken();
		expr = expr();

		// verifies if the expression is of boolean type
		if (expr.getType() != Type.booleanType)
			error.signal("Boolean type expected in if expression");

		// iterates the token to the DO part
		if (lexer.token != Symbol.DO)
			error.signal("Expecting 'DO'");
		lexer.nextToken();
		whilePart = stmts();

		// iterates to the ENDWHILE part
		if (lexer.token != Symbol.ENDWHILE)
			error.signal("Expecting 'ENDWHILE'");
		lexer.nextToken();

		whilePartList = new StmtList(whilePart);

		return new WhileStmt(expr, whilePartList);
	}

	// assignstmt ::= vbl ':=' expr
	private AssignStmt assignstmt() {
		VblExpr vbl;
		Expr expr;

		vbl = vbl();

		// iterates to the EQUAL part
		if (lexer.token != Symbol.EQUAL)
			error.signal("Expecting ':='");
		lexer.nextToken();

		expr = expr();

		// verifies if the types match
		if (vbl.getType() == Type.charType && expr.getType() != Type.charType)
			error.signal("Type error in assignment");
		else if (vbl.getType() == Type.stringType && ( expr.getType() != Type.charType && expr.getType() != Type.stringType ))
			error.signal("Type error in assignment");
	
		if ( (vbl.getType() != Type.charType && vbl.getType() != Type.stringType) && (vbl.getType() == Type.integerType && expr.getType() == Type.realType) )
			error.signal("Type error in assignment");

		return new AssignStmt(vbl, expr);
	}

	// readstmt ::= READ '(' vblist ')'
	private ReadStmt readstmt() {
		ArrayList<VblExpr> vblist = new ArrayList<VblExpr>();

		lexer.nextToken();
		if (lexer.token != Symbol.LEFTPAR)
			error.signal("Expecting '('");
		lexer.nextToken();
		vblist = vblist();

		if (lexer.token != Symbol.RIGHTPAR)
			error.signal("Expecting ')'");
		lexer.nextToken();

		return new ReadStmt(vblist);
	}

	// writestmt ::= WRITE '(' exprlist ')'
	private WriteStmt writestmt() {
		ArrayList<Expr> exprlist = new ArrayList<Expr>();

		lexer.nextToken();

		if (lexer.token != Symbol.LEFTPAR)
			error.signal("Expecting '('");

		lexer.nextToken();
		exprlist = exprlist();

		if (lexer.token != Symbol.RIGHTPAR)
			error.signal("Expecting ')'");
		lexer.nextToken();

		return new WriteStmt(exprlist);
	}

	// writelnstmt ::= WRITELN '(' [exprlist] ')'
	private WritelnStmt writelnstmt() {
		ArrayList<Expr> exprlist = new ArrayList<Expr>();

		lexer.nextToken();
		if (lexer.token != Symbol.LEFTPAR)
			error.signal("Expecting '('");

		lexer.nextToken();
		if (lexer.token != Symbol.RIGHTPAR){
			exprlist = exprlist();
			lexer.nextToken();
			return new WritelnStmt(exprlist);
		}

		if (lexer.token != Symbol.RIGHTPAR)
			error.signal("Expecting ')'");
		
		lexer.nextToken();

		return new WritelnStmt();
	}
	
	// returnstmt ::= RETURN [expr]]
	private ReturnStmt returnstmt() {
		if(lexer.token != Symbol.RETURN)
			error.signal("Expecting 'RETURN'");
		else{
			lexer.nextToken();			
		}
		
		if(lexer.token != Symbol.SEMICOLON && isfunction)
			return new ReturnStmt(expr());
		else if (lexer.token != Symbol.SEMICOLON && !isfunction)
			error.signal("PROCEDURES should not return an EXPRESSION.");
		return new ReturnStmt();
	}
	
	// procfuncstmt ::= pid '(' [exprlist] ')'
	private Object procfuncstmt() {
		String pid;
		ArrayList<Expr> exprlist = new ArrayList<Expr>();
		
		if(lexer.token != Symbol.IDPID)
				error.signal("Expecting a PROCEDURE or FUNCTION call");
		else{
			pid = lexer.getStringValue();
			Subhead s = (Subhead)symbolTable.getInGlobal(pid);
			if(s != null){
				lexer.nextToken();
				
				if(lexer.token != Symbol.LEFTPAR)
					error.signal("Expecting a '('");
				else{
					lexer.nextToken();
					if(lexer.token != Symbol.RIGHTPAR)
						exprlist = exprlist();
					lexer.nextToken();
				}
				
				if(s instanceof Procedure)
					return new ProcedureCall(pid, exprlist);
				else{
					Type type = ((Function)s).getType();
					return new FunctionCall(pid, type, exprlist);
				}
			}else
				error.signal(pid+" PROCEDURE or FUNCTION was not declared.");
		}
		return null;
	}

	// vblist ::= vbl {',' vbl}
	private ArrayList<VblExpr> vblist() {
		ArrayList<VblExpr> vblist = new ArrayList<VblExpr>();

		vblist.add(vbl());

		while (lexer.token == Symbol.COMMA) {
			lexer.nextToken();
			vblist.add(vbl());
		}

		return vblist;
	}

	// vbl ::= id ['[' expr ']']
	private VblExpr vbl() {
		Variable id = null;
		Expr expr = null;

		// verifies if the variable has been declared
		String name = lexer.getStringValue();
		if(main) id = (Variable) symbolTable.getInMain(name);
		else	 id = (Variable) symbolTable.getInLocal(name);
		if (id == null){
			Subhead s = (Subhead) symbolTable.getInGlobal(name);
			if(s == null )
				error.signal("Variable " + name + " was not declared");
			else
				error.signal("Return of FUNCTION " + name + " must be assigned.");
		}
		lexer.nextToken();

		// verifies if the vbl has a expr part
		if (lexer.token == Symbol.LEFTBRA) {
			lexer.nextToken();
			expr = expr();

			// semantic analysis of the type of the expression
			if (expr.getType() != Type.integerType)
				error.signal("Expecting an integer type expression");

			if (lexer.token != Symbol.RIGHTBRA)
				error.signal("Expecting ']'");
			lexer.nextToken();
			return new VblExpr(id, expr);
		}

		return new VblExpr(id);
	}

	// exprlist ::= expr {',' expr}
	private ArrayList<Expr> exprlist() {
		ArrayList<Expr> exprlist = new ArrayList<Expr>();

		exprlist.add(expr());

		while (lexer.token == Symbol.COMMA) {
			lexer.nextToken();
			exprlist.add(expr());
		}

		return exprlist;
	}

	// expr ::= simexp [relop expr]
	private Expr expr() {
		Expr left = null, right = null;
		Symbol relop = null;

		left = simexp();
		relop = relop();
		if (relop != null) {
			right = expr();

			left = new CompositeExpr(left, relop, right);
		}

		return left;
	}

	// simexp ::= [unary] term {addop term}
	private Expr simexp() {
		Expr left = null, right = null;
		Symbol unary = null, addop = null;

		unary = unary();
		left = term();
		if (unary != null)
			left = new UnaryExpr(left, unary);
		else {
			addop = addop();
			while (addop != null) {
				right = term();
				
				// verifies if the types match
				if (left.getType() == Type.integerType && right.getType() != Type.integerType)
					error.signal("Type error in expression");
				else if (left.getType() == Type.realType && ( right.getType() != Type.realType && right.getType() != Type.integerType ))
					error.signal("Type error in expression");
				
				left = new CompositeExpr(left, addop, right);
				addop = addop();
			}
		}

		return left;
	}

	// term ::= factor {mulop factor}
	private Expr term() {
		Expr left = null, right = null;
		Symbol mulop = null;

		left = factor();
		mulop = mulop();
		

		while (mulop != null) {
			right = factor();
			left = new CompositeExpr(left, mulop, right);
			mulop = mulop();
		}

		return left;
	}

	// factor ::= vbl | num | '(' expr ')' | '''.''' | procfuncstmt
	private Expr factor() {

		Expr factor = null;

		if (lexer.token == Symbol.IDPID){
			if (symbolTable.getInGlobal(lexer.getStringValue()) instanceof Function){
				factor = (FunctionCall) procfuncstmt();
			}else if (symbolTable.getInGlobal(lexer.getStringValue()) instanceof Procedure)
				error.signal("PROCEDURE");
			else
				factor = vbl();
		}else if (lexer.token == Symbol.PLUS || lexer.token == Symbol.MINUS
				|| lexer.token == Symbol.INTNUM)
			factor = num();
		else if (lexer.token == Symbol.LEFTPAR) {
			lexer.nextToken();
			factor = new ParenthesisExpr(expr());
			if (lexer.token != Symbol.RIGHTPAR)
				error.signal("Expecting ')'");
			lexer.nextToken();
		} else if (lexer.token == Symbol.DOUBLEQUOTES) {
			lexer.nextToken();
			if (lexer.getStringValue().length() == 1)
				factor = new CharExpr(lexer.getStringValue());
			else
				factor = new StringExpr(lexer.getStringValue());
		} else {
			error.signal("Expecting expression");
		}
			
		return factor;
	}

	// id ::= letter {letter | digit}
	private String id() {
		String s = lexer.getStringValue();
		lexer.nextToken();
		return s;
	}

	// pid ::= letter {letter | digit}
	private String pid() {
		String s = lexer.getStringValue();
		lexer.nextToken();
		return s;
	}

	// num ::= ['+' | '-'] digit ['.'] {digit}
	private Expr num() {
		StringBuffer s = new StringBuffer();
		Expr num = null;

		if (lexer.token == Symbol.PLUS || lexer.token == Symbol.MINUS) {
			if (lexer.token == Symbol.MINUS)
				s.append("-");
			lexer.nextToken();
		}

		if (lexer.token == Symbol.INTNUM) {
			s.append(Integer.toString(lexer.getNumberValue()));
			lexer.nextToken();
		} else
			error.signal("Expecting a digit");

		if (lexer.token == Symbol.POINT) {
			s.append(".");
			lexer.nextToken();
			if (lexer.token == Symbol.INTNUM) {
				s.append(Integer.toString(lexer.getNumberValue()));
				lexer.nextToken();
				num = new NumberFloatExpr(Float.valueOf(s.toString())
						.floatValue());
			}
		} else
			return new NumberIntExpr(Integer.valueOf(s.toString()).intValue());

		return num;
	}

	// relop ::= '=' | '<' | '>' | '<=' | '>=' | '<>'
	private Symbol relop() {
		Symbol oper = null;

		if (lexer.token == Symbol.ASSIGN) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.LT) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.GT) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.LE) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.GE) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.DIFF) {
			oper = lexer.token;
			lexer.nextToken();
		}

		return oper;
	}

	// addop ::= '+' | '-' | OR
	private Symbol addop() {
		Symbol oper = null;

		if (lexer.token == Symbol.PLUS) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.MINUS) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.OR) {
			oper = lexer.token;
			lexer.nextToken();
		}

		return oper;
	}

	// mulop ::= '*' | '/' | AND | MOD | DIV
	private Symbol mulop() {
		Symbol oper = null;

		if (lexer.token == Symbol.MULTIPLICATION) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.DIVISION) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.AND) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.MOD) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.DIV) {
			oper = lexer.token;
			lexer.nextToken();
		}

		return oper;
	}

	// unary ::= '+' | '-' | NOT
	private Symbol unary() {
		Symbol oper = null;

		if (lexer.token == Symbol.PLUS) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.MINUS) {
			oper = lexer.token;
			lexer.nextToken();
		} else if (lexer.token == Symbol.NOT) {
			oper = lexer.token;
			lexer.nextToken();
		}

		return oper;
	}

	private SymbolTable symbolTable;
	private Lexer lexer;
	private CompilerError error;
	private boolean main;
	private boolean isfunction;

}
