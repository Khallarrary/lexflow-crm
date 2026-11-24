package com.lexflow.app;

import com.lexflow.app.exception.EscritorioAtualIndisponivelException;
import com.lexflow.app.service.ClienteService;
import com.lexflow.domain.enums.TipoPessoa;
import com.lexflow.domain.model.Cliente;
import com.lexflow.domain.model.Escritorio;
import com.lexflow.infra.config.EscritorioContextHolder;
import com.lexflow.infra.repository.EscritorioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.lexflow.app.exception.ClienteDuplicadoException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class EscritorioIsolamentoTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private EscritorioRepository escritorioRepository;

    private Escritorio escritorioA;
    private Escritorio escritorioB;

    @BeforeEach
    void criarDoisEscritorios() {
        escritorioA = escritorioRepository.save(new Escritorio("Escritorio Teste A " + UUID.randomUUID()));
        escritorioB = escritorioRepository.save(new Escritorio("Escritorio Teste B " + UUID.randomUUID()));
    }

    @AfterEach
    void limparContexto() {
        EscritorioContextHolder.limpar();
    }

    @Test
    void devePermitirMesmoDocumentoEmEscritoriosDiferentes() {
        String documento = "11122233344";

        EscritorioContextHolder.definir(escritorioA.getId());
        clienteService.cadastrar(novoCliente("Cliente A", documento));

        EscritorioContextHolder.definir(escritorioB.getId());
        Cliente clienteB = clienteService.cadastrar(novoCliente("Cliente B", documento));

        assertThat(clienteB.getId()).isNotNull();
    }

    @Test
    void deveRejeitarDocumentoDuplicadoNoMesmoEscritorio() {
        String documento = "55566677788";

        EscritorioContextHolder.definir(escritorioA.getId());
        clienteService.cadastrar(novoCliente("Primeiro Cadastro", documento));

        assertThrows(ClienteDuplicadoException.class, () ->
                clienteService.cadastrar(novoCliente("Segundo Cadastro", documento))
        );
    }

    @Test
    void deveFalharSemContextoDeEscritorioDefinido() {
        assertThrows(EscritorioAtualIndisponivelException.class, () ->
                clienteService.cadastrar(novoCliente("Sem Contexto", null))
        );
    }

    @Test
    void naoDeveReaproveitarContextoEntreOperacoesConsecutivas() {
        EscritorioContextHolder.definir(escritorioA.getId());
        Cliente clienteA = clienteService.cadastrar(novoCliente("Cliente Do A", null));
        assertThat(clienteA.getEscritorio().getId()).isEqualTo(escritorioA.getId());

        EscritorioContextHolder.limpar();
        EscritorioContextHolder.definir(escritorioB.getId());
        Cliente clienteB = clienteService.cadastrar(novoCliente("Cliente Do B", null));
        assertThat(clienteB.getEscritorio().getId()).isEqualTo(escritorioB.getId());
    }

    private Cliente novoCliente(String nome, String cpfCnpj) {
        return new Cliente(
                nome,
                TipoPessoa.FISICA,
                cpfCnpj,
                null,
                UUID.randomUUID() + "@example.com",
                null
        );
    }
}