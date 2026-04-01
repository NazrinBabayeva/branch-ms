package com.example.branch_ms.model.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "branches", schema = "branch_schema")
@Getter
@Setter
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "branch_seq")
    @SequenceGenerator(name = "branch_seq", sequenceName = "branch_schema.branch_id_seq", allocationSize = 1)
    private Long id;

    private String name;
    private String address;
    private String coordinates;
    private Boolean status;
}