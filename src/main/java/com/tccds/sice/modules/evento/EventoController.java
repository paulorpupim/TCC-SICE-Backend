package com.tccds.sice.modules.evento;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {
    
    private final EventoRepository eventoRepository;

    

}
