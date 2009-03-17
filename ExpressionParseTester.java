import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.antlr.runtime.*;


public class ExpressionParseTester {

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {    
    BufferedReader br =new BufferedReader(new InputStreamReader(System.in));
    ANTLRReaderStream r = new ANTLRReaderStream(br);
    Lexer lexer = new HaskellExpressionsLexer(r);
    TokenStream tokens = new CommonTokenStream(lexer);
    tokens.LA(1);
    long start = System.currentTimeMillis();
    HaskellExpressionsParser parser = new HaskellExpressionsParser(tokens);
    try {
      parser.expressions();
    } catch (RecognitionException e) {
      e.printStackTrace();
    }
    long stop = System.currentTimeMillis();
    System.out.println("parse time (ms): "+(stop-start));
  }

}
