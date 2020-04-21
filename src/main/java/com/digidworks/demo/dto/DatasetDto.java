package com.digidworks.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class DatasetDto {

    @NotEmpty
    private String name;
}
