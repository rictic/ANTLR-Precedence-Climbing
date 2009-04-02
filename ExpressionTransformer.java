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
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.TreeWizard;
import org.antlr.tool.*;
import org.antlr.stringtemplate.*;
import antlr.ANTLRParser;
import antlr.RecognitionException;
import antlr.TokenStreamException;

public class ExpressionTransformer {
  List<String> rulesToRemove = new ArrayList<String>();
  TreeWizard wiz;
  Grammar g;
  int insertLocation = 0;
  TokenRewriteStream tokens;
  List<ExpressionRule> expressions;
  public ExpressionTransformer(String grammarText) throws RecognitionException, TokenStreamException, org.antlr.runtime.RecognitionException {
    ANTLRv3Lexer lex = new ANTLRv3Lexer(new ANTLRStringStream(grammarText));
    CommonTokenStream tokens = new CommonTokenStream(lex);
    ANTLRv3Parser p = new ANTLRv3Parser(tokens);
    RuleReturnScope r = p.grammarDef();   
    CommonTree t = (CommonTree)r.getTree();
    System.err.println("tree: "+t.toStringTree());

    CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
    nodes.setTokenStream(tokens);
    ExpressionCrawler ref = new ExpressionCrawler(nodes);
    ref.downup(t);
    expressions = ref.expressionRules;
    
//    this.g = new Grammar(grammarText);
//    CharStream input = new ANTLRStringStream(grammarText);
//    Lexer l = new ANTLRv3Lexer(input);
//    tokens = new TokenRewriteStream(l);
  }
  
  public void printGrammar(String name, boolean buildTree, PrintStream out) throws FileNotFoundException {
    if (expressions.size() == 0)
      throw new RuntimeException("expected an expression to rewrite, found none");
    ExpressionRule rule = expressions.get(0); 
    StringTemplateGroup stg = new StringTemplateGroup(new FileReader("Greedy.stg"));
    StringTemplate header = stg.getInstanceOf("header");
    if (header == null) System.err.println("null!");
    if (tokens == null) System.err.println("tokens!");
    header.setAttribute("precedences", rule.precidenceOpers);
//     tokens.insertAfter(0, header.toString());
//     out.println(header.toString());
    StringTemplate ruleTemplate = stg.getInstanceOf("exprRule");
    ruleTemplate.setAttribute("name",rule.name);
    ruleTemplate.setAttribute("terminals", rule.terminals);
    
    List<String> binaryOps = new ArrayList<String>();
    List<String> unaryOps = new ArrayList<String>();
    for (List<Operator> ops : rule.precidenceOpers) {
      if (ops == null) continue;
      for (Operator op : ops) {
        if (op.kind == Operator.Kind.Binary)
          binaryOps.add(op.tokenText);
        else if (op.kind == Operator.Kind.Unary)
          unaryOps.add(op.tokenText);
      }
    }
    ruleTemplate.setAttribute("bops", binaryOps);
    ruleTemplate.setAttribute("uops", unaryOps);
    ruleTemplate.setAttribute("buildTree", buildTree);
//     tokens.insertAfter(insertLocation, rule.toString());
//     System.out.println(tokens.toString());
    
    System.out.println(header.toString());
    System.out.println(ruleTemplate.toString());
  }
  
  public void removeRule(GrammarAST rule) {
//    tokens.delete(rule.ruleStartTokenIndex, rule.ruleStopTokenIndex);
  }
  
  
  public static void main(String[] args) throws Exception {
    if (args.length != 1){
      System.err.println("usage: java ExpressionTransformer <filename>");
      System.exit(1);
    }
    String filename = args[0];
    
    ExpressionTransformer et = new ExpressionTransformer(readFileAsString(filename));
//    et.rewriteExpression("e");
//    System.err.println(et.precedences.toString());
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