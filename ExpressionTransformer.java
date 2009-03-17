import java.io.*;
import java.util.*;

import org.antlr.grammar.v3.ANTLRv3Lexer;
import org.antlr.grammar.v3.ANTLRv3Parser;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeWizard;
import org.antlr.tool.*;
import org.antlr.stringtemplate.*;
import antlr.ANTLRParser;
import antlr.RecognitionException;
import antlr.TokenStreamException;

public class ExpressionTransformer {
  String eName;
  List<List<Precedence>> precedences = new ArrayList<List<Precedence>>();
  List<GrammarAST> terminals;
  List<String> rulesToRemove = new ArrayList<String>();
  TreeWizard wiz;
  Grammar g;
  List<String> binaryOps = new ArrayList<String>();
  List<String> unaryOps = new ArrayList<String>();
  List<Precedence> precedence;
  int insertLocation = 0;
  TokenRewriteStream tokens;
  public ExpressionTransformer(String grammarText) throws RecognitionException, TokenStreamException, org.antlr.runtime.RecognitionException {
    ANTLRv3Lexer lex = new ANTLRv3Lexer(new ANTLRStringStream(grammarText));
    CommonTokenStream tokens = new CommonTokenStream(lex);
    ANTLRv3Parser p = new ANTLRv3Parser(tokens);
    RuleReturnScope r = p.grammarDef();   
    CommonTree t = (CommonTree)r.getTree();
    System.err.println("tree: "+t.toStringTree());

    this.g = new Grammar(grammarText);
    CharStream input = new ANTLRStringStream(grammarText);
    Lexer l = new ANTLRv3Lexer(input);
    tokens = new TokenRewriteStream(l);
  }
  
  public void printGrammar(String name, boolean buildTree, PrintStream out) throws FileNotFoundException {
    StringTemplateGroup stg = new StringTemplateGroup(new FileReader("Greedy.stg"));
    StringTemplate header = stg.getInstanceOf("header");
    if (header == null) System.err.println("null!");
    if (tokens == null) System.err.println("tokens!");
    header.setAttribute("precedences", precedences);
//     tokens.insertAfter(0, header.toString());
//     out.println(header.toString());
    StringTemplate rule = stg.getInstanceOf("exprRule");
    rule.setAttribute("name",eName);
    rule.setAttribute("terminal", "sExpr");
    rule.setAttribute("bops", binaryOps);
    rule.setAttribute("uops", unaryOps);
    rule.setAttribute("buildTree", buildTree);
//     tokens.insertAfter(insertLocation, rule.toString());
//     System.out.println(tokens.toString());
    
    System.out.println(header.toString());
    System.out.println(rule.toString());
  }
  
  
  public void rewriteExpression(String eName) {
    this.eName = eName;
    
    this.wiz = new TreeWizard(new CommonTreeAdaptor(), ANTLRParser._tokenNames);
    GrammarAST t = g.getRule(eName).tree.getChild(4);
    insertLocation = t.ruleStartTokenIndex;
    removeRule(t);
    g.getActions().remove(eName);
    for (GrammarAST alt : t.getChildrenAsArray()) {
      System.err.println(alt.toStringTree());
      precedence = new ArrayList<Precedence>();
      precedences.add(precedence);
      handleAlt(alt);
      System.err.println("");
    }
  }
  
  public void handleAlt(GrammarAST alt) {
    GrammarAST[] children = alt.getChildrenAsArray();
    if (children.length < 2) return;
    for (GrammarAST el : children)
      System.err.println("  "+el.toStringTree());
    if (children.length == 2){
      
    }  
    if (children[0].getText().equals(eName) 
      &&children[children.length-2].getText().equals(eName)){
      addBinaryOps(children[1]);
    }
    if (children[1].getText().equals(eName)){
      System.err.println("uop found");
      addUnaryOps(children[0]);
    }
  }
  
  public void removeRule(GrammarAST rule) {
//    tokens.delete(rule.ruleStartTokenIndex, rule.ruleStopTokenIndex);
  }
  
  public void addBinaryOps(GrammarAST bopTree) {
    addOps(Operator.Binary, bopTree);
  }
  
  public void addUnaryOps(GrammarAST uopTree) {
    addOps(Operator.Unary, uopTree);
  }
  
  public void addOps(Operator kind, GrammarAST tree) {
    if (tree.getNumberOfChildren() == 0) {
      addPrecedence(kind, tree.getText());
      return;
    }
    GrammarAST[] bops = tree.getChildrenAsArray();
    
    for (int i = 0; i < bops.length -1; i++)
      addPrecedence(kind, bops[i].getChild(0).getText());
//    System.err.println("!!! " + bops.toStringTree() + " - " + bops.getChildrenAsArray().length);
  }

  
  
  private void addPrecedence(Operator kind, String text) {
    precedence.add(new Precedence(kind, g.getTokenType(text),text));
    if (kind == Operator.Binary)
      binaryOps.add(text);
    if (kind == Operator.Unary)
      unaryOps.add(text);
  }

  public enum Operator {Unary, Binary, TernaryPair};

  class Precedence {
    Operator kind;
    public int tokenType;
    public String tokenText;
    int assoc;
    public boolean isBinary() {
      return kind == Operator.Binary;
    }
    public boolean isUnary() {
      return kind == Operator.Unary;
    }
    public boolean isRightAssoc() {
      return assoc == RIGHT;
    }
    public boolean isLeftAssoc() {
      return assoc == LEFT;
    }
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public Precedence(Operator kind, int tokenType, String tokenText) {
      this.kind = kind; this.tokenType = tokenType; this.assoc=LEFT; this.tokenText = tokenText;
    }
    public String toString() {
      String result = kind == Operator.Binary ? "B " : "U ";
      return result + '"' + tokenText + '"'; 
    }
  }
  
  
  public static void main(String[] args) throws Exception {
    if (args.length != 1){
      System.err.println("usage: java ExpressionTransformer <filename>");
      System.exit(1);
    }
    String filename = args[0];
    
    ExpressionTransformer et = new ExpressionTransformer(readFileAsString(filename));
    et.rewriteExpression("e");
    System.err.println(et.precedences.toString());
    et.printGrammar(filename.subSequence(0, filename.indexOf('.')).toString(), true, System.out);
  }
  
  
  
  
  
  
  //stolen from the internet, copyright unclean
  private static String readFileAsString(String filePath) throws java.io.IOException{
      StringBuffer fileData = new StringBuffer(1000);
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      char[] buf = new char[1024];
      int numRead=0;
      while((numRead=reader.read(buf)) != -1){
          fileData.append(buf, 0, numRead);
      }
      reader.close();
      return fileData.toString();
  }
}