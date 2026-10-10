package com.example.demo.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrueOrderByNomeAsc();
}