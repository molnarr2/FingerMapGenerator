/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mlnr.util;

/** This class is used to create a 15 alpha-numeric which is 36^15 combinations. <p>
 * Total number of combinations: 221,073,919,720,733,357,899,776. <p>
 * This class will generate CD Keys and instant download keys.
 *
 * @author Robert Molnar
 */
public class KeyGenerator {
    
    public KeyGenerator() {
        
    }
    
    /** This will test the key generator.
     */
    public static void main(String []args) {
        int numbersGenerated = 10;
        String []numbers = new String[numbersGenerated];
        for (int i=1; i < numbersGenerated; i++) {
            numbers[i] = generateCDKey();
            System.out.println("" + numbers[i]);
            long seedNumber = getSeedNumber(numbers[i]);
            if (seedNumber == -1)
                System.out.println("bad number");
            
        }
    }
    
    /** This will the seed number from the key. key must be in format: ******-******-******
     * @param key must be in format: ******-******-******
     * @return a seed number, else -1 not a valid key.
     */
    public static long getSeedNumber(String key) {
        int keyLength = key.length();
        if (keyLength != 18 && keyLength != 20)
            throw new IllegalArgumentException("key[" + key + "] is not in the format ******-******-******");
        key = key.toUpperCase();
        
        // Remove the '-' from the string.
        String strCDKey = key;
        if (keyLength == 20)
            strCDKey = key.substring(0, 6) + key.substring(7, 13) + key.substring(14, 20);
        
        // De-encrypt the big key.
        Base36 cdKey = new Base36(strCDKey);
        cdKey = cdKey.encryptAlgorithm1(false);
        cdKey = cdKey.encryptAlgorithm2(false);
        cdKey = cdKey.encryptAlgorithm3(false);
        
        // Get the 3 keys from the big key.
        Base36 key1 = cdKey.getSub(0, 6);
        Base36 key2 = cdKey.getSub(6, 12);
        Base36 key3 = cdKey.getSub(12, 18);
        
        // De-encrypt the smaller keys.
        key1 = key1.encryptAlgorithm1(false);
        key2 = key2.encryptAlgorithm2(false);
        key3 = key3.encryptAlgorithm3(false);
        
        // Each key must be equal.
        if (key1.equals(key2) && key1.equals(key3)) {
            return key1.toLong();
        } else
            return -1; // not valid key.
    }
    
    /** This will generate a key needed by a CD. This will not generate a key used by the instant download. The seedNumber will
     * is used to generate a key from that number. The same seedNumber will always generate the same number. <p>
     * Total combinations is 36^18.
     * @param seedNumber is a number between 1 and 1,073,741,824, from 1 to 2^30.
     * @return a 18 sequence of alpha-numeric letters, so that the length of the string is 18. Letters are in Caps. Format is ******-******-******
     */
    public static String generateCDKey() {
        int seedNumber = (int)((double)Math.random() * (double)1000000000);
        
        // Convert number to base 36 and buffer out to 6 positions.
        Base36 seedBase36 = new Base36(seedNumber);
        seedBase36.bufferZeros(6);
        
        // Encrypt the key.
        Base36 encrypted1 = seedBase36.encryptAlgorithm1(true);
        Base36 encrypted2 = seedBase36.encryptAlgorithm2(true);
        Base36 encrypted3 = seedBase36.encryptAlgorithm3(true);
        
        // Create one big key.
        Base36 cdKey = new Base36(encrypted1);
        cdKey.append(encrypted2);
        cdKey.append(encrypted3);
        
        // Encrypt the big key.
        cdKey = cdKey.encryptAlgorithm3(true);
        cdKey = cdKey.encryptAlgorithm2(true);
        cdKey = cdKey.encryptAlgorithm1(true);
        
        // Now return the generated CD KEY.
        return formatKey(cdKey);
    }
    
