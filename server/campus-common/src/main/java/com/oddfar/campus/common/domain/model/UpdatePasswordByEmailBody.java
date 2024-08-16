package com.oddfar.campus.common.domain.model;


import lombok.Data;

@Data
public class UpdatePasswordByEmailBody {

    private String email;
    private String password;

}
