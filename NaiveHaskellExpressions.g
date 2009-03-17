grammar HaskellExpressions;

options { output=AST; ASTLabelType=CommonTree; }

expressions : expression ('\n' expression)+ EOF;

expression
	:	expr0;
expr0	:	expr1(('$'|'$!')^ expr1)*;
expr1	:	expr2(('>>'|'>>=')^ expr2)*; //l, =<< r
expr2	:	expr3 ('||'^ expr2)*;//r
expr3	:	expr4 ('&&'^ expr3)*;//r
expr4	:	expr5 (('=='|'/='|'<'|'<='|'>='|'>')^ expr5)*;//neither
expr5	:	expr6 (':'^ expr5)*; //r
expr6	:	expr7 (('+'|'-')^ expr7)*; //l
expr7	:	expr8 (('*'|'/')^ expr8)*; //l
expr8	:	expr9 (('^'|'^^'|'**')^ expr8)*; //r
expr9	:	sExpr ('.'^ expr9)*; //r

sExpr	:	NUMBER 
        |   BOOL 
        |   ID
        |   '-'^ sExpr;

NUMBER	:	('0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9')+;
BOOL    :   'True' | 'False';
ID      :   'f'|'g'|'h';