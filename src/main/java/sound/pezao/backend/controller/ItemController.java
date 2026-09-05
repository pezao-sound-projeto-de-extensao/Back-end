package sound.pezao.backend.controller;

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
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CADASTRAR_ITENS')")
    public ResponseEntity<ItemResponse> create(
            @RequestBody ItemRequest request
    ) {
        return ResponseEntity.status(201)
                .body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<ItemResponse>> findAll(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                service.findAll(ativo, search, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> findById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public ResponseEntity<ItemResponse> update(
            @PathVariable Integer id,
            @RequestBody ItemRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PostMapping("/{id}/imagem")
    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public ResponseEntity<ItemResponse> uploadImagem(
            @PathVariable Integer id,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        return ResponseEntity.status(201)
                .body(service.uploadImagem(id, arquivo));
    }

    @GetMapping("/{id}/imagem/download")
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
    public ResponseEntity<Void> deletarImagem(
            @PathVariable Integer id
    ) {
        service.deletarImagem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public ResponseEntity<Void> inativar(
            @PathVariable Integer id
    ) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public ResponseEntity<Void> reativar(
            @PathVariable Integer id
    ) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }
}