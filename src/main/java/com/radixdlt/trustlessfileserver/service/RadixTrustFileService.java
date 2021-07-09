package com.radixdlt.trustlessfileserver.service;

import com.radixdlt.trustlessfileserver.domain.ValidityDetails;
import com.radixdlt.trustlessfileserver.domain.FileDetails;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.radixdlt.trustlessfileserver.commons.RadixUtil.*;

public class RadixTrustFileService {

    static final int ONE_KB = 1024;
    private final List<FileDetails> fileDetailsList;
    private final Map<String, FileDetails> rootHashToFile;
    private List<String> binaryContent;

    public RadixTrustFileService() {
        this.rootHashToFile = new HashMap<>();
        this.binaryContent = new ArrayList<>();
        this.fileDetailsList = new ArrayList<>();
    }

    /**
     * @return List of FileDetails
     */
    public List<FileDetails> getRootHashes() {
        return fileDetailsList;
    }

    /**
     * @param inputFile
     * @return void
     */
    public void computeSHA256RootHash(File inputFile) {
        FileDetails fileDetails = getChunkSHA256Hashes(inputFile);
        fileDetails = computeSHA256TreeHash(fileDetails);
        rootHashToFile.put(fileDetails.getRootHash(), fileDetails);
        fileDetailsList.add(fileDetails);
    }

    /**  Create a  binary tree of sufficient height meaning
     * that the lowest level in the tree has enough nodes to hold all piece hashes in the set
     *  remaining leaves in the tree are assigned a filler hash value of 0 to make it a balanced binary tree
     * @param inputFile ,the file to compute the checksum
     * @return List<byte [ ]> ,list containing checksum of each chunk of the file
     */
    public FileDetails getChunkSHA256Hashes(File inputFile) {
        if (Objects.isNull(inputFile))
            throw new RuntimeException("Please pass a file");
        FileDetails fileDetails = new FileDetails();
        List<byte[]> chunkSHA256Hashes = new ArrayList<>();
        FileInputStream fileInputStream = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            fileInputStream = new FileInputStream(inputFile);
            byte[] buff = new byte[ONE_KB];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buff, 0, ONE_KB)) > 0) {
                md.reset();
                md.update(buff, 0, bytesRead);
                chunkSHA256Hashes.add(md.digest());
                String encodeBinaryContent = Base64.getEncoder().encodeToString(buff);
                binaryContent.add(encodeBinaryContent);
            }

            //set Total No of validpieces()
            fileDetails.setTotalPieces(chunkSHA256Hashes.size());

            int totalFillerPiece = calculateFillerPieces(chunkSHA256Hashes);
            //Fill the totalFiller with byte[0] ..So it hashed at the computeHashMethod
            for (int k = 0; k < totalFillerPiece / 2; k++) {
                byte[] zeroByte = new byte[1024];
                Arrays.fill(zeroByte, (byte) 0);
                md.reset();
                md.update(zeroByte);
                chunkSHA256Hashes.add(md.digest());
            }

            fileDetails.setChunkSHA256Hashes(chunkSHA256Hashes);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                    System.err.printf("Exception while closing %s.\n %s", inputFile.getName(),
                            ex.getMessage());
                }
            }
        }

        return fileDetails;
    }


    /**
     * Calculates the checksum of each node (i (left)and i+2 (right))
     * The process goes till a level where we have one Node (Root Hash)
     * On  each level , the value of the previous level two adjacent nodes (left and right)
     * are concatenated  and hashed
     *
     * @param fileDetails
     * @return FileDetails as json response containing
     * * {rootHash,totalPieces,List of binaryContent,level,List of Hashes}
     */
    public FileDetails computeSHA256TreeHash(FileDetails fileDetails) {
        int level = 1;

        if (Objects.isNull(fileDetails))
            throw new RuntimeException("Please pass valid byte[]");
        List<byte[]> chunkSHA256Hashes = fileDetails.getChunkSHA256Hashes();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            int length = chunkSHA256Hashes.size();
            if (length == 0)
                return null;
            List<byte[]> levelZero = new ArrayList<>();
            // original hash of the contents
            for (int k = 0; k < length; k++) {
                levelZero.add(chunkSHA256Hashes.get(k));
            }
            //level wise hash content stored for each with map of level to hashContent
            Map<Integer, List<byte[]>> mapOfLevelToHash = new HashMap<>();
            mapOfLevelToHash.put(0, levelZero);
            fileDetails.setLevelToHashData(mapOfLevelToHash);
            while (length > 1) {
                List<byte[]> concatenatedDigest = new ArrayList<>();
                int j = 0;
                int i = 0;
                for (; i < length; i += 2, ++j) {
                    byte[] digest1 = chunkSHA256Hashes.get(i);
                    byte[] digest2 = chunkSHA256Hashes.get(i + 1);
                    md.reset();
                    md.update(digest1);
                    md.update(digest2);
                    byte[] combinedDigest = md.digest();
                    chunkSHA256Hashes.set(j, combinedDigest); //iterative hashes
                    concatenatedDigest.add(combinedDigest);   //level wise hashes
                }
                mapOfLevelToHash.put(level, concatenatedDigest);
                fileDetails.setLevelToHashData(mapOfLevelToHash);
                length = j;
                level++;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        final String rootHash = toHex(fileDetails.getChunkSHA256Hashes().get(0));
        fileDetails.setRootHash(rootHash);
        fileDetails.setBinaryContent(binaryContent);
        fileDetails.setLevel(level);
       printTheTree(fileDetails.getLevelToHashData());
        return fileDetails;

    }




    /**
     * @param filesRootHash
     * @param pieceId       fetches the file from root hash,its level
     *                      Iterates over each level to find the sibling and uncle (proof nodes)
     * @return Validity details containing base64 binary content and its list of proof
     */
    public ValidityDetails getBinaryContentAndItsProofPieces(String filesRootHash, Integer pieceId) {
        FileDetails fileDetails = rootHashToFile.get(filesRootHash);
        List<String> proofHashList = new ArrayList<>();
        int level = fileDetails.getLevel() - 1;
        int internalLevel = 0;
        int index = 0;
        String binaryContent = fileDetails.getBinaryContent().get(pieceId);

        // Iterating the height of the tree using level and index value using internalLevel
        while (level > 0) {
            index = (isLeftNode(pieceId)) ? pieceId + 1 : pieceId - 1;
            byte[] value = fileDetails.getLevelToHashData().get(internalLevel).get(index);
            proofHashList.add(toHex(value));
            level--;
            internalLevel++;
            pieceId = pieceId / 2;  // (pieceId/2) as it is a binary tree
        }
        return ValidityDetails.builder()
                .content(binaryContent)
                .proof(proofHashList).build();
    }

}
