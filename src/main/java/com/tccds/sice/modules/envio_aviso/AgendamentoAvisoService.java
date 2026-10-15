package com.tccds.sice.modules.envio_aviso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.EventoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgendamentoAvisoService {

    private static final ZoneId ZONA =
            ZoneId.of("America/Sao_Paulo");

    private final EventoRepository eventoRepository;
    private final EnvioAvisoRepository envioAvisoRepository;
    private final EnvioAvisoService envioAvisoService;

    @Scheduled(
            cron = "0 0 7 * * *",
            zone = "America/Sao_Paulo"
        //     fixedRate = 10000
    )
    public void processarAvisosDoDia() {

        LocalDate hoje =
                LocalDate.now(ZONA);

        List<Evento> eventos =
                eventoRepository
                        .findByStatusAndDataAvisoLessThanEqualAndDataInicioGreaterThanEqual(
                                StatusEvento.ATIVO,
                                hoje,
                                hoje
                        );

        log.debug("[SICE-AVISOS] Agendamento: hoje={}, eventos elegiveis={}", hoje, eventos.size());

        for (Evento evento : eventos) {

            envioAvisoService
                    .criarEnviosParaEvento(evento);

            envioAvisoService
                    .processarEnviosPendentesDoEvento(
                            evento.getId()
                    );
        }
    }

    @Scheduled(
            cron = "0 5 * * * *",
            zone = "America/Sao_Paulo"
        //     fixedRate = 10000
    )
    public void tentarNovamenteEnviosPendentes() {

        LocalDate hoje =
                LocalDate.now(ZONA);

        LocalDateTime limiteTentativa =
                LocalDateTime.now(ZONA)
                        .minusHours(1);

        List<Long> enviosIds =
                envioAvisoRepository
                        .buscarIdsPendentesParaRetry(
                                StatusEnvioAviso.PENDENTE,
                                StatusEvento.ATIVO,
                                hoje,
                                limiteTentativa
                        );

        log.debug("[SICE-AVISOS] Retry: envios elegiveis={}", enviosIds.size());

        for (Long envioId : enviosIds) {

            envioAvisoService
                    .processarEnvioPorId(envioId);
        }
    }
}