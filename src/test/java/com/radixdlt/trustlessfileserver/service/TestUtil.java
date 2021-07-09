package com.radixdlt.trustlessfileserver.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestUtil {

    public static List<byte[]> getChunkSHA256Hashes(File inputFile) {
        if(Objects.isNull(inputFile))
            throw new RuntimeException("Please pass a file");
        List<byte[]> chunkSHA256Hashes = new ArrayList<>();
        FileInputStream fileInputStream = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            fileInputStream = new FileInputStream(inputFile);
            byte[] buff = new byte[1024];
            int bytesRead;
            while((bytesRead = fileInputStream.read(buff, 0, 1024))>0){
                md.reset();
                md.update(buff, 0, bytesRead);
                chunkSHA256Hashes.add(md.digest());
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                    System.err.printf("Exception while closing %s.\n %s", inputFile.getName(),
                            ex.getMessage());
                }
            }
        }

        return chunkSHA256Hashes;
    }


    public static String doHash(byte[] digest1, byte[] digest2){
        byte[] combinedDigest = new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(digest1);
            md.update(digest2);
            combinedDigest = md.digest();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return toHex(combinedDigest);
    }


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

}
