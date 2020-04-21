package com.digidworks.demo.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Register {

    private String email;
    private String password;
    private String firstName;
    private String lastName;

}
