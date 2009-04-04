
class Operator {
  public enum Kind {Unary, Binary, TernaryPair};
  public Kind kind;
  public int tokenType;
  public String tokenText;
  public enum Associativity {Left, Right};
  public Associativity assoc;
  public boolean isBinary() {
    return kind == Kind.Binary;
  }
  public boolean isUnary() {
    return kind == Kind.Unary;
  }
  public boolean isRightAssoc() {
    return assoc == Associativity.Right;
  }
  public boolean isLeftAssoc() {
    return assoc == Associativity.Left;
  }
  public Operator(int tokenType, String tokenText, Associativity assoc) {
    this.tokenType = tokenType; this.assoc=assoc; this.tokenText = tokenText;
  }
  public String toString() {
    String result = kind == Kind.Binary ? "B" : kind == Kind.Unary ? "U" : "T";
    result += assoc == Associativity.Right ? "R " : "L ";
    return result + '"' + tokenText + '"'; 
    
  }
}