    /** This will format the key to ******-******-******
     * @param cdKey must have a length of 18 and is the key to be formatted.
     * @return ******-******-******
     */
    private static String formatKey(Base36 cdKey) {
        if (cdKey.length() != 18)
            throw new IllegalArgumentException("cdKey[" + cdKey + "] is not length of 18.");
        
        String str = cdKey.toString();
        String strCDKey = str.substring(0, 6) + "-" + str.substring(6, 12) + "-" + str.substring(12, 18);
        return strCDKey;
    }
}

class Base36 {
    /** First number is the lowest and the last number is the highest. */
    int []internalBase36;
    
    /** This will create a base36 number from a base10.
     */
    Base36(int base10) {
        internalBase36 = convertBase10ToBase36(base10);
    }
    
    /** This will create a Base36 from the string.
     * @param base36 is a string in the Base36 format.
     */
    Base36(String base36) {
        int length = base36.length();
        internalBase36 = new int[length];
        
        // Load up the internal36 base.
        for (int i=0, j=length-1; i < length; i++, j--)
            internalBase36[i] = convertCharToInt(base36.charAt(j));
    }
    
    /** Create a new Base36.
     * @param array is an array of integers in format 36.
     */
    private Base36(int []array) {
        internalBase36 = new int[array.length];
        
        for (int i=0; i < array.length; i++)
            internalBase36[i] = array[i];
    }
    
    /** Creates a Base36 that is a duplicate of b.
     * @param b is the Base36 that this Base36 should duplicate.
     */
    Base36(Base36 b) {
        this.internalBase36 = new int[b.internalBase36.length];
        for (int i=0; i < internalBase36.length; i++)
            this.internalBase36[i] = b.internalBase36[i];
    }
    
    /** @return the length of this Base36.
     */
    public int length() {
        return internalBase36.length;
    }
    
    /** Append the Base36 b to this Base36.
     * @param b is to be appended to this Base36.
     */
    public void append(Base36 b) {
        int []array = new int[b.internalBase36.length + internalBase36.length];
        
        // Add the internal array.
        int i=0;
        for (i=0; i < internalBase36.length; i++)
            array[i] = internalBase36[i];
        
        // Now add the b array.
        for (int j=0; j < b.internalBase36.length; j++, i++)
            array[i] = b.internalBase36[j];
        
        internalBase36 = array;
    }
    
    /** Checks to see if they are equal.
     * @param b is to see if it equals this Base36.
     * @return true if they are equal.
     */
    public boolean equals(Base36 b) {
        if (b.internalBase36.length != internalBase36.length)
            return false;
        for (int i=0; i < internalBase36.length; i++) {
            if (b.internalBase36[i] != internalBase36[i])
                return false;
        }
        
        return true;
    }
    
    /** This will create a new Base36 key from the current one by using the indexStart and indexEnd on the array of integers that make
     * up the Base36.
     * @param indexStart is the start in the array.
     * @param indexEnd is the end in the array. It will pick up to but not including indexEnd number.
     * @return new Base36 of the sub part of this Base36.
     */
    public Base36 getSub(int indexStart, int indexEnd) {
        int []array = new int[indexEnd-indexStart];
        
        for (int i=0, j=indexStart; j < indexEnd; i++,j++)
            array[i] = internalBase36[j];
        
        return new Base36(array);
    }
    
    /** This will buffer the base 36 number. This will enlarge the size of the base36 by placing trailing zeros if the buffer size is larger than the current
     * size of the buffer.
     * @param bufferSize is the bufferSize the Base36 should be. If the bufferSize is bigger than what it currently is
     * then it will fill it with zeros.
     */
    public void bufferZeros(int bufferSize) {
        if (bufferSize < 1)
            throw new IllegalArgumentException("bufferSize[" + bufferSize + "] must be greater than zero.");
        
        int []newBuffer = new int[bufferSize];
        
        for (int i=0; i < internalBase36.length; i++)
            newBuffer[i] = internalBase36[i];
        
        // Make the internal now the new buffer that was created.
        internalBase36 = newBuffer;
    }
    
