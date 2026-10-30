package com.example.demo.repository; // Ajuste para o seu pacote

import com.example.demo.model.OrdemServico; // Ajuste para o caminho da sua entidade
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
}
