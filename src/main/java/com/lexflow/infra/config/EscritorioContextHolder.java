package com.lexflow.infra.config;

import java.util.Optional;

public final class EscritorioContextHolder {

    private static final ThreadLocal<Long> ESCRITORIO_ID = new ThreadLocal<>();

    private EscritorioContextHolder() {
    }

    public static void definir(Long escritorioId) {
        ESCRITORIO_ID.set(escritorioId);
    }

    public static Optional<Long> obter() {
        return Optional.ofNullable(ESCRITORIO_ID.get());
    }

    public static void limpar() {
        ESCRITORIO_ID.remove();
    }
}