    /** This will convert the number to base 36.
     * @param base10Number is a number from base 10.
     * @return the base10 as an array of base 36 numbers.
     */
    private int[] convertBase10ToBase36(int base10Number) {
        int[] arr7 = new int[100];
        
        int i=0;
        int number = base10Number;
        while(true) {
            arr7[i] = number % 36;
            number = number / 36;
            
            i++;
            if (number == 0)
                break;
            
        }
        
        // Create a new array with the numbers from the conversion.
        int []array = new int[i];
        
        for (int a=0; a < i; a++)
            array[a] = arr7[a];
        
        return array;
    }
    
    /** This will create a new Base36 encrypted by using algorithm 1.
     * @param encryptIt is true if it should encrypt it, else false to un-encrypt it.
     */
    public Base36 encryptAlgorithm1(boolean encryptIt) {
        Base36 encrypt = new Base36(this);
        boolean bToggle = true;
        
        /** encrypt the numbers.
         */
        for (int i=0; i < encrypt.internalBase36.length; i++) {
            if (bToggle)
                encrypt.internalBase36[i] = encryptAdd(encrypt.internalBase36[i], i + 7, encryptIt);
            else
                encrypt.internalBase36[i] = encryptSubtract(encrypt.internalBase36[i], i + 5, encryptIt);
            
            bToggle = !bToggle;
        }
        
        return encrypt;
    }
    
    /** This will create a new Base36 encrypted by using algorithm 1.
     * @param encryptIt is true if it should encrypt it, else false to un-encrypt it.
     */
    public Base36 encryptAlgorithm2(boolean encryptIt) {
        Base36 encrypt = new Base36(this);
        boolean bToggle = true;
        
        /** encrypt the numbers.
         */
        for (int i=0; i < encrypt.internalBase36.length; i++) {
            if (bToggle)
                encrypt.internalBase36[i] = encryptAdd(encrypt.internalBase36[i], i + 29, encryptIt);
            else
                encrypt.internalBase36[i] = encryptSubtract(encrypt.internalBase36[i], i + 1, encryptIt);
            
            bToggle = !bToggle;
        }
        
        return encrypt;
    }
    
    /** This will create a new Base36 encrypted by using algorithm 1.
     * @param encryptIt is true if it should encrypt it, else false to un-encrypt it.
     */
    public Base36 encryptAlgorithm3(boolean encryptIt) {
        Base36 encrypt = new Base36(this);
        boolean bToggle = true;
        
        /** encrypt the numbers.
         */
        for (int i=0; i < encrypt.internalBase36.length; i++) {
            if (bToggle)
                encrypt.internalBase36[i] = encryptSubtract(encrypt.internalBase36[i], i + 31, encryptIt);
            else
                encrypt.internalBase36[i] = encryptSubtract(encrypt.internalBase36[i], i + 4, encryptIt);
            
            bToggle = !bToggle;
        }
        
        return encrypt;
    }
    
    /** This will encrypt the number by adding to it.
     * @param number is the number to be encrypted.
     * @param add is the addition to it.
     * @param encrypt is true if this should encrypt it, else false unencrypt it.
     * @return a base 36 number. The number is modulated so that it will always be between 0-35.
     */
    private int encryptAdd(int number, int add, boolean encrypt) {
        add = add % 36;
        if (encrypt)
            return (number + add) % 36;
        return (number + (36 - add)) % 36;
    }
    
    /** This will encrypt the number by subtracting to it.
     * @param number is the number to be encrypted.
     * @param sub is the subtraction to it.
     * @param encrypt is true if this should encrypt it, else false unencrypt it.
     * @return a base 36 number. The number is modulated so that it will always be between 0-35.
     */
    private int encryptSubtract(int number, int sub, boolean encrypt) {
        sub = sub % 36;
        if (encrypt)
            return (number + (36 - sub)) % 36;
        return (number + sub) % 36;
    }
    
