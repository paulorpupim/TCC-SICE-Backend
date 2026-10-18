package com.tccds.sice.modules.envio_aviso;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Fila exclusiva dos disparos imediatos apos o commit do evento.
 * Uma unica thread preserva a ordem dos envios e nao ocupa a requisicao HTTP.
 */
@Configuration
public class ExecutorEnviosImediatosConfig {

    @Bean(name = "executorEnviosImediatos")
    public ThreadPoolTaskExecutor executorEnviosImediatos() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("sice-aviso-imediato-");
        // Se a fila estiver cheia, a tarefa sera rejeitada e registrada no listener.
        // Os avisos permanecem no banco para o agendador processar depois.
        executor.initialize();
        return executor;
    }
}
