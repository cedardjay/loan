package com.finance.loan.dto.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IwomiPayoutResponse {

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("internal_id")
    private String internalId;

    private String message;

    private String status;
}