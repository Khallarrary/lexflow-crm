package com.lexflow.app.service;

import com.lexflow.app.exception.EscritorioAtualIndisponivelException;
import com.lexflow.domain.model.Cliente;
import com.lexflow.domain.model.Escritorio;
import com.lexflow.infra.config.EscritorioContextHolder;
import com.lexflow.infra.repository.ClienteRepository;
import com.lexflow.infra.repository.EscritorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import com.lexflow.app.exception.ClienteDuplicadoException;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EscritorioRepository escritorioRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          EscritorioRepository escritorioRepository) {
        this.clienteRepository = clienteRepository;
        this.escritorioRepository = escritorioRepository;
    }

    @Transactional
    public Cliente cadastrar(Cliente cliente) {
        Escritorio escritorio = buscarEscritorioAtual();
        cliente.vincularAoEscritorio(escritorio);

        try {
            return clienteRepository.save(cliente);
        } catch (DataIntegrityViolationException ex) {
            if (violacaoDeDocumentoDuplicado(ex)) {
                throw new ClienteDuplicadoException(
                        "Já existe um cliente com este CPF/CNPJ neste escritório", ex);
            }
            throw ex;
        }
    }

    private boolean violacaoDeDocumentoDuplicado(DataIntegrityViolationException ex) {
        String mensagem = ex.getMostSpecificCause().getMessage();
        return mensagem != null && mensagem.contains("uk_cliente_escritorio_cpf_cnpj");
    }

    private Escritorio buscarEscritorioAtual() {
        Long escritorioId = EscritorioContextHolder.obter()
                .orElseThrow(() -> new EscritorioAtualIndisponivelException(
                        "Escritório atual não definido"));

        return escritorioRepository.findById(escritorioId)
                .filter(Escritorio::isAtivo)
                .orElseThrow(() -> new EscritorioAtualIndisponivelException(
                        "Escritório atual inexistente ou inativo"));
    }
}