import java.util.List;
import java.util.ArrayList;

class ExpressionRule {
  public String name;
  public List<String> terminals;
  public List<List<Operator>> precidenceOpers;
  public int startIndex;
  public int stopIndex;
  
  public ExpressionRule(String name, int startIndex, int stopIndex) {
    this.name = name;
    this.terminals = new ArrayList<String>();
    this.precidenceOpers = new ArrayList<List<Operator>>();
    this.startIndex = startIndex;
    this.stopIndex = stopIndex;
  }
  ExpressionRule(String name, List<String> terminals, List<List<Operator>> precidenceOpers, int startIndex, int stopIndex) {
    this.name = name; this.terminals = terminals; this.precidenceOpers = precidenceOpers;
    this.startIndex = startIndex; this.stopIndex = stopIndex;
  }
}