#read => as 'depends on'

task :default => :test

task :popup do
  sh "echo 'No errors!' | growl -H localhost"
end

task :test => :test_grammar

def compileJava
  sources = FileList["*.java"].map {|f| [f, f.ext(".class")]}.collect {|(java, jclass)|
    java if need_compile?([java], jclass)
  }.compact
  sh "javac #{sources.join ' '}" unless sources.empty?
end

task :test_grammar do #=> [:compile] do
  sh "java org.antlr.Tool ExpressionCrawler.g" if need_compile?(["ExpressionCrawler.g"], "ExpressionCrawler.java")
  sh "javac ExpressionCrawler.java" if need_compile?(["ExpressionCrawler.java"], "ExpressionCrawler.class")
  sh "javac ExpressionTransformer.java" if need_compile?(["ExpressionTransformer.java"], "ExpressionTransformer.class")
  FileList["*.ng"].each do |f|
    sh "java ExpressionTransformer #{f} > #{f.ext('g')}" if need_compile?([f,"Greedy.stg", "ExpressionTransformer.class", "ExpressionCrawler.class"], f.ext('g'))
  end
  
  FileList['*.g'].each do |grammar|
    /(.*).g$/ === grammar
    name = $1
    sh "java org.antlr.Tool #{grammar}" if need_compile?([grammar], grammar.ext("java"), "#{name}Parser.java", "#{name}Lexer.java")
  end
  compileJava
  
  ["OriginalJava.gunit", "Java.gunit"].each do |f|
    if need_compile?(["Java.ngunit"],f)
      sh "echo 'gunit #{f.sub('.gunit','')};\n' > #{f}"
      sh "cat Java.ngunit >> #{f}"
    end
  end
  
#   FileList["*.gunit"].each do |file|
  ["HaskellExpressions.gunit", "OriginalJava.gunit", "Java.gunit"].each do |file|
    sh "java org.antlr.gunit.Interp #{file}"
  end
end

task :clean do
  sh "rm -f *.class"
  sh "rm -f ExpressionParser.java ExpressionLexer.java HaskellExpressions.g Java.g ExpressionCrawler.java ExpressionCrawler.tokens"
  sh "rm -f Java.tokens JavaLexer.java JavaParser.java"
end

task :compile => FileList["*.java"] do
  sources = FileList["*.java"].map {|f| [f, f.ext(".class")]}.collect {|(java, jclass)|
    java if need_compile?([java], jclass)
  }.compact
  sh "javac #{sources.join ' '}" unless sources.empty?
end

GRAMMARS = FileList['*.g']
GRAMMARS.each do |grammar|
  /(.*).g$/ === grammar
  name = $1
  ["#{name}.java", "#{name}Parser.java", "#{name}Lexer.java"].each do |generated_source|
    file generated_source => grammar do |t|
      sh "java org.antlr.Tool #{grammar}"
    end
  end
end

PREGRAMMARS = FileList["*.ng"]
PREGRAMMARS.each do |pregrammar|
  file pregrammar.ext("g") => pregrammar do
    sh "javac ExpressionTransformer.java"
    sh "java ExpressionTransformer #{pregrammar} > #{pregrammar.ext('g')}"
  end
end

def need_compile? sources, *products
  products = products.delete_if {|product| !File.exists?(product) }
  return true if products.empty?
  source_time = sources.map{|f| File.mtime(f)}.max
  products.each do |product|
    if source_time >= File.mtime(product)
      return true 
    end
  end
  return false
end