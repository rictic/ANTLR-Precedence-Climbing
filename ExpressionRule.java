import java.util.List;

class ExpressionRule {
  public String name;
  public List<String> terminals;
  public List<List<Operator>> precidenceOpers;
  ExpressionRule(String name, List<String> terminals, List<List<Operator>> precidenceOpers) {
    this.name = name; this.terminals = terminals; this.precidenceOpers = precidenceOpers;
  }
}