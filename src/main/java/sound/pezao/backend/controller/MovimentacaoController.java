package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoRequest;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoResponse;
import sound.pezao.backend.facade.MovimentacaoFacade;

import java.time.LocalDate;

@RestController
@RequestMapping("/movimentacoes")
@Tag(name = "Movimentações", description = "Registro e histórico de entradas e saídas de estoque")
public class MovimentacaoController {

    private final MovimentacaoFacade facade;

    public MovimentacaoController(MovimentacaoFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de movimentações com filtros opcionais",
            description = "Filtra por produto (itemId), nome do produto (search), tipo, usuário e "
                    + "período (dataInicio e dataFim no formato aaaa-MM-dd). O resultado é paginado "
                    + "e ordenado da movimentação mais recente para a mais antiga.")
    public ResponseEntity<Page<MovimentacaoResponse>> listar(
            @RequestParam(required = false) Integer itemId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                facade.listar(itemId, tipo, usuarioId, search, dataInicio, dataFim, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma movimentação pelo ID")
    public ResponseEntity<MovimentacaoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(facade.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registra uma entrada ou saída de estoque")
    public ResponseEntity<MovimentacaoResponse> registrar(
            @RequestBody @Valid MovimentacaoRequest request
    ) {
        MovimentacaoResponse response = facade.registrar(request);

        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma movimentação e reverte o estoque automaticamente")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        facade.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
