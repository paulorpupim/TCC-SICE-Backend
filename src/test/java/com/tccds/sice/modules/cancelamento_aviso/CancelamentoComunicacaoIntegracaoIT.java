package com.tccds.sice.modules.cancelamento_aviso;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.exception.RegraNegocioEventoException;
import com.tccds.sice.modules.cancelamento_aviso.dto.CancelamentoComAvisoResponseDTO;
import com.tccds.sice.modules.credencial.Credencial;
import com.tccds.sice.modules.envio_aviso.EnvioAviso;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.EventoRepository;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.usuario.Usuario;

/**
 * SICE: teste integrado de comunicacao de cancelamentos.
 * Usa somente PostgreSQL DESCARTAVEL sice_concorrencia + bot HTTP simulado local.
 * Nao chama WhatsApp real. Nunca executar em banco com dados importantes.
 *
 * Nao executa com `mvn test` padrao devido ao sufixo IT.
 * Executar explicitamente: -Dtest=CancelamentoComunicacaoIntegracaoIT
 */
@SpringBootTest
@ActiveProfiles("concorrencia")
@Execution(ExecutionMode.SAME_THREAD)
class CancelamentoComunicacaoIntegracaoIT {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final String BANCO_EXCLUSIVO = "sice_concorrencia";
    private static final String SEGREDO_TESTE = "sice-cancelamento-bot-simulado";
    private static final AtomicInteger CHAMADAS_BOT = new AtomicInteger();
    private static final AtomicInteger RESPOSTA_BOT = new AtomicInteger(200);
    private static final AtomicReference<String> CORPO_RECEBIDO = new AtomicReference<>("");
    private static volatile CountDownLatch BOT_RECEBEU = new CountDownLatch(1);
    private static volatile CountDownLatch LIBERAR_BOT = new CountDownLatch(0);
    private static final HttpServer BOT = criarBotSimulado();

    @Autowired private ComunicacaoCancelamentoService cancelar;
    @Autowired private ConsultaComunicacaoCancelamentoService consulta;
    @Autowired private ProcessarComunicacaoCancelamentoService processamento;
    @Autowired private ComunicacaoCancelamentoTransacionalService transacional;
    @Autowired private ComunicacaoCancelamentoRepository comunicacoes;
    @Autowired private TentativaComunicacaoCancelamentoRepository tentativas;
    @Autowired private EventoRepository eventos;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void propriedadesIsoladas(DynamicPropertyRegistry props) {
        // URL fixa: nao depende do DB_URL ou do banco de desenvolvimento.
        props.add("spring.datasource.url", () -> "jdbc:postgresql://127.0.0.1:5432/" + BANCO_EXCLUSIVO);
        props.add("spring.datasource.username", () -> System.getenv().getOrDefault("CONCORRENCIA_DB_USERNAME", "postgres"));
        props.add("spring.datasource.password", () -> System.getenv().getOrDefault("CONCORRENCIA_DB_PASSWORD", ""));
        props.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        props.add("spring.jpa.show-sql", () -> "false");
        props.add("bot.url", () -> "http://127.0.0.1:" + BOT.getAddress().getPort());
        props.add("bot.secret", () -> SEGREDO_TESTE);
    }

    @BeforeEach
    void preparar() {
        assertEquals(BANCO_EXCLUSIVO, jdbcTemplate.queryForObject("select current_database()", String.class),
                "ABORTAR: somente o banco sice_concorrencia e permitido");
        LIBERAR_BOT.countDown();
        LIBERAR_BOT = new CountDownLatch(0);
        BOT_RECEBEU = new CountDownLatch(1);
        CHAMADAS_BOT.set(0);
        RESPOSTA_BOT.set(200);
        CORPO_RECEBIDO.set("");
    }

    @AfterEach
    void liberarSeFalhar() {
        LIBERAR_BOT.countDown();
    }

    @AfterAll
    static void encerrarBotSimulado() {
        LIBERAR_BOT.countDown();
        BOT.stop(0);
    }

