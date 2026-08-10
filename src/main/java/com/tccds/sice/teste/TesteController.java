package com.tccds.sice.teste;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
public class TesteController {
    
    @GetMapping
    public String teste(){
        return "Autenticação funcionando";
    }

    @PreAuthorize("hasRole('SECRETARIA')")
    @GetMapping("/secretaria")
    public String secretaria(){
        return "Você é da secretaria";
    }

}
