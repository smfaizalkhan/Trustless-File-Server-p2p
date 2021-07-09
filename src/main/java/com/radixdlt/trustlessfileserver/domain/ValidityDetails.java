package com.radixdlt.trustlessfileserver.domain;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidityDetails {
    @Expose
    private String content;
    @Expose
    private List<String> proof;
}
