package com.lexflow.infra.config;

import com.lexflow.domain.model.Escritorio;
import com.lexflow.infra.repository.EscritorioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Profile("local")
public class EscritorioLocalFilter extends OncePerRequestFilter {

    private final EscritorioRepository escritorioRepository;
    private final String nomeEscritorioFixo;

    public EscritorioLocalFilter(
            EscritorioRepository escritorioRepository,
            @Value("${lexflow.dev.escritorio-nome}") String nomeEscritorioFixo) {
        this.escritorioRepository = escritorioRepository;
        this.nomeEscritorioFixo = nomeEscritorioFixo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            escritorioRepository.findByNome(nomeEscritorioFixo)
                    .filter(Escritorio::isAtivo)
                    .ifPresent(e -> EscritorioContextHolder.definir(e.getId()));

            filterChain.doFilter(request, response);
        } finally {
            EscritorioContextHolder.limpar();
        }
    }
}