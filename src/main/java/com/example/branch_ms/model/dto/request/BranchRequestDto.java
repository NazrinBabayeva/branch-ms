package com.example.branch_ms.model.dto.request;

import lombok.Data;

@Data
public class BranchRequestDto {

    private String name;
    private String address;
    private String coordinates;
    private Boolean status;
}
