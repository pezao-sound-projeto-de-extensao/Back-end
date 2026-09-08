package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoRequest;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoResponse;
import sound.pezao.backend.service.OrcamentoService;

@RestController
@RequestMapping("/orcamentos")
@Tag(name = "Orçamentos", description = "Orçamentos enviados aos clientes")
public class OrcamentoController {

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista orçamentos com filtros opcionais",
            description = "O campo search aceita o nome do cliente ou o número do orçamento. "
                    + "O status aceita PENDENTE, ACEITO, REJEITADO ou CONCLUIDO.")
    public ResponseEntity<Page<OrcamentoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(service.listar(search, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um orçamento pelo ID, com todos os itens")
    public ResponseEntity<OrcamentoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria um orçamento",
            description = "Aceita um cliente já cadastrado (clienteId) ou os dados de um cliente "
                    + "novo (clienteNovo), nunca os dois. Cada item aponta para um produto do "
                    + "catálogo (itemId) ou traz a descrição de um produto ainda não cadastrado.")
    public ResponseEntity<OrcamentoResponse> criar(@RequestBody @Valid OrcamentoRequest request) {
        return ResponseEntity.status(201).body(service.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita um orçamento pendente")
    public ResponseEntity<OrcamentoResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid OrcamentoRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/aceitar")
    @Operation(summary = "Aceita um orçamento pendente")
    public ResponseEntity<OrcamentoResponse> aceitar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.aceitar(id));
    }

    @PatchMapping("/{id}/rejeitar")
    @Operation(summary = "Rejeita um orçamento pendente")
    public ResponseEntity<OrcamentoResponse> rejeitar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.rejeitar(id));
    }
}
