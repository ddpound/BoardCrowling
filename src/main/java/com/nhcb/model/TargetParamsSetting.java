package com.nhcb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TargetParamsSetting {

    private Integer index;
    private Object startParams;
    private Boolean addNumber;
    private Integer addValue;
}
