package com.example.demo.service;

import com.example.demo.dto.PerfilRequest;
import com.example.demo.exception.ClienteNaoEncontradoException;
import com.example.demo.model.Cliente;
import com.example.demo.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() { return clienteRepository.findAll(); }
    public Optional<Cliente> buscarPorId(Long id) { return clienteRepository.findById(id); }
    public Cliente salvar(Cliente cliente) { return clienteRepository.save(cliente); }
    public void deletar(Long id) { clienteRepository.deleteById(id); }

    // ---------- Perfil ----------

    public Cliente buscarPerfilPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
    }

    public Cliente buscarPerfilPorEmail(String email) {
        return clienteRepository.findFirstByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
    }

    public Cliente atualizarPerfil(Long id, PerfilRequest dados) {
        Cliente cliente = buscarPerfilPorId(id);
        cliente.setNome(dados.getNome().trim());
        cliente.setTelefone(dados.getTelefone() == null ? null : dados.getTelefone().trim());
        cliente.setTipoImovel(dados.getTipoImovel());
        cliente.setPossuiCarroEletrico(dados.getPossuiCarroEletrico());
        cliente.setPossuiBateria(dados.getPossuiBateria());
        return clienteRepository.save(cliente);
    }
}