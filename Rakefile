#read => as 'depends on'

task :default => :test

task :popup do
  sh "echo 'No errors!' | growl -H localhost"
end

task :test => :test_grammar

def compileJava
  sources = FileList["*.java"].map {|f| [f, f.ext(".class")]}.collect {|(java, jclass)|
    java if need_compile?(java, jclass)
  }.compact
  sh "javac #{sources.join ' '}" unless sources.empty?
end

task :test_grammar do #=> [:compile] do
  sh "javac ExpressionTransformer.java" if need_compile?("ExpressionTransformer.java", "ExpressionTransformer.class")
  FileList["*.ng"].each do |f|
    sh "java ExpressionTransformer #{f} > #{f.ext('g')}" if need_compile?(f, f.ext('g'))
  end
  
  FileList['*.g'].each do |grammar|
    /(.*).g$/ === grammar
    name = $1
    sh "java org.antlr.Tool #{grammar}" if need_compile?(grammar, grammar.ext("java"), "#{name}Parser.java", "#{name}Lexer.java")
  end
  compileJava
  
  FileList["*.gunit"].each do |file|
    sh "java org.antlr.gunit.Interp #{file}"
  end
end

task :compile => FileList["*.java"] do
  sources = FileList["*.java"].map {|f| [f, f.ext(".class")]}.collect {|(java, jclass)|
    java if need_compile?(java, jclass)
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

def need_compile? source, *products
  products = products.delete_if {|product| !File.exists?(product) }
  return true if products.empty?
  products.each do |product|
    if File.mtime(source) >= File.mtime(product)
      return true 
    end
  end
  return false
end