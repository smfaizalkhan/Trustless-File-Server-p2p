package com.radixdlt.trustlessfileserver.domain;


import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDetails {
    @Expose
    private String rootHash;
    @Expose
    private Integer totalPieces;
    private Integer level;
    private List<String> binaryContent;
    private List<byte[]> chunkSHA256Hashes;
    private Map<Integer, List<byte[]>> levelToHashData;
}
