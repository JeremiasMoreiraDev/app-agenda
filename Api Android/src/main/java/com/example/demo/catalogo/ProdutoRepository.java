package com.example.demo.catalogo;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository
        extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrueOrderByNomeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarPorIdComBloqueio(
            @Param("id") Long id
    );
}