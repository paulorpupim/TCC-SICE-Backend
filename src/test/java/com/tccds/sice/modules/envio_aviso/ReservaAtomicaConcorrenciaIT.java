package com.tccds.sice.modules.envio_aviso;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.credencial.Credencial;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAviso;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAvisoRepository;

/**
 * INTEGRACAO REAL: PostgreSQL + Spring + HTTP simulado, sem WhatsApp.
 *
 * NAO roda em 'mvn test' comum: sufixo IT; execute explicitamente -Dtest=ReservaAtomicaConcorrenciaIT.
 * Banco fixo e descartavel: sice_concorrencia. Nunca usar dados de producao.
 */
@SpringBootTest
@ActiveProfiles("concorrencia")
@Execution(ExecutionMode.SAME_THREAD)
class ReservaAtomicaConcorrenciaIT {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final String BANCO_EXCLUSIVO = "sice_concorrencia";
    private static final String SEGREDO_SIMULADO = "segredo-local-somente-teste";
    private static final AtomicInteger REQUISICOES_BOT = new AtomicInteger();
    private static volatile CountDownLatch BOT_RECEBEU = new CountDownLatch(1);
    private static volatile CountDownLatch BOT_LIBERADO = new CountDownLatch(0);
    private static volatile int STATUS_HTTP = 200;
    private static final HttpServer BOT_SIMULADO = iniciarBotSimulado();

    @Autowired private EnvioAvisoService envioService;
    @Autowired private ProcessamentoEnvioTransacionalService processamento;
    @Autowired private EnvioAvisoRepository envioRepository;
    @Autowired private TentativaEnvioAvisoRepository tentativaRepository;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configuracoesDeSeguranca(DynamicPropertyRegistry registro) {
        // Esta URL tem precedencia sobre application.properties e variaveis DB_URL.
        registro.add("spring.datasource.url", () -> "jdbc:postgresql://127.0.0.1:5432/" + BANCO_EXCLUSIVO);
        registro.add("spring.datasource.username",
                () -> System.getenv().getOrDefault("CONCORRENCIA_DB_USERNAME", "postgres"));
        registro.add("spring.datasource.password",
                () -> System.getenv().getOrDefault("CONCORRENCIA_DB_PASSWORD", ""));
        registro.add("bot.url", () -> "http://127.0.0.1:" + BOT_SIMULADO.getAddress().getPort());
        registro.add("bot.secret", () -> SEGREDO_SIMULADO);
        registro.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registro.add("spring.jpa.show-sql", () -> "false");
    }

    @BeforeEach
    void preparar() {
        String banco = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
        assertEquals(BANCO_EXCLUSIVO, banco, "ABORTAR: teste exige banco exclusivo sice_concorrencia");
        BOT_LIBERADO.countDown();
        BOT_LIBERADO = new CountDownLatch(0);
        BOT_RECEBEU = new CountDownLatch(1);
        REQUISICOES_BOT.set(0);
        STATUS_HTTP = 200;
    }

    @AfterEach
    void liberarBotMesmoSeOTesteFalhar() {
        BOT_LIBERADO.countDown();
    }

    @AfterAll
    static void fecharServidor() {
        BOT_LIBERADO.countDown();
        BOT_SIMULADO.stop(0);
    }

