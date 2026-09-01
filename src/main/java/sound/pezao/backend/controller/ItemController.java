package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import sound.pezao.backend.dto.itemDTO.ItemRequest;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.dto.unidadesDTO.UnidadeRequest;
import sound.pezao.backend.dto.unidadesDTO.UnidadeResponse;
import sound.pezao.backend.service.ItemService;

import java.net.URI;

@RestController
@RequestMapping("/itens")
@Tag(name = "Itens", description = "Gerenciamento de itens do estoque")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo item")
    public ResponseEntity<ItemResponse> create(
            @RequestBody @Valid ItemRequest request
    ) {
        ItemResponse response = service.create(request);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista todos os itens com filtros opcionais",
            description = "Filtra por situação (ativo), nome (search), categoria (categoriaId) e, "
                    + "com apenasAlerta=true, devolve somente os itens em alerta — estoque baixo ou zerado.")
    public ResponseEntity<Page<ItemResponse>> findAll(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Boolean apenasAlerta,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(ativo, search, categoriaId, apenasAlerta, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um item pelo ID")
    public ResponseEntity<ItemResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um item existente")
    public ResponseEntity<ItemResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid ItemRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/inativar")
    @Operation(summary = "Inativa um item")
    public ResponseEntity<Void> inativar(@PathVariable Integer id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativa um item")
    public ResponseEntity<Void> reativar(@PathVariable Integer id) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }
}