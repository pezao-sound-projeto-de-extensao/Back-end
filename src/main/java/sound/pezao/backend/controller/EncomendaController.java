package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sound.pezao.backend.dto.encomendaDTO.EncomendaKpisResponse;
import sound.pezao.backend.dto.encomendaDTO.EncomendaReceberRequest;
import sound.pezao.backend.dto.encomendaDTO.EncomendaResponse;
import sound.pezao.backend.service.EncomendaService;

@RestController
@RequestMapping("/encomendas")
@Tag(name = "Encomendas", description = "Encomendas geradas pelo aceite de orçamentos")
public class EncomendaController {

    private final EncomendaService service;

    public EncomendaController(EncomendaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista encomendas com filtros opcionais",
            description = "O campo search aceita o nome do item ou o nome do cliente. "
                    + "O status aceita PENDENTE, RECEBIDA ou CONCLUIDA.")
    public ResponseEntity<Page<EncomendaResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(service.listar(search, status, pageable));
    }

    @GetMapping("/kpis")
    @Operation(summary = "Indicadores de pendentes, recebidas e concluídas no mês")
    public ResponseEntity<EncomendaKpisResponse> kpis() {
        return ResponseEntity.ok(service.kpis());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma encomenda pelo ID")
    public ResponseEntity<EncomendaResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PatchMapping("/{id}/receber")
    @Operation(summary = "Marca a encomenda como recebida e registra a entrada no estoque",
            description = "Quando a encomenda é de um produto que não estava cadastrado, informe "
                    + "no corpo o itemId do produto correspondente já cadastrado.")
    public ResponseEntity<EncomendaResponse> receber(
            @PathVariable Integer id,
            @RequestBody(required = false) EncomendaReceberRequest request
    ) {
        Integer itemId = request != null ? request.itemId() : null;
        return ResponseEntity.ok(service.receber(id, itemId));
    }

    @PatchMapping("/{id}/concluir")
    @Operation(summary = "Conclui a entrega ao cliente e registra a saída do estoque")
    public ResponseEntity<EncomendaResponse> concluir(@PathVariable Integer id) {
        return ResponseEntity.ok(service.concluir(id));
    }
}
