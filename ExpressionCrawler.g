/** ANTLR v3 tree grammar to walk trees created by ANTLRv3.g */
tree grammar ExpressionCrawler;

options {
	filter='true';
	tokenVocab = ANTLRv3;
	ASTLabelType = CommonTree;
}

@members {
  public ArrayList<ExpressionRule> expressionRules = new ArrayList<ExpressionRule>();
  public boolean hasMembersSection = false;
  public int membersLocation = 0;
}

topdown : rule | grammarDef | action;

//Need to know where to put the members section
grammarDef
    :
      ^(
         (LEXER_GRAMMAR    
          	|   PARSER_GRAMMAR
          	|   TREE_GRAMMAR
          	|		COMBINED_GRAMMAR
          	)
         id DOC_COMMENT? ( ^(OPTIONS .*))? ( ^(TOKENS .*))?  
  		  //here's where a members section would go if there were to be one
      	{ membersLocation = ((CommonTree)input.LT(1)).getTokenStartIndex();}
  		  attrScope*
  		)
    ;


//Find expression rules and gather the necessary info about them
rule
scope {
  String name;
  List<String> terminals;
}
@init {
  CommonTree ruleTree = (CommonTree)input.LT(1);
  int startIndex = ruleTree.getTokenStartIndex();
  $rule::terminals  = new ArrayList<String>();

}
 : ^( RULE v=ID {$rule::name=$v.text;} (ARG .*)? ('returns' .*)?
	  (^('throws' .*))? opts=optionsSpec 
	  { if ($opts.isExpression) System.err.println(ruleTree.toStringTree());}
	  ('scope' .*)? ('@' ID ACTION)*
	  aL=altList
	  (^('catch' .*))* (^('finally' .*))?
	  EOR
	)
	{ if ($opts.isExpression) {
        rule_scope rs = (rule_scope)rule_stack.peek();
        System.err.println(aL);
        System.err.println(rs.terminals);
        expressionRules.add(new ExpressionRule(rs.name, rs.terminals, aL, startIndex, ((CommonTree)input.LT(1)).getTokenStartIndex()-1)); 
	  }
	}
   	;

optionsSpec returns [boolean isExpression]
	: {isExpression = false;} 
	  ^(OPTIONS (opt=option {if ($opt.isExpression) isExpression = true;})+)
	;

option returns [boolean isExpression]
    : ^('=' key=ID val=optionValue)
    {if ($key.text.equals("strategy"))
      return $val.text.equals("precedence");
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
    |   ^(ALT p=RULE_REF q=op t=RULE_REF c=op f=RULE_REF? EOA)
                      {if (  $p.text.equals($rule::name)
                           &&$t.text.equals($rule::name)
                           &&($f.text == null || $f.text.equals($rule::name))) {
                          opers = new ArrayList<Operator>();
                          $q.oper.ternary = $c.oper;
                          $q.oper.kind = Operator.Kind.TernaryPair;
                          opers.add($q.oper);
                       }
                      }
    |   ^(ALT terminals EOA)
    |   ^(ALT .* )
    ;

terminals : ((l=STRING_LITERAL | l=CHARLITERAL | l=TOKEN_REF | l=RULE_REF){$rule::terminals.add($l.text);})+ ;

ops returns [List<Operator> opers]
@init {
  $opers = new ArrayList<Operator>();
}
    : o=op {$opers.add($o.oper);}
    | ^(BLOCK 
        (^(ALT o=op{$opers.add($o.oper);} EOA))+ 
      EOB)
    ;
op returns [Operator oper]
@init {
  Operator.Associativity assoc = Operator.Associativity.Left;
  String opText = null;
}
@after {
  System.err.println("-!- " + opText);
  $oper = new Operator(input.LA(-1), opText, assoc);
}   : l=opVal {opText = $l.text;}
    | ^(l=opVal tops=tokenOptions 
        {assoc = $tops.assoc; opText = $l.text.substring(0,$l.text.lastIndexOf($tops.text)-1);});

opVal : STRING_LITERAL | CHAR_LITERAL | TOKEN_REF;

tokenOptions returns [Operator.Associativity assoc]
@init {$assoc = Operator.Associativity.Left;}
  : ^(OPTIONS 
      (o=tokenOption {if ($o.right == true) $assoc = Operator.Associativity.Right;})+
    );

tokenOption returns [boolean right]
@init {$right = false;}
  : ^('=' k=id v=optionValue) {
              if (  $k.text.equals("associativity")
                  &&$v.text.equals("right"))
                $right = true;}
  ;


attrScope
	:	^('scope' id ACTION);

/** Match stuff like @parser::members {int i;} */
action
	:	^('@' actionScopeName? kind=id {
	    String actionScope = $actionScopeName.text;
  	  if (actionScope == null || !actionScope.equals("lexer")){
  	    if ($kind.text.equals("members")){
    	    membersLocation = ((CommonTree)input.LT(1)).getTokenStartIndex();
    	    hasMembersSection = true;
  	    }
  	  }} 
	  ACTION) 
	;

actionScopeName :	id;

id	:	TOKEN_REF |	RULE_REF | ID;
