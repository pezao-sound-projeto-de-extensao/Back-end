package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.itemDTO.ItemRequest;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.service.ItemService;

@RestController
@RequestMapping("/itens")
@Tag(name = "Itens", description = "Gerenciamento de itens do estoque")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CADASTRAR_ITENS')")
    @Operation(summary = "Cadastra um novo item")
    public ResponseEntity<ItemResponse> create(
            @RequestBody @Valid ItemRequest request
    ) {
        ItemResponse response = service.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Lista todos os itens com filtros opcionais",
            description = "Filtra por situação (ativo), nome (search), categoria (categoriaId) e, "
                    + "com apenasAlerta=true, devolve somente os itens em alerta — estoque baixo ou zerado."
    )
    public ResponseEntity<Page<ItemResponse>> findAll(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Boolean apenasAlerta,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                service.findAll(ativo, search, categoriaId, apenasAlerta, pageable)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um item pelo ID")
    public ResponseEntity<ItemResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    @Operation(summary = "Atualiza um item existente")
    public ResponseEntity<ItemResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid ItemRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PostMapping("/{id}/imagem")
    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    @Operation(summary = "Faz upload de uma imagem para o item")
    public ResponseEntity<ItemResponse> uploadImagem(
            @PathVariable Integer id,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        return ResponseEntity.status(201)
                .body(service.uploadImagem(id, arquivo));
    }

    @GetMapping("/{id}/imagem/download")
    @Operation(summary = "Baixa o arquivo de uma imagem")
    public ResponseEntity<Resource> baixarImagem(
            @PathVariable Integer id
    ) {
        ItemResponse item = service.findById(id);

        if (item.imagem() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource arquivo = service.baixarImagem(id);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                item.imagem().mimeType()
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + item.imagem().nomeArquivo() + "\""
                )
                .body(arquivo);
    }

    @DeleteMapping("/{id}/imagem")
    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    @Operation(summary = "Remove uma imagem do item")
    public ResponseEntity<Void> deletarImagem(
            @PathVariable Integer id
    ) {
        service.deletarImagem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    @Operation(summary = "Inativa um item")
    public ResponseEntity<Void> inativar(@PathVariable Integer id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    @Operation(summary = "Reativa um item")
    public ResponseEntity<Void> reativar(@PathVariable Integer id) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }
}