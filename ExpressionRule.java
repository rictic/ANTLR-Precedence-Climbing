import java.util.List;

class ExpressionRule {
  public String name;
  public List<String> terminals;
  public List<List<Operator>> precidenceOpers;
  public int startIndex;
  public int stopIndex;
  ExpressionRule(String name, List<String> terminals, List<List<Operator>> precidenceOpers, int startIndex, int stopIndex) {
    this.name = name; this.terminals = terminals; this.precidenceOpers = precidenceOpers;
    this.startIndex = startIndex; this.stopIndex = stopIndex;
  }
}