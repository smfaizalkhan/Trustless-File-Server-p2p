package com.radixdlt.trustlessfileserver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.radixdlt.trustlessfileserver.domain.ValidityDetails;
import com.radixdlt.trustlessfileserver.service.RadixTrustFileService;

import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static spark.Spark.get;


public class RadixTrustFileServer {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing required filename argument");
            System.exit(-1);
        }
        System.out.println("args"+args[0]);
        File inputFile = new File(args[0]);
        System.out.println("inputFile"+inputFile.getName());

        RadixTrustFileService radixTrustFileService = new RadixTrustFileService();
        radixTrustFileService.computeSHA256RootHash(inputFile);

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        get("/hashes", (req, res) -> {
            res.type("application/json");
           return gson.toJson(radixTrustFileService.getRootHashes());
        });

        get("/piece/:hashId/:pieceIndex", (req, res) -> {
            res.type("application/json");
            ValidityDetails validityDetails = radixTrustFileService
                    .getBinaryContentAndItsProofPieces(req.params(":hashId"),Integer.parseInt(req.params(":pieceIndex")));
            return gson.toJson(validityDetails);
        });
    }
}
