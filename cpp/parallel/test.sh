g++ main.cpp -pthread -lcrypt -o mainCpp
StringVal="123456 parallel hawker1 ac3127 mritaly"

for val in $StringVal; do   
    for thread in {1..5}
        do
            for i in {1..20..1}
                do
                    ./mainCpp $((2**$thread)) $val
                done
        done
done