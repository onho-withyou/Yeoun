package com.yeoun.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderModifyDTO {

    @JsonProperty("prc-bld")
    private String prcBld;

    @JsonProperty("prc-bld")
    private String prcFlt;
    private String prcFil;
    private String prcCap;
    private String prcLbl;
    private String remark;

}
