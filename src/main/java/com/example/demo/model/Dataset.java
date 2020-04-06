package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dataset {

    @Id
    private String id;

    @NotEmpty
    private String name;

    @NotEmpty
    private Integer rows;

    @NotEmpty
    private Date createdAt;

    private Date modifiedAt;

    @DBRef
    @JsonIgnore
    private User user;

    @JsonIgnore
    private List<List<String>> data;

}
