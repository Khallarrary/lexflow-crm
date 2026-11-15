package com.lexflow.app.service;

import com.lexflow.domain.model.Cliente;
import com.lexflow.infra.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Cliente cadastrar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }
}