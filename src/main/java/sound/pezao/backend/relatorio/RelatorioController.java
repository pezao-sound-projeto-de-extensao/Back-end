package sound.pezao.backend.relatorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sound.pezao.backend.relatorio.dto.RelatorioResponseDTO;

import java.time.LocalDate;

@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios", description = "Dados analíticos do estoque")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Busca todos os dados do relatório filtrados por período")
    public ResponseEntity<RelatorioResponseDTO> buscar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Integer categoriaId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.buscar(dataInicio, dataFim, categoriaId, pageable));
    }
}