package com.example.demo.repository;

import com.example.demo.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // O @EntityGraph traz os itens junto com o pedido, numa consulta so

    @Override
    @EntityGraph(attributePaths = "itens")
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findAllByOrderByDataPedidoDesc();

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findByClienteIdOrderByDataPedidoDesc(Long clienteId);
}