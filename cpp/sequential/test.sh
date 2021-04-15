g++ main.cpp -lcrypt -o mainCpp
StringVal="123456 parallel hawker1 ac3127 mritaly" 

for val in $StringVal; do   
    for i in {1..20..1}
        do
            ./mainCpp $val
        done
done