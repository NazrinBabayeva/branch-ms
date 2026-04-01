package com.example.branch_ms.mapper;

import com.example.branch_ms.model.dto.request.BranchRequestDto;
import com.example.branch_ms.model.dto.response.BranchResponseDto;
import com.example.branch_ms.model.entity.Branch;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    Branch toEntity(BranchRequestDto request);

    BranchResponseDto toDto(Branch branch);
}