package com.example.branch_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableRedisRepositories
@SpringBootApplication
public class BranchMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BranchMsApplication.class, args);
    }

}
