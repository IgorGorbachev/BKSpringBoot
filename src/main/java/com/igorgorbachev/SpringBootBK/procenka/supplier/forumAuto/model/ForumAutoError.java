package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumAutoError {
    @JsonProperty("FaultCode")
    private Integer faultCode;

    @JsonProperty("FaultString")
    private String faultString;

    @JsonProperty("Detail")
    private String detail;
}
