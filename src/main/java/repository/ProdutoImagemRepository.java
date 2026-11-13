
package com.example.demo.repository;

import com.example.demo.model.ProdutoImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProdutoImagemRepository
        extends JpaRepository<ProdutoImagem, Long> {

    Optional<ProdutoImagem> findByProdutoId(Long produtoId);
}
