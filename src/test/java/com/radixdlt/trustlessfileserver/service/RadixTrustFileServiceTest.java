package com.radixdlt.trustlessfileserver.service;

import com.radixdlt.trustlessfileserver.commons.RadixUtil;
import com.radixdlt.trustlessfileserver.domain.FileDetails;
import com.radixdlt.trustlessfileserver.domain.ValidityDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static com.radixdlt.trustlessfileserver.service.TestUtil.toHex;
import static org.junit.jupiter.api.Assertions.*;

class RadixTrustFileServiceTest {

    private RadixTrustFileService radixTrustFileService;
    private List<byte[]> chunkSHA256Hashes;
    private File file;

    @BeforeEach
    public void setup() {
        radixTrustFileService = new RadixTrustFileService();
        file = new File(getClass()
                .getClassLoader()
                .getResource("./download.jpg")
                .getFile());
        chunkSHA256Hashes = TestUtil.getChunkSHA256Hashes(file);
    }

    @Test
    void test_getChunkSHA256Hashes_Success() {
        FileDetails fileDetails = radixTrustFileService.getChunkSHA256Hashes(file);
        assertEquals(fileDetails.getTotalPieces(), chunkSHA256Hashes.size());
    }

    @Test
    void test_getChunkSHA256Hashes_Failure_NoFilePassed() {
        File nullFile = null;
        assertThrows(RuntimeException.class, () -> radixTrustFileService.getChunkSHA256Hashes(null));
    }

    @Test
    void test_computeSHA256TreeHash_Success() {
        FileDetails fileDetails = radixTrustFileService.getChunkSHA256Hashes(file);
        fileDetails = radixTrustFileService.computeSHA256TreeHash(fileDetails);
        String rootHash = "458e88d70beeebd02544c0af5d710e43dbe1f2c0baf495a445ecbf59922f6e38";
        assertEquals(rootHash, fileDetails.getRootHash());
        assertEquals(chunkSHA256Hashes.size(), fileDetails.getTotalPieces());
        assertEquals(fileDetails.getLevel() - 1, 4);//excluding the initial level

    }

    @Test
    void test_computeSHA256TreeHash_Failure() {
        assertThrows(RuntimeException.class, () -> radixTrustFileService.computeSHA256TreeHash(null));
    }

    @Test
    void test_getBinaryContentAndItsProofPieces_AndAbleToRecreate_Tree() {
        radixTrustFileService.computeSHA256RootHash(file);
        byte[] digest1;
        byte[] digest2;
        FileDetails fileDetails = radixTrustFileService.getRootHashes().get(0);
        String siblingHashof6 = toHex(fileDetails.getLevelToHashData().get(0).get(7));
        String hashValueAtLevel1Index2 = toHex(fileDetails.getLevelToHashData().get(1).get(2));
        String hashValueAtLeve2Index0 = toHex(fileDetails.getLevelToHashData().get(2).get(0));
        String hashValueAtLeve3Index1 = toHex(fileDetails.getLevelToHashData().get(3).get(1));
        ValidityDetails validityDetails = radixTrustFileService.getBinaryContentAndItsProofPieces(fileDetails.getRootHash(),
                6);
        List<String> proofHashes = Arrays.asList(siblingHashof6, hashValueAtLevel1Index2, hashValueAtLeve2Index0, hashValueAtLeve3Index1);
        assertEquals(proofHashes, validityDetails.getProof());
        String hashValueAtLevel1Index3 = toHex(fileDetails.getLevelToHashData().get(1).get(3));

             digest1 = fileDetails.getLevelToHashData().get(0).get(6);  //pieceId
             digest2 = fileDetails.getLevelToHashData().get(0).get(7);  //sibling
        //asserting the combined value
        assertTrue(hashValueAtLevel1Index3.equals(TestUtil.doHash(digest1,digest2)));

        String hashValueAtLevel2Index1 = toHex(fileDetails.getLevelToHashData().get(2).get(1));
        digest1 = fileDetails.getLevelToHashData().get(1).get(2);  //uncle
        digest2 = fileDetails.getLevelToHashData().get(1).get(3);  //uncle's cousin
        //asserting the combined value
        assertTrue(hashValueAtLevel2Index1.equals(TestUtil.doHash(digest1,digest2)));

        String hashValueAtLevel3Index0 = toHex(fileDetails.getLevelToHashData().get(3).get(0));
        digest1 = fileDetails.getLevelToHashData().get(2).get(0);  //uncle
        digest2 = fileDetails.getLevelToHashData().get(2).get(1);  //uncle's cousin
        //asserting the combined value
        assertTrue(hashValueAtLevel3Index0.equals(TestUtil.doHash(digest1,digest2)));


        String hashValueAtLeve4Index0 = toHex(fileDetails.getLevelToHashData().get(4).get(0));
        digest1 = fileDetails.getLevelToHashData().get(3).get(0);  //uncle
        digest2 = fileDetails.getLevelToHashData().get(3).get(1);  //uncle's cousin
        //asserting the combined value
        assertTrue(hashValueAtLeve4Index0.equals(TestUtil.doHash(digest1,digest2)));

    }
}
