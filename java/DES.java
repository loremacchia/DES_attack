import info.debatty.java.stringsimilarity.Jaccard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.util.*;

public class DES {

    //Alphabet for rockyou elaboration
    public static char[] A = { 'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F', 'g', 'G', 'h', 'H',
            'i', 'I', 'j', 'J', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'p', 'P', 'q', 'Q', 'r',
            'R', 's', 'S', 't', 'T', 'u', 'U', 'v', 'V', 'x', 'X', 'y', 'Y', 'z', 'Z', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '0', '-', '_'};

    //Alphabet for PINs search
    public static char[] N = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    //Alphabet for Key search
    public static char[] Q = { '1', '2', '3', '4', '5'};

    //Volatile variable for for thread's stopping condition
    public static volatile boolean found = false;

    private static Cipher ecipher;
    private static Cipher dcipher;

    public static SecretKey key;

    public static boolean check(char el, char[] A) {
        for (char element : A) {
            if (element == el)
                return true;
        }
        return false;
    }

    public static void readTxt(ArrayList<String> data, char[] alphabet){
        //this method saves the words of the rockyou.txt file in a list of strings eliminating all those with length> 8
        // and with characters not present in the alphabet A passed as parameter

        try(BufferedReader br = new BufferedReader(new FileReader("src/rockyou.txt"))) {
            for(String line; (line = br.readLine()) != null; ) {
                boolean alpha = true;
                for (int i = 0; i < line.length(); i++) {
                    if(!check(line.charAt(i), alphabet))
                        alpha = false;
                }

                if (line.length() <= 8 && alpha)
                    data.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int checkPasswordEncryptionEquality(int j, StringBuilder password, String message, long start) {
        if (Objects.equals(encrypt(password.toString()), message)) { //check the equality between the ciphetext and the current password encryption
            found = true; //Warn other threads to stop
            long finish = System.currentTimeMillis();
            System.out.println("LA PASSWORD CORRETTA E': " + password );
            System.out.println("time: " + (finish - start) / 1000f + " sec");
        }
        j+=1;
        return j;
    }

    public static String encrypt(String str) {
        try {
        // encode the string into a sequence of bytes using the named charset
        // storing the result into a new byte array.
            byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
            byte[] enc = ecipher.doFinal(utf8);
        // encode to base64
            return Base64.getEncoder().encodeToString(enc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String str) {
        try {
        // decode with base64 to get bytes
            byte[] dec = Base64.getDecoder().decode(str.getBytes());
            try{
            byte[] utf8 = dcipher.doFinal(dec);
        // create new string based on the specified charset
            return new String(utf8, StandardCharsets.UTF_8);
            }catch(BadPaddingException ignored){}
        }
        catch (Exception ignored) {
        }
        return null;
    }

    public static String tdecrypt(String str, Cipher dc) {
        //This is a thread version which performs decryption with an cipher passed as a parameter
        try {
        // decode with base64 to get bytes
            byte[] dec = Base64.getDecoder().decode(str.getBytes());
            try{
                byte[] utf8 = dc.doFinal(dec);
        // create new string based on the specified charset
                return new String(utf8, StandardCharsets.UTF_8);
            }catch(BadPaddingException ignored){}
        }
        catch (Exception ignored) {
        }
        return null;
    }

    public static class SearchThread extends Thread {
        //Known password search thread
        int i; //Starting index
        int f; //Final index
        ArrayList<String> data;
        long start;
        String message; //Ciphertext

        SearchThread(int i, int f, ArrayList<String> data, long start, String message) {
            this.i = i;
            this.f = f;
            this.data = data;
            this.start = start;
            this.message = message;
        }

        public void run() {
            int j = i; //Search index
            while (j < f && !found) { //Stopping rule: if cycle terminates or if a thread found the correct password
                StringBuilder password = new StringBuilder();
                password.append(data.get(j)); //Take a password from the list (data) with index j
                //System.out.println(password.toString());

                j = checkPasswordEncryptionEquality(j, password, message, start);
            }

        }
    }

    public static class SearchAllThread extends Thread {
        //Generic PIN search thread
        int i; //Starting index
        int f; //Final index
        ArrayList<String> data;
        long start;
        String message; //Ciphertext

        SearchAllThread(int i, int f, ArrayList<String> data, long start, String message) {
            this.i = i;
            this.f = f;
            this.data = data;
            this.start = start;
            this.message = message;
        }

        public void run() {
            int j = i; //Search index
            while (j < f && !found) { //Stopping rule: if cycle terminates or if a thread found the correct password
                StringBuilder password = new StringBuilder();
                for (int x = 0; x < 8; x++)
                    //creation of eight-digit pins by remainders of the division between index j
                    // and powers of 10 (length of the alphabet N)
                    password.append(N[Math.toIntExact(j / Math.round(Math.pow(N.length, x))) % N.length]);
                //System.out.println(password + " "+ encrypt(password.toString()) + " id: "+getId());


                j = checkPasswordEncryptionEquality(j, password, message, start);
            }

        }
    }

    public static class SearchKeyThread extends Thread{
        int i; //Starting index
        int f; //Final index
        ArrayList<String> data;
        long start;
        String message; //Ciphertext
        HashMap<String,ArrayList<String>> triMap; //Map of rockyou's trigrams


        SearchKeyThread(int i, int f, ArrayList<String> data, long start, String message, HashMap<String,ArrayList<String>> triMap) {
            this.i = i;
            this.f = f;
            this.data = data;
            this.start = start;
            this.message = message;
            this.triMap = triMap;
        }

        public void run() {
            Cipher tdcipher = null;
            SecretKey tkey;
            int j = i;
            while (j < f && !found) {
                Jaccard jaccard = new Jaccard();
                StringBuilder tempKey = new StringBuilder();
                for (int k = 0; k < 10; k++)
                    //creation of ten-digit keys by remainders of the division between index j
                    // and powers of 5 (length of the alphabet Q)
                    tempKey.append(Q[Math.toIntExact(j / Math.round(Math.pow(Q.length, k))) % Q.length]);
                //System.out.println(key);
                tempKey.append("0");
                tempKey.append("=");

                try {
                    // generate secret key using DES algorithm
                    byte[] encodedKey = Base64.getDecoder().decode(tempKey.toString());
                    tkey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "DES");
                    tdcipher = Cipher.getInstance("DES");
                    
                    tdcipher.init(Cipher.DECRYPT_MODE, tkey);
                } catch (InvalidKeyException e) {
                    System.out.println("Invalid Key:" + e.getMessage());
                    return;
                } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }


                //Decryption of the ciphertext with the current key
                String result = tdecrypt(message, tdcipher);


                if (result != null) {
                    //System.out.println("KEY: " + Base64.getEncoder().encodeToString(key.getEncoded()) + " result: " + result);
                    List<String> triArray;

                    //Generation of decryption trigrams
                    triArray = Ngrams.ngrams(3, result);
                    ArrayList<String> possiblePassword = new ArrayList<>();

                    //Search of rockyou password with at least one trigram in common with the decryption
                    for (String value : triArray) {
                        if (triMap.get(value) != null)
                            possiblePassword.addAll(triMap.get(value));
                    }

                    //Search if a possible password is similar enough (with Jaccard coefficient)
                    for (String value : possiblePassword) {
                        if (jaccard.similarity(result, value) >= 0.5) { //Check of the Jaccard similarity
                            found = true; //Warn other threads to stop
                            long finish = System.currentTimeMillis();
                            System.out.println("Probably key: " + tempKey.toString() + "\nPASSWORD: " + result + " [Jaccard similarity " + jaccard.similarity(result, value) + " with " + value + "]");
                            System.out.println("time: " + (finish - start) / 1000f + " sec");
                            j = (int) Math.round(Math.pow(Q.length, 10));
                            break;
                        }
                    }
                }
                j+=1;
            }

        }
    }

    public static void main(String[] args) {

        boolean enc = true, ry= false, parallel = false;
        String message = null, result, k = null;
        ArrayList<String> data = new ArrayList<>();
        HashMap<String,ArrayList<String>> triMap = new HashMap<>();
        int numThreads = 8;


        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-m" -> message = args[++i];
                case "-d" -> enc = false;
                case "-ry" -> ry = true;
                case "-p" -> parallel = true;
                case "-k" -> k = args[++i];
            }
        }

        if(enc) { //ENCRYPTION
            System.out.println("PASSWORD: " + message);
            try {
                // generate secret key using DES algorithm
                byte[] encodedKey = Base64.getDecoder().decode(k);
                key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "DES");
                System.out.println("KEY: " + Base64.getEncoder().encodeToString(key.getEncoded()));
                ecipher = Cipher.getInstance("DES");
                dcipher = Cipher.getInstance("DES");

                // initialize the ciphers with the given key

                ecipher.init(Cipher.ENCRYPT_MODE, key);
                dcipher.init(Cipher.DECRYPT_MODE, key);
            }
            catch (NoSuchAlgorithmException e) {
                System.out.println("No Such Algorithm:" + e.getMessage());
                return;
            }
            catch (NoSuchPaddingException e) {
                System.out.println("No Such Padding:" + e.getMessage());
                return;
            }
            catch (InvalidKeyException e) {
                System.out.println("Invalid Key:" + e.getMessage());
                return;
            }

            assert message != null;
            result = encrypt(message);
            System.out.println("\nPASSWORD ENCRIPTION: " + result);
        }else { //DECRYPTION

            System.out.println("CIPHERTEXT: " + message);

            if (k != null) { //Attack with known key

                try {
                    // generate secret key using DES algorithm
                    byte[] encodedKey = Base64.getDecoder().decode(k);
                    key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "DES");
                    System.out.println("KEY: " + Base64.getEncoder().encodeToString(key.getEncoded()));
                    ecipher = Cipher.getInstance("DES");
                    dcipher = Cipher.getInstance("DES");

                    // initialize the ciphers with the given key

                    ecipher.init(Cipher.ENCRYPT_MODE, key);
                    dcipher.init(Cipher.DECRYPT_MODE, key);
                } catch (NoSuchAlgorithmException e) {
                    System.out.println("No Such Algorithm:" + e.getMessage());
                    return;
                } catch (NoSuchPaddingException e) {
                    System.out.println("No Such Padding:" + e.getMessage());
                    return;
                } catch (InvalidKeyException e) {
                    System.out.println("Invalid Key:" + e.getMessage());
                    return;
                }


                if (!parallel) {

                    StringBuilder password;

                    if (ry) { //Sequential Known Password Attack
                        readTxt(data, A);
                        long start = System.currentTimeMillis();
                        System.out.println("\nSearching for password in Rockyou...");
                        for (String datum : data) {
                            password = new StringBuilder();
                            password.append(datum);
                            //System.out.println(password + " " + binToHex(des.encrypt(key1, utfToBin(password.toString()))));
                            if (Objects.equals(encrypt(password.toString()), message)) { //check the equality between the ciphertext and the current password encryption
                                long finish = System.currentTimeMillis();
                                System.out.println("LA PASSWORD CORRETTA E': " + password);
                                System.out.println("time: " + (finish - start) / 1000f + " sec");
                                break;
                            }
                        }

                    } else { //Sequential Generic PIN Attack
                        System.out.println("Searching among all possible PINs...");
                        long start = System.currentTimeMillis();
                        for (int j = 0; j < Math.round(Math.pow(N.length, 8)); j++) {
                            password = new StringBuilder();
                            for (int x = 0; x < 8; x++)
                                //creation of eight-digit pins by remainders of the division between index j
                                // and powers of 10 (length of the alphabet N)
                                password.append(N[Math.toIntExact(j / Math.round(Math.pow(N.length, x))) % N.length]);
                            //System.out.println(password + " "+ encrypt(password.toString()));
                            if (Objects.equals(encrypt(password.toString()), message)) { //check the equality between the ciphertext and the current password encryption
                                long finish = System.currentTimeMillis();
                                System.out.println("LA PASSWORD CORRETTA E': " + password);
                                System.out.println("time: " + (finish - start) / 1000f + " sec");
                                break;
                            }
                        }

                    }
                } else {

                    if (ry) { //Parallel Known Password Attack
                        readTxt(data, A);
                        long start = System.currentTimeMillis();
                        ArrayList<SearchThread> threads = new ArrayList<>();
                        int inizio , fine;


                        for (int i = 0; i < numThreads; i++) {
                            //Creation of upper and lower bounds
                            inizio = (data.size() / numThreads) * i;
                            if (i == numThreads - 1) {
                                fine = data.size();
                            } else {
                                fine = (data.size() / numThreads) * (i + 1);
                            }
                            //System.out.println(inizio + " " + fine);
                            threads.add(new SearchThread(inizio, fine, data, start, message));
                        }
                        for (int i = 0; i < numThreads; i++) {
                            threads.get(i).start();
                        }

                    } else { //Parallel Generic PIN Attack
                        System.out.println("Searching among all possible PINs...");
                        long start = System.currentTimeMillis();
                        ArrayList<SearchAllThread> threads = new ArrayList<>();
                        int inizio ,fine ;
                        for (int i = 0; i < numThreads; i++) {
                            //Creation of upper and lower bounds
                            inizio = (((int) Math.round(Math.pow(N.length, 8))) / numThreads) * i;
                            if (i == numThreads - 1) {
                                fine = (int) Math.round(Math.pow(N.length, 8));
                            } else {
                                fine = (((int) Math.round(Math.pow(N.length, 8))) / numThreads) * (i + 1);
                            }
                            //System.out.println(inizio + " " + fine);
                            threads.add(new SearchAllThread(inizio, fine, data, start, message));
                        }
                        for (int i = 0; i < numThreads; i++) {
                            threads.get(i).start();
                        }
                    }

                }


            }
            else{ //Attack on key
                readTxt(data, A);
                StringBuilder tempKey;
                long start;
                System.out.println("Building trigrams...");

                //Cycle that maps rockyou passwords with their trigrams
                for (String datum : data) {
                    List<String> triarray = Ngrams.ngrams(3, datum);
                    for (String value : triarray) {
                        triMap.computeIfAbsent(value, k1 -> new ArrayList<>());
                        triMap.get(value).add(datum); //Map has the shape (trigram, password[])
                    }
                }

                if(!parallel) { //Sequential Attack on Key
                    start = System.currentTimeMillis();
                    System.out.println("Searching for the key...");
                    Jaccard jaccard = new Jaccard();
                    for (int count = 0; count < Math.round(Math.pow(Q.length, 10)); count++) {
                        tempKey = new StringBuilder();
                        for (int j = 0; j < 10; j++)
                            //creation of ten-digit keys by remainders of the division between index j
                            // and powers of 5 (length of the alphabet Q)
                            tempKey.append(Q[Math.toIntExact(count / Math.round(Math.pow(Q.length, j))) % Q.length]);
                        tempKey.append("0");
                        tempKey.append("=");
//                        System.out.println(tempKey.toString());
                        try {
                            // generate secret key using DES algorithm
                            byte[] encodedKey = Base64.getDecoder().decode(tempKey.toString());
                            key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "DES");
                            dcipher = Cipher.getInstance("DES");
                            // initialize the ciphers with the given key

                            dcipher.init(Cipher.DECRYPT_MODE, key);
                        } catch (InvalidKeyException e) {
                            System.out.println("Invalid Key:" + e.getMessage());
                            return;
                        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }


                        result = decrypt(message);

                        if (result != null) {
//                        System.out.println("KEY: " + Base64.getEncoder().encodeToString(key.getEncoded()) + " result: " + result);
                            List<String> triArray;

                            //Generation of decryption trigrams
                            triArray = Ngrams.ngrams(3, result);
                            ArrayList<String> possiblePassword = new ArrayList<>();

                            //Search of rockyou password with at least one trigram in common with the decryption
                            for (String value : triArray) {
                                if (triMap.get(value) != null)
                                    possiblePassword.addAll(triMap.get(value));
                            }

                            //Search if a possible password is similar enough (with Jaccard coefficient)
                            for (String value : possiblePassword) {
                                if (jaccard.similarity(result, value) >= 0.5) { //Check of the Jaccard similarity
                                    long finish = System.currentTimeMillis();
                                    System.out.println("Probably key: " + Base64.getEncoder().encodeToString(key.getEncoded()) + "\nPASSWORD: " + result + " [Jaccard similarity " + jaccard.similarity(result, value) + " with " + value + "]");
                                    System.out.println("time: " + (finish - start) / 1000f + " sec");
                                    count = (int) Math.round(Math.pow(Q.length, 10));
                                    break;
                                }
                            }
                        }
                    }
                }else{ //Parallel Attack on Key
                    start = System.currentTimeMillis();
                    System.out.println("Searching for the key...");
                    ArrayList<SearchKeyThread> threads = new ArrayList<>();
                    int inizio ,fine ;
                    for (int i = 0; i < numThreads; i++) {
                        //Creation of upper and lower bounds
                        inizio = (((int) Math.round(Math.pow(Q.length, 10))) / numThreads) * i;
                        if (i == numThreads - 1) {
                            fine = (int) Math.round(Math.pow(Q.length, 10));
                        } else {
                            fine = (((int) Math.round(Math.pow(Q.length, 10))) / numThreads) * (i + 1);
                        }
                        //System.out.println(inizio + " " + fine);
                        threads.add(new SearchKeyThread(inizio, fine, data, start, message, triMap));
                    }
                    for (int i = 0; i < numThreads; i++) {
                        threads.get(i).start();
                    }
                }


            }
        }


        }




}

