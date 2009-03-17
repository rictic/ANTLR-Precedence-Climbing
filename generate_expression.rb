ops = ['$','$!','>>','>>=','||','&&','==','/=','<','<=','>=','>',':','+','-','*','/','^','^^','**','.']

(ARGV[1]||1).to_i.times do
  rand(ARGV[0].to_i).times do
    print "#{rand(10)}#{ops[rand(ops.length)]}"
  end
  puts rand(10)
end
print rand(10)