    /** This will convert the base36 to a long.
     * @return the long that represents this number.
     */
    public long toLong() {
        int integer = 0;
        
        for (int i=0; i < internalBase36.length; i++)
            integer += internalBase36[i] * power(36, i);
        
        return integer;
    }
    
    /** This will power the numberToRaise by raiseTo.
     * @param numberToRaise is the number to be raised.
     * @param raiseTo is the number to raise it to.
     * @return power of the numberToRaise to by raiseTo.
     */
    private long power(int numberToRaise, int raiseTo) {
        if (raiseTo == 0)
            return numberToRaise;
        
        int total = 1;
        for (int i=0; i < raiseTo; i++) {
            total *= numberToRaise;
        }
        
        return total;
    }
    
    /** This will create a String of the Base36. All letters are in Capital form. Letters are used for any
     * number above 9.
     * @return a String of the Base36.
     */
    public String toString() {
        StringBuffer sBuffer = new StringBuffer();
        
        for (int i=internalBase36.length-1; i >= 0; i--)
            sBuffer.append(convertNumberToChar(internalBase36[i]));
        
        return sBuffer.toString();
    }
    
    /** This will convert the char to an integer from 0-35.
     * @param c is 0-9, A-Z. Must be in caps.
     * @return 0-35.
     */
    private int convertCharToInt(char c) {
        switch(c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'A':
                return 10;
            case 'B':
                return 11;
            case 'C':
                return 12;
            case 'D':
                return 13;
            case 'E':
                return 14;
            case 'F':
                return 15;
            case 'G':
                return 16;
            case 'H':
                return 17;
            case 'I':
                return 18;
            case 'J':
                return 19;
            case 'K':
                return 20;
            case 'L':
                return 21;
            case 'M':
                return 22;
            case 'N':
                return 23;
            case 'O':
                return 24;
            case 'P':
                return 25;
            case 'Q':
                return 26;
            case 'R':
                return 27;
            case 'S':
                return 28;
            case 'T':
                return 29;
            case 'U':
                return 30;
            case 'V':
                return 31;
            case 'W':
                return 32;
            case 'X':
                return 33;
            case 'Y':
                return 34;
            case 'Z':
                return 35;
            default:
                throw new IllegalArgumentException("Char[" + c + "] is not in base 36.");
        }
    }
    
    /** This will convert the number in base36 to a char.
     * @param numberInBase36 is a number between 0-35.
     * @return a letter in caps or a number. number for 0-9 and letters for 10-35.
     */
    private char convertNumberToChar(int numberInBase36) {
        switch (numberInBase36) {
            case 0:
                return '0';
            case 1:
                return '1';
            case 2:
                return '2';
            case 3:
                return '3';
            case 4:
                return '4';
            case 5:
                return '5';
            case 6:
                return '6';
            case 7:
                return '7';
            case 8:
                return '8';
            case 9:
                return '9';
            case 10:
                return 'A';
            case 11:
                return 'B';
            case 12:
                return 'C';
            case 13:
                return 'D';
            case 14:
                return 'E';
            case 15:
                return 'F';
            case 16:
                return 'G';
            case 17:
                return 'H';
            case 18:
                return 'I';
            case 19:
                return 'J';
            case 20:
                return 'K';
            case 21:
                return 'L';
            case 22:
                return 'M';
            case 23:
                return 'N';
            case 24:
                return 'O';
            case 25:
                return 'P';
            case 26:
                return 'Q';
            case 27:
                return 'R';
            case 28:
                return 'S';
            case 29:
                return 'T';
            case 30:
                return 'U';
            case 31:
                return 'V';
            case 32:
                return 'W';
            case 33:
                return 'X';
            case 34:
                return 'Y';
            case 35:
                return 'Z';
            default:
                throw new IllegalArgumentException("Not in base 36 the number[" + numberInBase36 + "] is.");
        }
    }
}