package com.example.ssotestserver.entity;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class User {

    private String user;

    private String username;

    private String password;

    private String url;
}
