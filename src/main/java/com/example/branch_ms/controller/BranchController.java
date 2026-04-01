package com.example.branch_ms.controller;

import com.example.branch_ms.model.dto.request.BranchRequestDto;
import com.example.branch_ms.model.dto.response.BranchResponseDto;
import com.example.branch_ms.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    public ResponseEntity<BranchResponseDto> createBranch(@RequestBody BranchRequestDto request) {
        BranchResponseDto response = branchService.createBranch(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-insert")
    public ResponseEntity<List<BranchResponseDto>> createBranches(@RequestBody List<BranchRequestDto> requests) {
        List<BranchResponseDto> responses = branchService.createBranches(requests);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/db/{id}")
    public ResponseEntity<BranchResponseDto> getBranchFromDb(@PathVariable Long id) {
        BranchResponseDto response = branchService.getBranchFromDb(id);
        if (response != null) return ResponseEntity.ok(response);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/db")
    public ResponseEntity<List<BranchResponseDto>> getBranchesFromDb() {
        List<BranchResponseDto> responses = branchService.getBranchesFromDb();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/redis/{id}")
    public ResponseEntity<BranchResponseDto> getBranchFromRedis(@PathVariable Long id) {
        BranchResponseDto response = branchService.getBranchFromRedis(id);
        if (response != null) return ResponseEntity.ok(response);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/redis")
    public ResponseEntity<List<BranchResponseDto>> getBranchesFromRedis() {
        List<BranchResponseDto> responses = branchService.getBranchesFromRedis();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/sync-redis")
    public ResponseEntity<String> syncRedis() {
        branchService.syncRedisWithDb();
        return ResponseEntity.ok("Redis synced with DB");
    }
}