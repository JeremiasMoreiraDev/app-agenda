package com.example.demo.repository;

import com.example.demo.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("SELECT a FROM Agendamento a WHERE a.cliente.id = :clienteId")
    List<Agendamento> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.colaborador.id = :colaboradorId")
    long countByColaboradorId(@Param("colaboradorId") Long colaboradorId);

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.colaborador.id = :colaboradorId AND a.status = :status")
    long countByColaboradorIdAndStatus(@Param("colaboradorId") Long colaboradorId, @Param("status") String status);

}