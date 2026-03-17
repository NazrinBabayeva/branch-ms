package com.example.branch_ms.service;

import com.example.branch_ms.mapper.BranchMapper;
import com.example.branch_ms.model.dto.request.BranchRequestDto;
import com.example.branch_ms.model.dto.response.BranchResponseDto;
import com.example.branch_ms.model.entity.Branch;
import com.example.branch_ms.repo.BranchRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BranchMapper branchMapper;

    private String getRedisKey(Long id) {
        return "branch:" + id;
    }

    public BranchResponseDto createBranch(BranchRequestDto request) {
        Branch branch = branchMapper.toEntity(request);
        Branch saved = branchRepository.save(branch);
        saveToRedis(saved.getId(), saved);
        return branchMapper.toDto(saved);
    }

    public List<BranchResponseDto> createBranches(List<BranchRequestDto> requests) {
        List<Branch> branches = requests.stream()
                .map(branchMapper::toEntity)
                .collect(Collectors.toList());

        List<Branch> savedBranches = branchRepository.saveAll(branches);

        savedBranches.forEach(branch -> saveToRedis(branch.getId(), branch));

        return savedBranches.stream()
                .map(branchMapper::toDto)
                .collect(Collectors.toList());
    }

    private void saveToRedis(Long id, Branch branch) {
        System.out.println("➡️ Redisə gedir: " + id);

        redisTemplate.opsForValue().set("branch:" + id, branch);

        System.out.println("✅ Redisə yazıldı: " + id);
    }

    public BranchResponseDto getBranchFromRedis(Long id) {
        long start = System.currentTimeMillis();
        Branch branch = (Branch) redisTemplate.opsForValue().get(getRedisKey(id));
        long time = System.currentTimeMillis() - start;
        System.out.println("Redis get time for id " + id + ": " + time + "ms");
        return branch != null ? branchMapper.toDto(branch) : null;
    }

    public List<BranchResponseDto> getBranchesFromRedis() {
        long start = System.currentTimeMillis();
        Set<String> keys = redisTemplate.keys("branch:*");
        List<Branch> branches = keys.stream()
                .map(k -> (Branch) redisTemplate.opsForValue().get(k))
                .collect(Collectors.toList());
        long time = System.currentTimeMillis() - start;
        System.out.println("Redis getAll time: " + time + "ms");
        return branches.stream()
                .map(branchMapper::toDto)
                .collect(Collectors.toList());
    }

    public BranchResponseDto getBranchFromDb(Long id) {
        long start = System.currentTimeMillis();
        Branch branch = branchRepository.findById(id).orElse(null);
        long time = System.currentTimeMillis() - start;
        System.out.println("DB get time for id " + id + ": " + time + "ms");
        return branch != null ? branchMapper.toDto(branch) : null;
    }

    public List<BranchResponseDto> getBranchesFromDb() {
        long start = System.currentTimeMillis();
        List<Branch> branches = branchRepository.findAll();
        long time = System.currentTimeMillis() - start;
        System.out.println("DB getAll time: " + time + "ms");
        return branches.stream()
                .map(branchMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void testRedis() {
        try {
            redisTemplate.opsForValue().set("test-key", "Salam Nazrin");
            Object value = redisTemplate.opsForValue().get("test-key");

            System.out.println("✅ Redis write OK: " + value);
        } catch (Exception e) {
            System.err.println("❌ Redis ERROR: " + e.getMessage());
        }
    }
}