# DES_attack

## Algorithm
DES is an encryption algorithm based on substitution. In this repository we show three different attacks to this algorithm:
* Known ciphertext and chosen plaintext: attack using a set of frequently used passwords `rockyou.txt` that has to be downloaded from https://www.kaggle.com/wjburns/common-password-list-rockyoutxt/version/1;
* Known ciphertext and chosen plaintext: attack bruteforcing all the possible passwords;
* known ciphertext: briteforce over the keys to decrypt the ciphertext, the obtained plaintext is chosen as correct if it has a reasonable low distance from a frequent password of `rockyou.txt`.

## Implementation
The first attack is implemented both in Java and in C++, while the second and the third only in Java. All those implementations include a parallelized version respectively with C++ Threads and Java Threads.

### C++ version
To run the c++ version you have to move to the relative folder and then in the parallel or sequential. To run the tests you can execute the relative bash script 
```
bash test.sh
```
If you want to execute a custom test run: 
* parallel version
```
g++ main.cpp -pthread -lcrypt -o mainCpp
./mainCpp num_threads plaintext_target
```
* sequential version
```
g++ main.cpp -lcrypt -o mainCpp
./mainCpp plaintext_target
```

## Results
The performance improvements are significant given that the problem is embrassingly parallel. Chek the results on the relative csv files.

### Contributors
Contributions are made by Lorenzo Macchiarini and Andrea Leonardo for the course Parallel Computing of the Master Degree in Software Engineering.
