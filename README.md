# CompiladoresFase3

Coding Project - Third Phase. Compilers course - 2015. Federal University of São Carlos - UFSCar Sorocaba.

### Developers: 

- Igor Felipe Ferreira Ceridório
- Daniel Ramos Miola

### Implemented Grammar:
```
- prog ::= P pid ';' body '.'

- body ::= [dclpart] compstmt
	
- dclpart ::= VAR dcls [subdcls] | subdcls
  
- dcls ::= dcl {dcl}
  
- dcl ::= idlist ’:’ type ’;’

- idlist ::= id {’,’ id}

- type ::= stdtype | arraytype

- stdtype ::= INTEGER | REAL | CHAR | STRING
 
- arraytype ::= ARRAY ’[’ intnum ’..’ intnum ’]’ OF stdtype

- subdcls ::= subdcl {subdcl}

- subdcl ::= subhead ’;’ body ’;’

- subhead ::= FUNCTION pid args ’:’ stdtype | PROCEDURE pid args

- args ::= ’(’ [dcls] ’)’

- compstmt ::= BEGIN stmts END

- stmts ::= stmt {’;’ stmt} ’;’

 stmt ::= ifstmt | whilestmt | assignstmt | compstmt | readstmt | writestmt | writelnstmt | returnstmt | procfuncstmt
  
- ifstmt ::= IF expr THEN stmts [ELSE stmts] ENDIF
  
- whilestmt ::= WHILE expr DO stmts ENDWHILE

- assignstmt ::= vbl ’:=’ expr

- readstmt ::= READ ’(’ vblist ’)’

- writestmt ::= WRITE ’(’ exprlist ’)’

- writelnstmt ::= WRITELN ’(’ [exprlist] ’)’

- returnstmt ::= RETURN [expr]

- procfuncstmt ::= pid ’(’ [exprlist] ’)’

- vblist ::= vbl {’,’ vbl}

 - vbl ::= id [’[’ expr ’]’]

- exprlist ::= expr {’,’ expr}

- expr ::= simexp [relop expr]

- simexp ::= [unary] term {addop term}

- term ::= factor {mulop factor}

- factor ::= vbl | num | ’(’ expr ’)’ | ’"’.’"’ | procfuncstmt

- id ::= letter {letter | digit}

- pid ::= letter {letter | digit}

- num ::= intnum [’.’ intnum]

- intnum ::= digit {digit}

- relop ::= ’=’ | ’<’ | ’>’ | ’<=’ | ’>=’ | ’<>’

- addop ::= ’+’ | ’-’ | OR

- mulop ::= ’*’ | ’/’ | AND | MOD | DIV

- unary ::= ’+’ | ’-’ | NOT
```
