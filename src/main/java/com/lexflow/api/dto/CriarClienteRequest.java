package com.lexflow.api.dto;

import com.lexflow.domain.enums.TipoPessoa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarClienteRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 200, message = "O nome deve ter até 200 caracteres")
        String nome,

        @NotNull(message = "O tipo de pessoa é obrigatório")
        TipoPessoa tipoPessoa,

        @Size(max = 20, message = "O CPF/CNPJ deve ter até 20 caracteres")
        String cpfCnpj,

        @Size(max = 30, message = "O telefone deve ter até 30 caracteres")
        String telefone,

        @Email(message = "O e-mail deve ter um formato válido")
        @Size(max = 254, message = "O e-mail deve ter até 254 caracteres")
        String email,

        String observacoes

) {
}