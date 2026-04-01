package com.example.branch_ms.service;

import com.example.branch_ms.model.dto.request.BranchRequestDto;
import com.example.branch_ms.model.dto.response.BranchResponseDto;

import java.util.List;

public interface BranchService {

    BranchResponseDto createBranch(BranchRequestDto request);

    List<BranchResponseDto> createBranches(List<BranchRequestDto> requests);

    BranchResponseDto getBranchFromRedis(Long id);

    List<BranchResponseDto> getBranchesFromRedis();

    BranchResponseDto getBranchFromDb(Long id);

    List<BranchResponseDto> getBranchesFromDb();

    void syncRedisWithDb();
}