package com.digidworks.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataPoint {
    private Object x;
    private Object y;

    @JsonIgnore
    private Integer totalRowsCount = 0;
}
