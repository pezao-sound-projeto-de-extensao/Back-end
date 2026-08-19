package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoRequest;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoResponse;
import sound.pezao.backend.dto.unidadesDTO.UnidadeRequest;
import sound.pezao.backend.dto.unidadesDTO.UnidadeResponse;
import sound.pezao.backend.facade.MovimentacaoFacade;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/movimentacoes")
@Tag(name = "Movimentações", description = "Registro e histórico de entradas e saídas de estoque")
public class MovimentacaoController {

    private final MovimentacaoFacade facade;

    public MovimentacaoController(MovimentacaoFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    @Operation(summary = "Lista o histórico completo de movimentações com filtros opcionais")
    @PreAuthorize("hasAuthority('REGISTRAR_ENTRADA_SAIDA')")
    public ResponseEntity<List<MovimentacaoResponse>> listar(
            @RequestParam(required = false) Integer itemId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim
    ) {
        return ResponseEntity.ok(facade.listar(itemId, tipo, usuarioId, dataInicio, dataFim));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma movimentação pelo ID")
    @PreAuthorize("hasAuthority('REGISTRAR_ENTRADA_SAIDA')")
    public ResponseEntity<MovimentacaoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(facade.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registra uma entrada ou saída de estoque")
    @PreAuthorize("hasAuthority('REGISTRAR_ENTRADA_SAIDA')")
    public ResponseEntity<MovimentacaoResponse> registrar(
            @RequestBody @Valid MovimentacaoRequest request
    ) {
        MovimentacaoResponse response = facade.registrar(request);

        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma movimentação e reverte o estoque automaticamente")
    @PreAuthorize("hasAuthority('REGISTRAR_ENTRADA_SAIDA')")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        facade.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
