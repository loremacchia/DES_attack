#include <fstream>
#include <iostream>
#include <string.h>
#include <set>
#include <vector>
#include <map>
#include <chrono> 
#include <thread>
#include <atomic>
#include <crypt.h>
#include <math.h>

std::string find_plaintext(std::string* vec, std::string ct, std::string salt, int lower, int higher);
std::atomic<bool> found(false);

int main(int argc, char const *argv[]) {
    auto start = std::chrono::high_resolution_clock::now(); 
	
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

	std::string pt = "parallel";
	std::string salt = "LM";
    std::string ct = crypt(pt.c_str(), salt.c_str());
    std::cout << "Plaintext is: " << pt << " with salt: " << salt << " and cyphertext: " << ct << std::endl;

    std::string foundPt = find_plaintext( vec, ct, salt, 0, length);
    std::cout << foundPt << std::endl;

    auto stop = std::chrono::high_resolution_clock::now(); 
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(stop - start); 
    double outerTime = duration.count()/(double)1000;
    printf("\n%f\n",outerTime);

	return 0;
}


std::string find_plaintext(std::string* vec, std::string ct, std::string salt, int lower, int higher){
    int itr = lower;
    while (itr < higher && !(found.load())) {
        if(ct.compare(crypt((vec[itr]).c_str(),salt.c_str())) == 0){
			found = true;
            return (vec[itr]).c_str();
        }
        itr++;
    }
    return "";
} 