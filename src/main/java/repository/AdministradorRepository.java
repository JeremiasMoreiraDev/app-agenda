package com.example.demo.repository;

import com.example.demo.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    boolean existsByEmail(String email);
}