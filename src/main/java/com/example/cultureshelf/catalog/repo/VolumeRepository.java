package com.example.cultureshelf.catalog.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cultureshelf.catalog.model.Volume;

public interface VolumeRepository extends JpaRepository<Volume, UUID> {
}
