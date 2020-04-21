package com.digidworks.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class VisualizationDto {

    @NotEmpty
    private String name;

    @NotEmpty
    private String dataset;

    private boolean showOnDashboard;

    @NotEmpty
    @JsonProperty("xAxis")
    private String xAxis;

    @NotEmpty
    @JsonProperty("yAxis")
    private String yAxis;

    @JsonProperty("xAxisAggregateSum")
    private boolean xAxisAggregateSum;

    @JsonProperty("yAxisAggregateSum")
    private boolean yAxisAggregateSum;

    @JsonProperty("xAxisAggregateAvg")
    private boolean xAxisAggregateAvg;

    @JsonProperty("yAxisAggregateAvg")
    private boolean yAxisAggregateAvg;
}