    @Test
    void duasExecucoesEnquantoBotEsperaGeramUmaUnicaRequisicaoEUmaTentativa() throws Exception {
        Long id = criarEnvioFicticio();
        BOT_LIBERADO = new CountDownLatch(1); // resposta HTTP fica retida
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> primeira = executor.submit(() -> envioService.processarEnvioPorId(id));
            assertTrue(BOT_RECEBEU.await(10, TimeUnit.SECONDS),
                    "A primeira execucao nao chegou ao bot simulado");

            // A primeira execucao JA confirmou a reserva no banco e esta esperando o HTTP.
            Future<?> segunda = executor.submit(() -> envioService.processarEnvioPorId(id));
            segunda.get(8, TimeUnit.SECONDS); // segunda deve desistir SEM chamar o bot
            assertEquals(1, REQUISICOES_BOT.get(), "Duas chamadas ao bot durante a mesma reserva");

            BOT_LIBERADO.countDown();
            primeira.get(10, TimeUnit.SECONDS);

            conferirEnvio(id, StatusEnvioAviso.ENVIADO, 1, 1,
                    ResultadoTentativaEnvioAviso.CONFIRMADO_PELO_BOT);
            assertEquals(1, REQUISICOES_BOT.get());
        } finally {
            BOT_LIBERADO.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void bloqueioPessimistaNoPostgresEsperaCommitENaoReservaOutraVez() throws Exception {
        Long id = criarEnvioFicticio();
        String tokenManual = UUID.randomUUID().toString();
        CountDownLatch linhaBloqueada = new CountDownLatch(1);
        CountDownLatch liberarTransacao = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // Uma conexao segura a linha com SELECT FOR UPDATE e confirma a reserva apos ser liberada.
            Future<?> primeiraTransacao = executor.submit(() -> transacao().execute(status -> {
                EnvioAviso envio = envioRepository.buscarParaReserva(id).orElseThrow();
                envio.setReservaToken(tokenManual);
                envio.setProcessandoDesde(LocalDateTime.now(ZONA));
                linhaBloqueada.countDown();
                try {
                    if (!liberarTransacao.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Tempo esgotado esperando liberar bloqueio");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
                return null;
            }));
            assertTrue(linhaBloqueada.await(10, TimeUnit.SECONDS));

            CountDownLatch segundaIniciada = new CountDownLatch(1);
            Future<ReservaEnvioAviso> segunda = executor.submit(() -> {
                segundaIniciada.countDown();
                return processamento.reservar(id);
            });
            assertTrue(segundaIniciada.await(5, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> segunda.get(350, TimeUnit.MILLISECONDS),
                    "A segunda consulta nao aguardou o bloqueio da primeira transacao");

            liberarTransacao.countDown();
            primeiraTransacao.get(10, TimeUnit.SECONDS);
            assertNull(segunda.get(10, TimeUnit.SECONDS),
                    "Depois do commit, a segunda reserva deve reconhecer a reserva existente");
            assertEquals(0, REQUISICOES_BOT.get());
            EnvioAviso atual = envioRepository.findById(id).orElseThrow();
            assertEquals(tokenManual, atual.getReservaToken());
            assertEquals(0, atual.getTentativas()); // bloqueio manual apenas demonstrou isolamento
        } finally {
            liberarTransacao.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void repetirUmEnvioJaConfirmadoNaoChamaOBotNovamente() {
        Long id = criarEnvioFicticio();
        envioService.processarEnvioPorId(id);
        envioService.processarEnvioPorId(id);
        conferirEnvio(id, StatusEnvioAviso.ENVIADO, 1, 1,
                ResultadoTentativaEnvioAviso.CONFIRMADO_PELO_BOT);
        assertEquals(1, REQUISICOES_BOT.get());
    }

    @Test
    void erroHttpRegistraTentativaENaoRepeteAntesDeUmaHora() {
        Long id = criarEnvioFicticio();
        STATUS_HTTP = 503;
        envioService.processarEnvioPorId(id);
        envioService.processarEnvioPorId(id);
        conferirEnvio(id, StatusEnvioAviso.PENDENTE, 1, 1,
                ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO);
        assertEquals(1, REQUISICOES_BOT.get());
    }

    @Test
    void reservaAbandonadaNaoDisparaBotECriaHistoricoIncerto() {
        Long id = criarEnvioFicticio();
        ReservaEnvioAviso reserva = processamento.reservar(id);
        assertNotNull(reserva);
        envioService.processarEnvioPorId(id); // com reserva ativa nao deve enviar
        assertEquals(0, REQUISICOES_BOT.get());

        // Forca criterio de expiracao sem esperar 15 minutos reais.
        processamento.interromperReservaExpirada(id, LocalDateTime.now(ZONA).plusMinutes(1));
        conferirEnvio(id, StatusEnvioAviso.ERRO, 1, 1,
                ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO);
        envioService.processarEnvioPorId(id);
        assertEquals(0, REQUISICOES_BOT.get());
    }

    private Long criarEnvioFicticio() {
        return transacao().execute(estado -> {
            String unico = UUID.randomUUID().toString().replace("-", "");
            Credencial credencial = new Credencial("teste-" + unico, "senha-ficticia");
            Usuario usuario = new Usuario("Usuario teste", null, PerfilUsuario.ADMIN, credencial);
            entityManager.persist(usuario);

            // Grupo sem identificador de WhatsApp real: bot HTTP simulado nao usa a API do WhatsApp.
            GrupoWhatsapp grupo = new GrupoWhatsapp("Grupo ficticio", "grupo-ficticio-" + unico,
                    TipoGrupo.GERAL_PROFESSORES, null);
            entityManager.persist(grupo);
            Evento evento = new Evento("Evento concorrencia", "Apenas teste local",
                    LocalDate.now(ZONA).plusDays(2), LocalTime.of(14, 0), usuario);
            entityManager.persist(evento);
            EnvioAviso envio = new EnvioAviso(evento, grupo);
            entityManager.persist(envio);
            entityManager.flush();
            return envio.getId();
        });
    }

    private TransactionTemplate transacao() {
        return new TransactionTemplate(transactionManager);
    }

    private void conferirEnvio(Long id, StatusEnvioAviso statusEsperado, int tentativasEsperadas,
                              int historicosEsperados, ResultadoTentativaEnvioAviso resultadoEsperado) {
        EnvioAviso envio = envioRepository.findById(id).orElseThrow();
        assertEquals(statusEsperado, envio.getStatus());
        assertEquals(tentativasEsperadas, envio.getTentativas());
        assertNull(envio.getProcessandoDesde(), "Reserva deveria ser liberada");
        assertNull(envio.getReservaToken(), "Token deveria ser limpo");
        List<TentativaEnvioAviso> historico = tentativaRepository
                .findByEnvioAvisoIdOrderByNumeroTentativaAsc(id);
        assertEquals(historicosEsperados, historico.size());
        assertEquals(resultadoEsperado, historico.get(0).getResultado());
    }

    private static HttpServer iniciarBotSimulado() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.createContext("/enviar-aviso", exchange -> {
                try {
                    if (!"POST".equals(exchange.getRequestMethod())
                            || !SEGREDO_SIMULADO.equals(exchange.getRequestHeaders().getFirst("X-Bot-Secret"))) {
                        exchange.sendResponseHeaders(403, -1);
                        return;
                    }
                    exchange.getRequestBody().readAllBytes();
                    REQUISICOES_BOT.incrementAndGet();
                    BOT_RECEBEU.countDown();
                    if (!BOT_LIBERADO.await(12, TimeUnit.SECONDS)) {
                        exchange.sendResponseHeaders(504, -1);
                        return;
                    }
                    int status = STATUS_HTTP;
                    byte[] corpo = (status == 200 ? "{\"ok\":true}" : "{\"error\":\"simulado\"}")
                            .getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(status, corpo.length);
                    try (var saida = exchange.getResponseBody()) {
                        saida.write(corpo);
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
            throw new IllegalStateException("Nao foi possivel iniciar o bot HTTP simulado local", e);
        }
    }
}
