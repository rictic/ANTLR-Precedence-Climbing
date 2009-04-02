/** ANTLR v3 tree grammar to walk trees created by ANTLRv3.g */
tree grammar ExpressionCrawler;

options {
	filter='true';
	tokenVocab = ANTLRv3;
	ASTLabelType = CommonTree;
}

@members {
  public ArrayList<ExpressionRule> expressionRules = new ArrayList<ExpressionRule>();
}

topdown : rule ;  

rule
scope {
  String name;
  List<String> terminals;
}  : {$rule::terminals  = new ArrayList<String>();}
  ^( RULE v=ID {$rule::name=$v.text;} (ARG .*)? ('returns' .*)?
	  throwsSpec? opts=optionsSpec ('scope' .*)? ('@' ID ACTION)*
	  {$opts.isExpression}?
	  aL=altList
	  exceptionGroup?
	  EOR
	)
	  {System.err.println("should rewrite "+$rule::name);
	   System.err.println($aL.precOpers);
	   System.err.println($rule::terminals);
	   expressionRules.add(new ExpressionRule($rule::name, $rule::terminals, $aL.precOpers));
	   }
   	;

optionsSpec returns [boolean isExpression]
	: {isExpression = false;} ^(OPTIONS (opt=option {if ($opt.isExpression) isExpression = true;})+)
	;

option returns [boolean isExpression]
    : ^('=' key=ID val=optionValue)
    {if ($key.text.equals("strategy"))
      return $val.text.equals("tokenGrab");
     return false;}
 	;
 	
optionValue
    :   ID
    |   STRING_LITERAL
    |   CHAR_LITERAL
    |   INT
    ;


altList returns [List<List<Operator>> precOpers]
@init {
  $precOpers = new ArrayList<List<Operator>>();
}
    :   ^( BLOCK (a=alternative {$precOpers.add($a.opers);})+ EOB)
    ;

alternative returns [List<Operator> opers]
    :   ^(ALT  v=RULE_REF o=ops k=RULE_REF EOA) {
                              if ($v.text.equals($rule::name) && $k.text.equals($rule::name)) {
                                $opers = $o.opers;
                                for(Operator op : $opers)
                                  op.kind = Operator.Kind.Binary;
                              }
                                
                             }
    |   ^(ALT o=ops v=RULE_REF EOA) {if ($v.text.equals($rule::name)){
                                        $opers = $o.opers;
                                        for (Operator op : $opers)
                                          op.kind = Operator.Kind.Unary;
                                    }}
    |   ^(ALT terminals EOA)
    |   ^(ALT .* )
    ;

terminals : ((l=STRING_LITERAL | l=CHARLITERAL | l=TOKEN_REF){$rule::terminals.add($l.text);})+ ;

ops returns [List<Operator> opers]
@init {
  $opers = new ArrayList<Operator>();
}
    : o=op {$opers.add($o.oper);}
    | ^(BLOCK (^(ALT o=op{$opers.add($o.oper);} EOA))+ EOB)
    ;
op returns [Operator oper]
@after {
  $oper = new Operator(input.LA(-1), $l.text);
}   :
l=STRING_LITERAL | l=CHAR_LITERAL;

throwsSpec
	:	^('throws' ID+)
	;

exceptionGroup
	:	( exceptionHandler )+ ( finallyClause )?
	|	finallyClause
    ;

exceptionHandler
    :    ^('catch' ARG_ACTION ACTION)
    ;

finallyClause
    :    ^('finally' ACTION)
    ;
