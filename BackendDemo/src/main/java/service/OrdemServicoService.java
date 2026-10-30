package com.example.demo.service;

import com.example.demo.model.OrdemServico;
import com.example.demo.repository.OrdemServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrdemServicoService {

    @Autowired
    private OrdemServicoRepository repository;

    public List<OrdemServico> listarTodas() {
        return repository.findAll();
    }

    public Optional<OrdemServico> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public OrdemServico salvar(OrdemServico ordemServico) {
        // Aqui você pode colocar regras de negócio antes de salvar (ex: definir status inicial)
        return repository.save(ordemServico);
    }

    public OrdemServico atualizar(Long id, OrdemServico ordemAtualizada) {
        return repository.findById(id).map(ordem -> {
            // Atualize os campos necessários da sua entidade aqui
            // Exemplo:
            // ordem.setDescricao(ordemAtualizada.getDescricao());
            // ordem.setStatus(ordemAtualizada.getStatus());
            return repository.save(ordem);
        }).orElseThrow(() -> new RuntimeException("Ordem de Serviço não encontrada com o ID: " + id));
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}