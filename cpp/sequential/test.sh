g++ main.cpp -pthread -lcrypt -o mainCpp
StringVal="hawker1 ac3127 mritaly" #123456 parallel 

for val in $StringVal; do   
    for i in {1..20..1}
        do
            ./mainCpp $val
        done
done