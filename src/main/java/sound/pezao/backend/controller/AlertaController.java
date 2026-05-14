package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sound.pezao.backend.dto.alertaDTO.AlertaResponse;
import sound.pezao.backend.service.AlertaService;

import java.util.List;

@RestController
@RequestMapping("/alertas")
public class AlertaController {
    private final AlertaService service;

    public AlertaController(AlertaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Retorna todos os Alertas")
    public ResponseEntity<List<AlertaResponse>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca alertas filtrados por tipo (zerado/estoque_baixo)")
    public ResponseEntity<List<AlertaResponse>> findByTipo(@RequestParam String tipo) {
        return ResponseEntity.ok(service.findByTipo(tipo));
    }
}
