import java.util.List;


class Operator {
  public enum Kind {Prefix, Suffix, Binary, TernaryPair};
  public Kind kind;
  public List<String> tokenTexts;
  public boolean predictable;
  public enum Associativity {Left, Right};
  public Associativity assoc;
  public Operator ternaryOp = null;
  public boolean ternaryAfter;  
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
  public String getSafePredictiveToken() { return getPredictiveToken().replace("%","\\%");}
  public List<String> getTokenTexts() {return tokenTexts;}
  public Operator(List<String> tokenTexts, Associativity assoc, boolean predictable) {
    this.assoc=assoc; this.tokenTexts = tokenTexts; this.predictable = predictable;
  }
  public String toString() {
    String result = "";
    if (kind != null)
    switch (kind)
    {
      case Binary: result = "B"; break;
      case Prefix: result = "P"; break;
      case Suffix: result = "S"; break;
      case TernaryPair: result = "T"; break;
    }
    result += assoc == Associativity.Right ? "R " : "L ";
    result += '"' + tokenTexts.toString() + '"';
    if (ternaryOp != null)
      result += " (" + ternaryOp.toString() + ")";
    return result; 
  }
  public String getPredictiveToken() {
    return tokenTexts.get(0);
  }
}