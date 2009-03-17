grammar HaskellExpressions; //REMOVE when tree rewriting is working
@members {
public static final int LEFT = 1;
public static final int RIGHT = 2;
static int[] prec = new int[tokenNames.length];
static int[] uprec = new int[tokenNames.length];
static int[] postprec = new int[tokenNames.length];
static int[] assoc = new int[tokenNames.length];
static int lookupTokenFromGrammarString(String token) {
	for (int i = 0; i < tokenNames.length; i++)
		if (tokenNames[i].equals(token))
			return i;
	throw new RuntimeException("can't dereference token: " + token);
}
static {

           prec[lookupTokenFromGrammarString("'$'")] = 1;
   		assoc[lookupTokenFromGrammarString("'$'")] = LEFT;
           prec[lookupTokenFromGrammarString("'$!'")] = 1;
   		assoc[lookupTokenFromGrammarString("'$!'")] = LEFT;

           prec[lookupTokenFromGrammarString("'>>'")] = 2;
   		assoc[lookupTokenFromGrammarString("'>>'")] = LEFT;
           prec[lookupTokenFromGrammarString("'>>='")] = 2;
   		assoc[lookupTokenFromGrammarString("'>>='")] = LEFT;

           prec[lookupTokenFromGrammarString("'||'")] = 3;
   		assoc[lookupTokenFromGrammarString("'||'")] = LEFT;

           prec[lookupTokenFromGrammarString("'&&'")] = 4;
   		assoc[lookupTokenFromGrammarString("'&&'")] = LEFT;

           prec[lookupTokenFromGrammarString("'=='")] = 5;
   		assoc[lookupTokenFromGrammarString("'=='")] = LEFT;
           prec[lookupTokenFromGrammarString("'/='")] = 5;
   		assoc[lookupTokenFromGrammarString("'/='")] = LEFT;
           prec[lookupTokenFromGrammarString("'<'")] = 5;
   		assoc[lookupTokenFromGrammarString("'<'")] = LEFT;
           prec[lookupTokenFromGrammarString("'<='")] = 5;
   		assoc[lookupTokenFromGrammarString("'<='")] = LEFT;
           prec[lookupTokenFromGrammarString("'>='")] = 5;
   		assoc[lookupTokenFromGrammarString("'>='")] = LEFT;
           prec[lookupTokenFromGrammarString("'>'")] = 5;
   		assoc[lookupTokenFromGrammarString("'>'")] = LEFT;

           prec[lookupTokenFromGrammarString("':'")] = 6;
   		assoc[lookupTokenFromGrammarString("':'")] = LEFT;

           uprec[lookupTokenFromGrammarString("'-'")] = 7;
   		assoc[lookupTokenFromGrammarString("'-'")] = LEFT;

           prec[lookupTokenFromGrammarString("'+'")] = 8;
   		assoc[lookupTokenFromGrammarString("'+'")] = LEFT;
           prec[lookupTokenFromGrammarString("'-'")] = 8;
   		assoc[lookupTokenFromGrammarString("'-'")] = LEFT;

           prec[lookupTokenFromGrammarString("'*'")] = 9;
   		assoc[lookupTokenFromGrammarString("'*'")] = LEFT;
           prec[lookupTokenFromGrammarString("'/'")] = 9;
   		assoc[lookupTokenFromGrammarString("'/'")] = LEFT;

           prec[lookupTokenFromGrammarString("'^'")] = 10;
   		assoc[lookupTokenFromGrammarString("'^'")] = LEFT;
           prec[lookupTokenFromGrammarString("'^^'")] = 10;
   		assoc[lookupTokenFromGrammarString("'^^'")] = LEFT;
           prec[lookupTokenFromGrammarString("'**'")] = 10;
   		assoc[lookupTokenFromGrammarString("'**'")] = LEFT;

           prec[lookupTokenFromGrammarString("'.'")] = 11;
   		assoc[lookupTokenFromGrammarString("'.'")] = LEFT;



}
int nextp(int p) {
   int prevOpType = input.LA(-1);
   if ( assoc[prevOpType]==LEFT ) return prec[prevOpType]+1;
   else return prec[prevOpType];
}
}
e[int p]
   :   sExpr
       (   {prec[input.LA(1)]>=p}?=> ('$'|'$!'|'>>'|'>>='|'||'|'&&'|'=='|'/='|'<'|'<='|'>='|'>'|':'|'+'|'-'|'*'|'/'|'^'|'^^'|'**'|'.')^ e[nextp(p)]

       )*
   |   '-'^ {int q=uprec[input.LA(-1)];} e[q]
   ;

expressions : expression ('\n' expression)+ EOF;

expression
	:	e[0];

sExpr :	NUMBER 
      | BOOL 
      | ID;

NUMBER	: ('0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9')+;
BOOL    : 'True' | 'False';
ID      : 'f'|'g'|'h';