    @Test
    void avisoConfirmadoGeraUmaComunicacaoEHistoricoSemAlterarOriginal() {
        Dados dados = criarDados(StatusEnvioAviso.ENVIADO, true);
        CancelamentoComAvisoResponseDTO resposta = cancelar.cancelarEComunicar(dados.eventoId());
        assertEquals(1, resposta.comunicacoesCriadas());
        assertEquals(0, resposta.gruposIgnoradosInativos());
        assertEquals(StatusEvento.CANCELADO, eventos.findById(dados.eventoId()).orElseThrow().getStatus());
        assertEquals(1, CHAMADAS_BOT.get());
        assertTrue(CORPO_RECEBIDO.get().contains("CANCELAMENTO"));

        ComunicacaoCancelamento c = unicaComunicacao(dados.eventoId());
        assertEquals(dados.envioId(), c.getEnvioOriginalId());
        assertEquals(StatusComunicacaoCancelamento.ENVIADO, c.getStatus());
        assertEquals(1, c.getTentativas());
        assertNotNull(c.getEnviadoEm());
        assertNull(c.getReservaToken());
        assertNull(c.getProcessandoDesde());
        assertEquals(ResultadoTentativaEnvioAviso.CONFIRMADO_PELO_BOT, unicaTentativa(c).getResultado());
        assertEquals(StatusEnvioAviso.ENVIADO, entityManagerQueryStatusOriginal(dados.envioId()));

        var consultaAdmin = consulta.listar(dados.eventoId());
        assertEquals(1, consultaAdmin.size());
        assertEquals(1, consultaAdmin.get(0).historico().size());
        assertEquals(StatusComunicacaoCancelamento.ENVIADO, consultaAdmin.get(0).status());

        assertThrows(RegraNegocioEventoException.class, () -> cancelar.cancelarEComunicar(dados.eventoId()));
        processamento.processarPorId(c.getId());
        assertEquals(1, CHAMADAS_BOT.get());
        assertEquals(1, tentativas.findByComunicacaoIdOrderByNumeroTentativaAsc(c.getId()).size());
    }

    @Test
    void semAvisoOriginalNenhumWhatsapp() {
        Dados dados = criarDados(null, true);
        var resposta = cancelar.cancelarEComunicar(dados.eventoId());
        assertEquals(0, resposta.comunicacoesCriadas());
        assertEquals(StatusEvento.CANCELADO, eventos.findById(dados.eventoId()).orElseThrow().getStatus());
        assertTrue(comunicacoes.findByEventoIdOrderByIdAsc(dados.eventoId()).isEmpty());
        assertEquals(0, CHAMADAS_BOT.get());
    }

    @Test
    void avisoOriginalPendenteNaoGeraComunicacao() {
        Dados dados = criarDados(StatusEnvioAviso.PENDENTE, true);
        var resposta = cancelar.cancelarEComunicar(dados.eventoId());
        assertEquals(0, resposta.comunicacoesCriadas());
        assertTrue(comunicacoes.findByEventoIdOrderByIdAsc(dados.eventoId()).isEmpty());
        assertEquals(0, CHAMADAS_BOT.get());
    }

    @Test
    void grupoInativoContadoMasNaoRecebeCancelamento() {
        Dados dados = criarDados(StatusEnvioAviso.ENVIADO, false);
        var resposta = cancelar.cancelarEComunicar(dados.eventoId());
        assertEquals(0, resposta.comunicacoesCriadas());
        assertEquals(1, resposta.gruposIgnoradosInativos());
        assertTrue(comunicacoes.findByEventoIdOrderByIdAsc(dados.eventoId()).isEmpty());
        assertEquals(0, CHAMADAS_BOT.get());
    }

