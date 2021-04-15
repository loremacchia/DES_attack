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

std::string find_plaintext(std::string* vec, std::string ct, std::string salt);

int main(int argc, char const *argv[]) {
    std::string pt;
	if(argc > 1) pt = argv[1];
    else pt = "parallel";
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

	// std::string pt = "parallel";
	std::string salt = "LM";
    std::string ct = crypt(pt.c_str(), salt.c_str());
    std::cout << "Plaintext is: " << pt << " with salt: " << salt << " and cyphertext: " << ct << std::endl;

    auto start = std::chrono::high_resolution_clock::now(); 
    std::string foundPt = find_plaintext( vec, ct, salt);
    std::cout << foundPt << std::endl;

    auto stop = std::chrono::high_resolution_clock::now(); 
    auto duration = std::chrono::duration_cast<std::chrono::microseconds>(stop - start); 
    double outerTime = duration.count()/(double)1000000;
    printf("\n%f\n",outerTime);
    std::cout << "Plaintext is: " << foundPt << std::endl;

    std::ofstream myfile1;
    myfile1.open ("cppRes.csv", std::ios::app);
    myfile1 << foundPt;
    myfile1 << "," << outerTime;
    myfile1 << "\n";
    myfile1.close();

	return 0;
}


std::string find_plaintext(std::string* vec, std::string ct, std::string salt){
    int i = 0;
    while (i < vec->length()) {
        if(ct.compare(crypt((vec[i]).c_str(),salt.c_str())) == 0){
            return (vec[i]).c_str();
        }
        i++;
    }
    return "";
} 