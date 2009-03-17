#read => as 'depends on'

task :default => :test

task :popup do
  sh "echo 'No errors!' | growl -H localhost"
end

task :test => :test_grammar

task :test_grammar => [:compile] do
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
  /(.*).g/ === grammar
  name = $1
  ["#{name}.java", "#{name}Parser.java", "#{name}Lexer.java"].each do |generated_source|
    file generated_source => grammar do |t|
      sh "java org.antlr.Tool #{grammar}"
    end
  end
end

file "HaskellExpressions.g" => "HaskellExpressions.ng" do |t|
  sh "javac ExpressionTransformer.java"
  sh "java ExpressionTransformer HaskellExpressions.ng > HaskellExpressions.g"
  sh "cat HaskellExpressions.lexer >> HaskellExpressions.g"
end

def need_compile? source, product
  !(File.exists?(product) && File.mtime(source) <= File.mtime(product))
end