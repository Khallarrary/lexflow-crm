package com.lexflow.api.dto;

import com.lexflow.domain.enums.TipoPessoa;
import com.lexflow.domain.model.Cliente;

public record ClienteResponse(
        Long id,
        String nome,
        TipoPessoa tipoPessoa,
        String cpfCnpj,
        String telefone,
        String email,
        String observacoes
) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTipoPessoa(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getObservacoes()
        );
    }
}