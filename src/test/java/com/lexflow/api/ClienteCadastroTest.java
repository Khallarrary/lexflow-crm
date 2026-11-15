package com.lexflow.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteCadastroTest {

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Test
    void deveCadastrarClienteEPersistirNoPostgreSQL() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String json = """
                {
                  "nome": "Cliente CI",
                  "tipoPessoa": "FISICA",
                  "cpfCnpj": null,
                  "email": "%s"
                }
                """.formatted(email);

        HttpResponse<String> resposta = cadastrar(json);

        assertThat(resposta.statusCode()).isEqualTo(201);
        assertThat(resposta.body()).contains("\"nome\":\"Cliente CI\"");

        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM cliente WHERE email = ?",
                Long.class,
                email
        );
        assertThat(id).isNotNull().isPositive();
        assertThat(resposta.body()).containsPattern("\"id\"\\s*:\\s*" + id + "(?:,|\\s*})");

        String tipoPessoa = jdbcTemplate.queryForObject(
                "SELECT tipo_pessoa FROM cliente WHERE id = ?",
                String.class,
                id
        );
        assertThat(tipoPessoa).isEqualTo("FISICA");
    }

    @Test
    void deveRejeitarNomeEmBrancoSemPersistirCliente() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String json = """
                {
                  "nome": "   ",
                  "tipoPessoa": "FISICA",
                  "email": "%s"
                }
                """.formatted(email);

        HttpResponse<String> resposta = cadastrar(json);

        assertThat(resposta.statusCode()).isEqualTo(400);
        Long quantidade = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM cliente WHERE email = ?",
                Long.class,
                email
        );
        assertThat(quantidade).isZero();
    }

    private HttpResponse<String> cadastrar(String json) throws Exception {
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + porta + "/clientes"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());
    }
}
