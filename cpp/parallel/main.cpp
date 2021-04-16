#include <fstream>
#include <iostream>
#include <string.h>
#include <vector>
#include <cstdlib>
#include <chrono> 
#include <crypt.h>
#include <math.h>
#include <thread>
#include <atomic>
#include <future>
#include "rapidcsv.h"

std::string find_plaintext(std::string* vec, std::string ct, std::string salt, int lower, int higher);
std::atomic<bool> found(false);

int main(int argc, char const *argv[]) {
    // Get thread number as a parameter
    int threadNumber;
    if(argc > 1) threadNumber = atoi(argv[1]);
    else threadNumber = 8;
    std::thread threads[threadNumber];

    // Get plaintext as a parameter
    std::string pt;
    if(argc > 2) pt = argv[2];
    else pt = "parallel";
    
    // Compute the length of rockyou.txt. It only takes the words with length <= 8. (it is computed previously)
	long length = 7700016;
	// std::string lineTmp;
	// std::ifstream myfileTmp( "../rockyou.txt" );
    // long length = 0;
	// std::cout << length << std::endl;
    // if (myfileTmp) {
	// 	while (getline( myfileTmp, lineTmp )) {
	// 		if(lineTmp.length() <= 8){
    //             length++;
	// 		}
	// 	}
	// 	myfileTmp.close();
	// }
    // std::cout << length << std::endl;

    // Allocate the vector with all possible plaintexts
    std::string* vec = new std::string[length];
	std::ifstream myfile( "../rockyou.txt" );
	std::string line;
    long i = 0;
	if (myfile) {
		while (getline( myfile, line )) {
			if(line.length() <= 8){
                vec[i] = line;
                i++;
			}
		}
		myfile.close();
	}

    // Compute the ciphertext target for the attack
	std::string salt = "LM";
    std::string ct = crypt(pt.c_str(), salt.c_str());
    std::cout << "Plaintext is: " << pt << " with salt: " << salt << " and cyphertext: " << ct << std::endl;

    // Parallel version of Des attack: each thread iterates through a smaller set of possible plaintext reducing the computational time
    auto start = std::chrono::high_resolution_clock::now(); 
    std::vector<std::future<std::string>> futures; // Each thread returns a string (empty or correct plaintext)
                                                   // collected in an asynchronous way by a future
    int incrAmount = (int)floor(length/threadNumber); // Length of the subset of each threads
    // Launching the thradNumber threads with async giving at each one the bounds of the vector between searching for the plaintext 
    for (int itr = 0; itr < threadNumber; itr++){
        int lower = incrAmount*itr;
        int higher = itr == threadNumber-1 ? length : incrAmount*(itr+1);
        futures.push_back(std::async(std::launch::async, find_plaintext, vec, ct, salt, lower, higher)); 
    }

    // Getting the correct plaintext from the thread's futures
    std::string foundPt;
    std::string tmpStr;
    for (int itr = 0; itr < threadNumber; itr++){
        tmpStr = futures[itr].get();
        if(tmpStr != "") foundPt = tmpStr;
    }

    // Getting the computation time and writing it into a csv file
    auto stop = std::chrono::high_resolution_clock::now(); 
    auto duration = std::chrono::duration_cast<std::chrono::microseconds>(stop - start); 
    double outerTime = duration.count()/(double)1000000;
    std::cout << "Plaintext found is: " << foundPt << std::endl;
    printf("\n%f\n",outerTime);

    std::ofstream myfile1;
    myfile1.open ("parRes.csv", std::ios::app);
    myfile1 << threadNumber;
    myfile1 << "," << foundPt;
    myfile1 << "," << outerTime;
    myfile1 << "\n";
    myfile1.close();

	return 0;
}

// Function to exploit the DES attack getting the possible plaintexts from a vector vec from the indexes lower and higher.
// It has to encrypt all the plaintexts with the given salt and compare the found ciphertexts with the target one ct.
// This is the parallel version that uses the crypt_r reentrant des encryption function and has a boolean atomic found to synchronize the threads
std::string find_plaintext(std::string* vec, std::string ct, std::string salt, int lower, int higher){
    struct crypt_data data;
    data.initialized = 0;
    // Loop on the given possible plaintexts while any other thread has found the correct plaintext
    int itr = lower;
    while (itr < higher && !(found.load())) {
        if(ct.compare(crypt_r((vec[itr]).c_str(),salt.c_str(), &data)) == 0){ // Encrypt and compare
			if(!(found.load())){ // Another check if other threads have found the plaintext
                found.store(true); // Set found at true
                return (vec[itr]).c_str(); // Return the correct plaintext
            }
        }
        itr++;
    }    
    return "";
} 