
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
  public Operator(int tokenType, String tokenText) {
    this.tokenType = tokenType; this.assoc=Associativity.Left; this.tokenText = tokenText;
  }
  public String toString() {
    String result = kind == Kind.Binary ? "B " : "U ";
    return result + '"' + tokenText + '"'; 
  }
}