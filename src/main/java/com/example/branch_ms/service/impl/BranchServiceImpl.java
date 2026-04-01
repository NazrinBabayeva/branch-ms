package com.example.branch_ms.service.impl;

import com.example.branch_ms.mapper.BranchMapper;
import com.example.branch_ms.model.dto.request.BranchRequestDto;
import com.example.branch_ms.model.dto.response.BranchResponseDto;
import com.example.branch_ms.model.entity.Branch;
import com.example.branch_ms.repo.BranchRepository;
import com.example.branch_ms.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private static final Logger log = LoggerFactory.getLogger(BranchServiceImpl.class);

    private final BranchRepository branchRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BranchMapper branchMapper;

    private String getRedisKey(Long id) {
        return "branch:" + id;
    }

    @Override
    public BranchResponseDto createBranch(BranchRequestDto request) {
        log.debug("-----Mapping BranchRequestDto to Branch entity: {}", request);

        Branch branch = branchMapper.toEntity(request);

        try {
            Branch saved = branchRepository.save(branch);
            log.info("-----Branch saved in DB: {} - {}", saved.getId(), saved.getName());

            saveToRedis(saved.getId(), saved);

            return branchMapper.toDto(saved);
        } catch (Exception e) {
            log.error("------Failed to create branch", e);
            throw e;
        }
    }

    @Override
    public List<BranchResponseDto> createBranches(List<BranchRequestDto> requests) {
        log.trace("------Mapping list of BranchRequestDto to Branch entities, size: {}", requests.size());

        List<Branch> branches = requests.stream()
                .peek(req -> log.trace("------Mapping request: {}", req))
                .map(branchMapper::toEntity)
                .collect(Collectors.toList());

        try {
            List<Branch> savedBranches = branchRepository.saveAll(branches);
            log.info("------Saved {} branches in DB", savedBranches.size());

            savedBranches.forEach(branch -> saveToRedis(branch.getId(), branch));

            return savedBranches.stream()
                    .map(branchMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("------Failed to create branches", e);
            throw e;
        }
    }

    private void saveToRedis(Long id, Branch branch) {
        log.debug("-----Saving branch to Redis with id: {}", id);
        try {
            redisTemplate.opsForValue().set(getRedisKey(id), branch);
            log.info("-----Branch saved to Redis: {}", id);
        } catch (Exception e) {
            log.error("-----Failed to save branch to Redis: {}", id, e);
        }
    }

    @Override
    public BranchResponseDto getBranchFromRedis(Long id) {
        log.debug("-----Fetching branch from Redis with id: {}", id);
        long start = System.currentTimeMillis();
        try {
            Branch branch = (Branch) redisTemplate.opsForValue().get(getRedisKey(id));
            long time = System.currentTimeMillis() - start;
            log.info("------Redis get time for id {}: {}ms", id, time);

            if (branch == null) {
                log.warn("------Branch not found in Redis for id: {}", id);
                return null;
            }

            return branchMapper.toDto(branch);
        } catch (Exception e) {
            log.error("-----Error fetching branch from Redis for id: {}", id, e);
            return null;
        }
    }

    @Override
    public List<BranchResponseDto> getBranchesFromRedis() {
        log.debug("-----Fetching all branches from Redis");
        long start = System.currentTimeMillis();
        try {
            Set<String> keys = redisTemplate.keys("branch:*");
            List<Branch> branches = keys.stream()
                    .map(k -> {
                        log.trace("-----Fetching Redis key: {}", k);
                        return (Branch) redisTemplate.opsForValue().get(k);
                    })
                    .collect(Collectors.toList());

            long time = System.currentTimeMillis() - start;
            log.info("-----Fetched {} branches from Redis in {}ms", branches.size(), time);

            if (branches.isEmpty()) {
                log.warn("-----No branches found in Redis");
            }

            return branches.stream()
                    .map(branchMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("----Error fetching branches from Redis", e);
            return List.of();
        }
    }

    @Override
    public BranchResponseDto getBranchFromDb(Long id) {
        log.debug("-----Fetching branch from DB with id: {}", id);
        long start = System.currentTimeMillis();
        try {
            Branch branch = branchRepository.findById(id).orElse(null);
            long time = System.currentTimeMillis() - start;
            log.info("-----DB get time for id {}: {}ms", id, time);

            if (branch == null) {
                log.warn("-----Branch not found in DB for id: {}", id);
                return null;
            }

            return branchMapper.toDto(branch);
        } catch (Exception e) {
            log.error("-----Error fetching branch from DB for id: {}", id, e);
            return null;
        }
    }

    @Override
    public List<BranchResponseDto> getBranchesFromDb() {
        log.debug("-----Fetching all branches from DB");
        long start = System.currentTimeMillis();
        try {
            List<Branch> branches = branchRepository.findAll();
            long time = System.currentTimeMillis() - start;
            log.info("-----Fetched {} branches from DB in {}ms", branches.size(), time);

            if (branches.isEmpty()) {
                log.warn("-----No branches found in DB");
            }

            return branches.stream()
                    .map(branchMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("-----Error fetching branches from DB", e);
            return List.of();
        }
    }

    @Override
    public void syncRedisWithDb() {
        log.info("-----Starting Redis sync with DB...");

        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.info("-----Redis database flushed");

            List<Branch> branches = branchRepository.findAll();
            log.info("------Fetched {} branches from DB", branches.size());

            branches.forEach(branch -> {
                redisTemplate.opsForValue().set("branch:" + branch.getId(), branch);
                log.debug("------Saved branch {} to Redis", branch.getId());
            });

            log.info("------Redis sync completed successfully");
        } catch (Exception e) {
            log.error("------Error syncing Redis with DB", e);
            throw e;
        }
    }
}