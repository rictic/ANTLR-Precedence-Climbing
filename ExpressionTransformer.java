import java.io.*;
import java.util.*;

import org.antlr.grammar.v3.ANTLRv3Lexer;
import org.antlr.grammar.v3.ANTLRv3Parser;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.stringtemplate.*;
import antlr.RecognitionException;
import antlr.TokenStreamException;

public class ExpressionTransformer {
  int membersLocation;
  boolean hasMembersSection;
  TokenRewriteStream tokens;
  List<ExpressionRule> expressions;
  
  public ExpressionTransformer(String grammarText) throws RecognitionException, TokenStreamException, org.antlr.runtime.RecognitionException {
    ANTLRv3Lexer lex = new ANTLRv3Lexer(new ANTLRStringStream(grammarText));
    tokens = new TokenRewriteStream(lex);
    ANTLRv3Parser p = new ANTLRv3Parser(tokens);
    RuleReturnScope r = p.grammarDef();   
    CommonTree t = (CommonTree)r.getTree();
    CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
    nodes.setTokenStream(tokens);
    ExpressionCrawler ref = new ExpressionCrawler(nodes);
    ref.downup(t);
    expressions = ref.expressionRules;
    membersLocation = ref.membersLocation;
    hasMembersSection = ref.hasMembersSection;
  }
  
  public void printGrammar(boolean buildTree, PrintStream out) throws FileNotFoundException {
    if (expressions.size() == 0){
      System.err.println("No valid expressions found");
      out.print(tokens);
      return;
    }
    
    
    StringTemplateGroup stg = new StringTemplateGroup(new FileReader("Greedy.stg"));
    
    String membersText;
    if (!hasMembersSection)
       membersText = "@members {";
    else {
      membersText = tokens.get(membersLocation).getText();
      //remove the closing bracket
      membersText = membersText.substring(0,membersText.lastIndexOf('}'));
      tokens.delete(membersLocation);
    }
    StringTemplate staticHeader = stg.getInstanceOf("staticHeader");
    membersText += staticHeader;
    
    for (ExpressionRule rule: expressions) {
//       System.err.println("rewriteing rule: " + rule.name + "\nwith precidences: " + rule.precidenceOpers);
      StringTemplate header = stg.getInstanceOf("header");
      header.setAttribute("precedences", rule.precidenceOpers);
      header.setAttribute("name", rule.name);
      membersText += header;
         
      List<Operator> binaryOps = new ArrayList<Operator>();
      List<Operator> prefixOps = new ArrayList<Operator>();
      List<Operator> suffixOps = new ArrayList<Operator>();
      List<Operator> ternaryOps = new ArrayList<Operator>();
      for (List<Operator> ops : rule.precidenceOpers) {
        if (ops == null) continue;
        for (Operator op : ops) {
          if (op.kind == Operator.Kind.Binary)
            binaryOps.add(op);
          else if (op.kind == Operator.Kind.Prefix)
            prefixOps.add(op);
          else if (op.kind == Operator.Kind.Suffix)
            suffixOps.add(op);
          else if (op.kind == Operator.Kind.TernaryPair){
            ternaryOps.add(op);
          }
        }
      }
//       System.err.println("binaries " + binaryOps);
//       System.err.println("primaries " + rule.terminals);
//       System.err.println("ternaries " + ternaryOps);
//       System.err.println("prefixes " + prefixOps);
      StringTemplate ruleTemplate = stg.getInstanceOf("exprRule");
      ruleTemplate.setAttribute("name",rule.name);
      ruleTemplate.setAttribute("terminals", rule.terminals);
      ruleTemplate.setAttribute("bops", binaryOps);
      ruleTemplate.setAttribute("pops", prefixOps);
      ruleTemplate.setAttribute("sops", suffixOps);
      ruleTemplate.setAttribute("tops", ternaryOps);
      ruleTemplate.setAttribute("buildTree", buildTree);
      
      tokens.replace(rule.startIndex, rule.stopIndex, ruleTemplate.toString());
    }
    membersText += "}";
    tokens.insertBefore(membersLocation, membersText);
    out.print(tokens);
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length != 1){
      System.err.println("usage: java ExpressionTransformer <filename>");
      System.exit(1);
    }
    ExpressionTransformer et = new ExpressionTransformer(readFileAsString(args[0]));
    et.printGrammar(true,System.out);
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