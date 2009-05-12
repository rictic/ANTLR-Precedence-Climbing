class Operator {
  public enum Kind {Prefix, Suffix, Binary, TernaryPair};
  public Kind kind;
  public int tokenType;
  public String tokenText;
  public enum Associativity {Left, Right};
  public Associativity assoc;
  public Operator ternary = null;
  public boolean ternaryAfter;
  public String ternaryText;
  public boolean isBinary() {
    return kind == Kind.Binary;
  }
  public boolean isPrefix() {
    return kind == Kind.Prefix;
  }
  public boolean isSuffix() {
    return kind == Kind.Suffix;
  }
  public boolean isTernary() {
    return kind == Kind.TernaryPair;
  }
  public boolean isRightAssoc() {
    return assoc == Associativity.Right;
  }
  public boolean isLeftAssoc() {
    return assoc == Associativity.Left;
  }
  public String getSafeTokenText() { return tokenText.replace("%","\\%");}
  public Operator(int tokenType, String tokenText, Associativity assoc) {
    this.tokenType = tokenType; this.assoc=assoc; this.tokenText = tokenText;
  }
  public String toString() {
    String result = "";
    switch (kind)
    {
      case Binary: result = "B"; break;
      case Prefix: result = "P"; break;
      case Suffix: result = "S"; break;
      case TernaryPair: result = "T"; break;
    }
    result += assoc == Associativity.Right ? "R " : "L ";
    result += '"' + tokenText + '"';
    if (ternary != null)
      result += " (" + ternary.toString() + ")";
    return result; 
  }
  public String sndop() {
    return ternary.tokenText;
  }
}