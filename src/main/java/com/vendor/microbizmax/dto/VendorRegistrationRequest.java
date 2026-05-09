package com.vendor.microbizmax.dto;


import lombok.*;

@Getter
@Setter
public class VendorRegistrationRequest {

    private String name;
    private String shopName;
    private String email;
    private String password;
    private String phone;
    private String address;
}