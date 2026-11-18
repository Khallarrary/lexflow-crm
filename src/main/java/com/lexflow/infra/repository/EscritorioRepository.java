package com.lexflow.infra.repository;

import com.lexflow.domain.model.Escritorio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscritorioRepository extends JpaRepository<Escritorio, Long> {
}