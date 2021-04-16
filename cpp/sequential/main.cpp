#include <fstream>
#include <iostream>
#include <string.h>
#include <vector>
#include <chrono> 
#include <crypt.h>
#include <math.h>

std::string find_plaintext(std::string* vec, std::string ct, std::string salt);

int main(int argc, char const *argv[]) {
    // Get plaintext as a parameter
    std::string pt;
	if(argc > 1) pt = argv[1];
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

    // Perform the attack on the possible set of plaintexts giving the target ciphertext and the salt. The found plaintext is returned.
    auto start = std::chrono::high_resolution_clock::now(); 
    std::string foundPt = find_plaintext(vec, ct, salt);
    std::cout << foundPt << std::endl;

    // Getting the computation time and writing it into a csv file
    auto stop = std::chrono::high_resolution_clock::now(); 
    auto duration = std::chrono::duration_cast<std::chrono::microseconds>(stop - start); 
    double outerTime = duration.count()/(double)1000000;
    std::cout << "Plaintext found is: " << foundPt << std::endl;
    printf("\n%f\n",outerTime);

    std::ofstream myfile1;
    myfile1.open ("cppRes.csv", std::ios::app);
    myfile1 << foundPt;
    myfile1 << "," << outerTime;
    myfile1 << "\n";
    myfile1.close();

	return 0;
}

// Function to exploit the DES attack getting the possible plaintexts from a vector vec.
// It has to encrypt all the plaintexts with the given salt and compare the found ciphertexts with the target one ct.
std::string find_plaintext(std::string* vec, std::string ct, std::string salt){
    // Loop on the given possible plaintexts while any other thread has found the correct plaintext
    int i = 0;
    while (i < vec->length()) {
        if(ct.compare(crypt((vec[i]).c_str(),salt.c_str())) == 0){ // Encrypt and compare
            return (vec[i]).c_str(); // Return the correct plaintext
        }
        i++;
    }
    return "";
} 