package com.example.demo.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Visualization {

    @Id
    private String id;

    @NotEmpty
    private String name;

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

    @NotEmpty
    private Date createdAt;

    private Date modifiedAt;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @DBRef
    private Dataset dataset;

    @DBRef
    @JsonIgnore
    private User user;
}
