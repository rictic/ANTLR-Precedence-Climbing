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
  public ExpressionRule currentRule = null;
  private boolean isTopLevelAlternative() {
    return (currentRule != null) && ((CommonTree)input.LT(1)).parent.parent.getToken().getType() == RULE;
  }
  private boolean isE() {
    return ((CommonTree)input.LT(1)).getText().equals(currentRule.name);
  }
}


topdown : grammarDef | action | rule | eor
        | binary | ternary
        | simplePrefix | simpleSuffix
        | primary;

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
@init {
  CommonTree ruleTree = (CommonTree)input.LT(1);
}
 : ^( RULE v=ID (ARG .*)? ('returns' .*)?
	  (^('throws' .*))? opts=optionsSpec 
	  { if ($opts.isExpression) {
	    System.err.println(ruleTree.toStringTree());
	    currentRule = new ExpressionRule($v.text, ruleTree.getTokenStartIndex(), ruleTree.getTokenStopIndex());
	  } }
	  .* )
   	;

eor :
  EOR
  { if (currentRule != null) {
    expressionRules.add(currentRule);
    currentRule = null;
  }}
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


binary :  {isTopLevelAlternative()}?=>
          ^(ALT {isE()}?=> RULE_REF o=ops {isE()}?=> RULE_REF EOA) {
            for(Operator op : $o.opers)
              op.kind = Operator.Kind.Binary;
            currentRule.precidenceOpers.add($o.opers);
           };

ternary : {isTopLevelAlternative()}?=>
          ^(ALT {isE()}?=>RULE_REF q=op {isE()}?=>RULE_REF c=op f=RULE_REF? EOA) {
            if (($f.text == null || $f.text.equals(currentRule.name))) {
              List<Operator> opers = new ArrayList<Operator>();
              $q.oper.ternary = $c.oper;
              $q.oper.kind = Operator.Kind.TernaryPair;
              $q.oper.ternaryAfter = $f.text != null;
              opers.add($q.oper);
              currentRule.precidenceOpers.add(opers);
            }
            else
              currentRule.terminals.add($text);
            };


simplePrefix :  {isTopLevelAlternative()}?=>
                ^(ALT o=ops {isE()}?=>RULE_REF EOA) {
                   for (Operator op : $o.opers)
                     op.kind = Operator.Kind.Prefix;
                   currentRule.precidenceOpers.add($o.opers);
                };

simpleSuffix :  {isTopLevelAlternative()}?=>
                ^(ALT {isE()}?=>RULE_REF o=ops EOA) {
                  System.err.println($text);
                  for(Operator op : $o.opers)
                    op.kind = Operator.Kind.Suffix;
                  currentRule.precidenceOpers.add($o.opers);
                };


primary :  {isTopLevelAlternative()}?=> 
           ^(ALT .*) {
          currentRule.terminals.add($text);
        };



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
