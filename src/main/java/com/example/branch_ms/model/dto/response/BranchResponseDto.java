package com.example.branch_ms.model.dto.response;

import lombok.*;

@Data
@Builder
public class BranchResponseDto {

    private Long id;
    private String name;
    private String address;
    private String coordinates;
    private Boolean status;
}


