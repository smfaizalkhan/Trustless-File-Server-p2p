package com.radixdlt.trustlessfileserver.commons;

import java.util.List;
import java.util.Map;

public class RadixUtil {


    /**
     * Returns the hexadecimal representation of the input byte array
     *
     * @param data a byte[] to convert to Hex characters
     * @return A String containing Hex characters
     */
    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);

        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);

            if (hex.length() == 1) {
                // Append leading zero.
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase();
    }


    /**
     * @param index Check is the passed index is left or right
     * @return boolean
     */
    public static boolean isLeftNode(int index) {
        return (index % 2) == 0;
    }

    /**
     * Calculates total filler pieces for the file to balance the tree
     * @param chunkSHA256Hashes
     * @return int
     */
    public static int calculateFillerPieces(List<byte[]> chunkSHA256Hashes) {
        int level = 1;  //starting from 1 as we are calculating next level
        int length = chunkSHA256Hashes.size() / 2;
        int totalFillerPiece = 0;
        while (length != 0) {
            int fillerpiece = calculateFillerPieceAtEachLevel(level);
            length = length / 2;
            level++;
            totalFillerPiece += fillerpiece;
        }
        return totalFillerPiece;
    }

    /**
     * Calculates total filler pieces for the file at each level the balance the tree
     * @param level
     * @return int
     */
    private static int calculateFillerPieceAtEachLevel(int level) {
        int noOfPieces = 0;
        while (level != 0) {
            // Formula (2 to power of level)
            noOfPieces += Math.pow(2, level);
            level--;
        }
        noOfPieces = (noOfPieces / 2); // As it is binary
        return noOfPieces;
    }

    /**
     * Takes  a Map print the hash values at each level
     * Print the tree in vertically
     * @param levelToHashData
     */

    public static void printTheTree(Map<Integer, List<byte[]>> levelToHashData){

        for(Map.Entry<Integer, List<byte[]>> keyVal : levelToHashData.entrySet()){
            System.out.println("level"+keyVal.getKey());
            for(byte[] value:keyVal.getValue()){
                System.out.print(toHex(value)+"\t");
            }
            System.out.println();
        }

    }
}
