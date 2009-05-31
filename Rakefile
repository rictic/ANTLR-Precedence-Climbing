#read => as 'depends on'

task :default => :test

task :popup do
  sh "echo 'No errors!' | growl -H localhost"
end

task :test => :test_grammar

# sh "export CLASSPATH=bin/:#{ENV['CLASSPATH']}"

def compileJava(files)
  sources = (files).map {|f| [f, "bin/" + f.ext(".class")]}.collect {|(java, jclass)|
    java if need_compile?([java], jclass)
  }.compact
  javac(sources.join(' ')) unless sources.empty?
end

def javac source
  sh "mkdir -p bin"
  sh "javac -d bin #{source}"
end

def java main
  sh "java -cp bin/:#{ENV['CLASSPATH']} #{main}"
end

def antlr source
  sh "java org.antlr.Tool #{source}"
end

def gunit test
  sh "java org.antlr.gunit.Interp #{test}"
end

task :test_grammar do #=> [:compile] do
  antlr "ExpressionCrawler.g" if need_compile?(["ExpressionCrawler.g"], "ExpressionCrawler.java")
  compileJava(FileList["*.java"])
  FileList["test/*.ng"].each do |f|
    java "ExpressionTransformer #{f} > #{f.ext('g')}" if need_compile?([f,"Greedy.stg", "bin/ExpressionTransformer.class", "bin/ExpressionCrawler.class"], f.ext('g'))
  end
  
  FileList['test/*.g'].each do |grammar|
    /(.*).g$/ === grammar
    name = $1
    antlr grammar if need_compile?([grammar], grammar.ext("java"), "#{name}Parser.java", "#{name}Lexer.java")
  end
  compileJava(FileList["test/*.java"])
  
  ["OriginalJava.gunit", "Java.gunit"].each do |f|
    if need_compile?(["test/Java.ngunit"],f)
      sh "echo 'gunit #{f.sub('.gunit','')};\n' > test/#{f}"
      sh "cat test/Java.ngunit >> test/#{f}"
    end
  end

  cd "bin"
  FileList["../test/*.gunit"].each do |file|
#   ["HaskellExpressions.gunit", "OriginalJava.gunit", "Java.gunit"].each do |file|
    gunit file
  end
  cd "../"
end

task :clean do
  sh "rm -f bin/*.class"
  sh "rm -f test/*.class"
  sh "rm -f ExpressionParser.java ExpressionLexer.java test/HaskellExpressions.g test/Java.g ExpressionCrawler.java ExpressionCrawler.tokens"
  sh "rm -f test/Java.tokens test/JavaLexer.java test/JavaParser.java"
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
