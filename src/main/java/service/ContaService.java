package com.example.demo.service;

import com.example.demo.dto.ContaRequest;
import com.example.demo.dto.ContaResponse;
import com.example.demo.model.Administrador;
import com.example.demo.model.Cliente;
import com.example.demo.model.TipoConta;
import com.example.demo.repository.AdministradorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContaService {

    private final ClienteService clienteService;
    private final AdministradorRepository administradorRepository;

    public ContaService(ClienteService clienteService, AdministradorRepository administradorRepository) {
        this.clienteService = clienteService;
        this.administradorRepository = administradorRepository;
    }

    public List<ContaResponse> listar(TipoConta tipo) {
        List<ContaResponse> lista = new ArrayList<>();

        if (tipo == null || tipo == TipoConta.ADMIN) {
            administradorRepository.findAll()
                    .forEach(a -> lista.add(ContaResponse.deAdministrador(a)));
        }
        if (tipo == null || tipo == TipoConta.CLIENTE) {
            clienteService.listarTodos()
                    .forEach(c -> lista.add(ContaResponse.deCliente(c)));
        }
        return lista;
    }

    public ContaResponse criar(ContaRequest req) {
        if (req.getNome() == null || req.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório.");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail é obrigatório.");
        }
        if (req.getTipo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tipo da conta é obrigatório (CLIENTE ou ADMIN).");
        }

        String nome = req.getNome().trim();
        String email = req.getEmail().trim().toLowerCase();

        if (emailJaExiste(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com esse e-mail.");
        }

        if (req.getTipo() == TipoConta.CLIENTE) {
            Cliente cliente = new Cliente();
            cliente.setNome(nome);
            cliente.setEmail(email);
            cliente.setTelefone(req.getTelefone());
            cliente.setPossuiCarroEletrico("Nao");
            cliente.setPossuiBateria("Nao");
            return ContaResponse.deCliente(clienteService.salvar(cliente));
        }

        Administrador admin = new Administrador();
        admin.setNome(nome);
        admin.setEmail(email);
        admin.setTelefone(req.getTelefone());
        return ContaResponse.deAdministrador(administradorRepository.save(admin));
    }

    public void remover(TipoConta tipo, Long id) {
        if (tipo != TipoConta.CLIENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só é possível remover contas de clientes.");
        }
        if (clienteService.buscarPorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.");
        }
        try {
            clienteService.deletar(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esse cliente possui pedidos ou agendamentos e não pode ser removido.");
        }
    }

    private boolean emailJaExiste(String email) {
        boolean emClientes = clienteService.listarTodos().stream()
                .anyMatch(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(email));
        return emClientes || administradorRepository.existsByEmail(email);
    }
}