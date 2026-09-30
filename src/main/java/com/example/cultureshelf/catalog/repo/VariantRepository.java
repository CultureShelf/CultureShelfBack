package com.example.cultureshelf.catalog.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cultureshelf.catalog.model.Variant;

public interface VariantRepository extends JpaRepository<Variant, UUID> {
}
