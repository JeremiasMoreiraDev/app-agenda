package com.example.demo.repository;

import com.example.demo.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    // Busca as mensagens da OS em ordem cronológica (da mais antiga para a mais recente)
    List<Mensagem> findByOrdemServicoIdOrderByDataHoraAsc(Long ordemServicoId);
}