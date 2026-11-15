package com.lexflow.api.controller;

import com.lexflow.api.dto.ClienteResponse;
import com.lexflow.api.dto.CriarClienteRequest;
import com.lexflow.app.service.ClienteService;
import com.lexflow.domain.model.Cliente;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse cadastrar(
            @Valid @RequestBody CriarClienteRequest request
    ) {
        Cliente cliente = new Cliente(
                request.nome(),
                request.tipoPessoa(),
                request.cpfCnpj(),
                request.telefone(),
                request.email(),
                request.observacoes()
        );

        Cliente clienteSalvo = clienteService.cadastrar(cliente);

        return ClienteResponse.de(clienteSalvo);
    }
}