    @Test
    void originalReservadoImpedeCancelarEMantemEventoAtivo() {
        Dados dados = criarDados(StatusEnvioAviso.PENDENTE, true);
        transacao().execute(status -> {
            EnvioAviso original = entityManager.find(EnvioAviso.class, dados.envioId());
            original.setProcessandoDesde(LocalDateTime.now(ZONA));
            original.setReservaToken(UUID.randomUUID().toString());
            return null;
        });
        assertThrows(RegraNegocioEventoException.class, () -> cancelar.cancelarEComunicar(dados.eventoId()));
        assertEquals(StatusEvento.ATIVO, eventos.findById(dados.eventoId()).orElseThrow().getStatus());
        assertEquals(0, CHAMADAS_BOT.get());
    }

    @Test
    void botResponde503RegistraResultadoIncertoESemRetry() {
        Dados dados = criarDados(StatusEnvioAviso.ENVIADO, true);
        RESPOSTA_BOT.set(503);
        var resposta = cancelar.cancelarEComunicar(dados.eventoId());
        assertEquals(1, resposta.comunicacoesCriadas());
        ComunicacaoCancelamento c = unicaComunicacao(dados.eventoId());
        assertEquals(StatusComunicacaoCancelamento.ERRO, c.getStatus());
        assertEquals(1, c.getTentativas());
        assertEquals(ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO, unicaTentativa(c).getResultado());
        assertNull(c.getProcessandoDesde());
        assertEquals(1, CHAMADAS_BOT.get());
        RESPOSTA_BOT.set(200);
        processamento.processarPorId(c.getId());
        processamento.despacharNaoIniciadas();
        assertEquals(1, CHAMADAS_BOT.get(), "Uma resposta HTTP incerta nao autoriza reenvio");
        assertEquals(StatusEvento.CANCELADO, eventos.findById(dados.eventoId()).orElseThrow().getStatus());
    }

    @Test
    void reservaExpiradaNuncaPublicaNovaMensagem() {
        Dados dados = criarDados(StatusEnvioAviso.ENVIADO, true);
        Long id = transacao().execute(status -> {
            Evento e = entityManager.find(Evento.class, dados.eventoId());
            GrupoWhatsapp g = entityManager.find(GrupoWhatsapp.class, dados.grupoId());
            e.setStatus(StatusEvento.CANCELADO);
            ComunicacaoCancelamento c = new ComunicacaoCancelamento(e, g, dados.envioId(),
                    "CANCELAMENTO ficticio para teste", LocalDateTime.now(ZONA));
            entityManager.persist(c);
            entityManager.flush();
            return c.getId();
        });
        ReservaComunicacaoCancelamento reserva = transacional.reservar(id);
        assertNotNull(reserva);
        assertEquals(0, CHAMADAS_BOT.get());
        transacional.marcarExpirada(id, LocalDateTime.now(ZONA).plusMinutes(1));
        ComunicacaoCancelamento atual = comunicacoes.findById(id).orElseThrow();
        assertEquals(StatusComunicacaoCancelamento.ERRO, atual.getStatus());
        assertEquals(ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO, unicaTentativa(atual).getResultado());
        processamento.processarPorId(id);
        assertEquals(0, CHAMADAS_BOT.get());
    }

