package com.finance.loan.dto.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IwomiPayoutRequest {

    @JsonProperty("op_type")
    private String opType;

    private String type;

    private String amount; // string, matches their payload format

    @JsonProperty("external_id")
    private String externalId;

    private String motif;

    private String tel;

    private String country;

    @JsonProperty("callback_url")
    private String callbackUrl;
}