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


    int threadNumber;
    if(argc > 1) threadNumber = atoi(argv[1]);
    else threadNumber = 8;

    std::string pt;
    if(argc > 2) pt = argv[2];
    else pt = "parallel";
    std::thread threads[threadNumber];
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
    std::cout << length << std::endl;

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
    
	// std::string pt = "123456";
	std::string salt = "LM";
    std::string ct = crypt(pt.c_str(), salt.c_str());
    std::cout << "Plaintext is: " << pt << " with salt: " << salt << " and cyphertext: " << ct << std::endl;

    auto start = std::chrono::high_resolution_clock::now(); 
    std::vector<std::future<std::string>> futures;
    int incrAmount = (int)floor(length/threadNumber);
    for (int itr = 0; itr < threadNumber; itr++){
        int lower = incrAmount*itr;
        int higher = itr == threadNumber-1 ? length : incrAmount*(itr+1);
        futures.push_back(std::async(std::launch::async, find_plaintext, vec, ct, salt, lower, higher));
    }

    std::string foundPt;
    std::string tmpStr;
    for (int itr = 0; itr < threadNumber; itr++){
        tmpStr = futures[itr].get();
        if(tmpStr != "") foundPt = tmpStr;
    }

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


std::string find_plaintext(std::string* vec, std::string ct, std::string salt, int lower, int higher){
    struct crypt_data data;
    data.initialized = 0;

    int itr = lower;
    while (itr < higher && !(found.load())) {
        if(ct.compare(crypt_r((vec[itr]).c_str(),salt.c_str(), &data)) == 0){
            // std::cout << (vec[itr]) << std::endl;
			found = true;
            return (vec[itr]).c_str();
        }
        itr++;
    }    
    return "";
} 