    @Test
    void duasRequisicoesDeCancelamentoResultamEmUmaMensagem() throws Exception {
        Dados dados = criarDados(StatusEnvioAviso.ENVIADO, true);
        LIBERAR_BOT = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<CancelamentoComAvisoResponseDTO> primeira = executor.submit(
                    () -> cancelar.cancelarEComunicar(dados.eventoId()));
            assertTrue(BOT_RECEBEU.await(15, TimeUnit.SECONDS), "Primeira operacao nao chegou ao bot simulado");

            Future<?> segunda = executor.submit(() -> cancelar.cancelarEComunicar(dados.eventoId()));
            ExecutionException erro = assertThrows(ExecutionException.class, () -> segunda.get(10, TimeUnit.SECONDS));
            assertTrue(erro.getCause() instanceof RegraNegocioEventoException,
                    "A segunda requisicao deve encontrar o evento ja cancelado");
            assertEquals(1, CHAMADAS_BOT.get());
            LIBERAR_BOT.countDown();
            assertEquals(1, primeira.get(15, TimeUnit.SECONDS).comunicacoesCriadas());
            ComunicacaoCancelamento c = unicaComunicacao(dados.eventoId());
            assertEquals(StatusComunicacaoCancelamento.ENVIADO, c.getStatus());
            assertEquals(1, tentativas.findByComunicacaoIdOrderByNumeroTentativaAsc(c.getId()).size());
        } finally {
            LIBERAR_BOT.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    private Dados criarDados(StatusEnvioAviso statusOriginal, boolean grupoAtivo) {
        return transacao().execute(status -> {
            String unico = UUID.randomUUID().toString().replace("-", "");
            Usuario usuario = new Usuario("Responsavel ficticio", null, PerfilUsuario.ADMIN,
                    new Credencial("teste-cancel-" + unico, "hash-ficticio"));
            entityManager.persist(usuario);

            GrupoWhatsapp grupo = new GrupoWhatsapp("Grupo de integracao", "grupo-test-" + unico,
                    TipoGrupo.GERAL_PROFESSORES, null);
            grupo.setAtivo(grupoAtivo);
            entityManager.persist(grupo);

            Evento evento = new Evento("Reuniao simulada " + unico.substring(0, 6), "Evento descartavel",
                    LocalDate.now(ZONA).plusDays(2), LocalTime.of(18, 0), usuario);
            entityManager.persist(evento);
            Long envioId = null;
            if (statusOriginal != null) {
                EnvioAviso original = new EnvioAviso(evento, grupo);
                original.setStatus(statusOriginal);
                if (statusOriginal == StatusEnvioAviso.ENVIADO) {
                    original.setTentativas(1);
                    original.setEnviadoEm(LocalDateTime.now(ZONA));
                }
                entityManager.persist(original);
                envioId = original.getId();
            }
            entityManager.flush();
            return new Dados(evento.getId(), envioId, grupo.getId());
        });
    }

    private StatusEnvioAviso entityManagerQueryStatusOriginal(Long envioId) {
        return transacao().execute(status -> entityManager.find(EnvioAviso.class, envioId).getStatus());
    }

    private ComunicacaoCancelamento unicaComunicacao(Long eventoId) {
        List<ComunicacaoCancelamento> lista = comunicacoes.findByEventoIdOrderByIdAsc(eventoId);
        assertEquals(1, lista.size(), "Esperada uma comunicacao de cancelamento para este evento");
        return lista.get(0);
    }

    private TentativaComunicacaoCancelamento unicaTentativa(ComunicacaoCancelamento c) {
        List<TentativaComunicacaoCancelamento> lista = tentativas.findByComunicacaoIdOrderByNumeroTentativaAsc(c.getId());
        assertEquals(1, lista.size(), "Esperado um historico persistido por comunicacao");
        return lista.get(0);
    }

    private TransactionTemplate transacao() {
        return new TransactionTemplate(transactionManager);
    }

    private record Dados(Long eventoId, Long envioId, Long grupoId) {}

    private static HttpServer criarBotSimulado() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.createContext("/enviar-aviso", exchange -> {
                try {
                    if (!"POST".equals(exchange.getRequestMethod())
                            || !SEGREDO_TESTE.equals(exchange.getRequestHeaders().getFirst("X-Bot-Secret"))) {
                        exchange.sendResponseHeaders(403, -1);
                        return;
                    }
                    CORPO_RECEBIDO.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    CHAMADAS_BOT.incrementAndGet();
                    BOT_RECEBEU.countDown();
                    if (!LIBERAR_BOT.await(12, TimeUnit.SECONDS)) {
                        exchange.sendResponseHeaders(504, -1);
                        return;
                    }
                    int http = RESPOSTA_BOT.get();
                    byte[] body = (http == 200 ? "{\"ok\":true}" : "{\"erro\":\"simulado\"}")
                            .getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(http, body.length);
                    try (var saida = exchange.getResponseBody()) {
                        saida.write(body);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    exchange.close();
                }
            });
            servidor.start();
            return servidor;
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel iniciar o bot simulado", e);
        }
    }
}
