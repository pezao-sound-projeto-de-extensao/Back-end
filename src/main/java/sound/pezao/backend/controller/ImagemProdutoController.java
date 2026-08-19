package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.arquivoDTO.ArquivoDownload;
import sound.pezao.backend.dto.imagemProdutoDTO.ImagemProdutoResponse;
import sound.pezao.backend.service.ImagemProdutoService;

import java.util.List;

@RestController
@RequestMapping("/itens/{itemId}/imagens")
@Tag(name = "Imagens de Produto", description = "Upload e gerenciamento de imagens dos itens")
public class ImagemProdutoController {

    private final ImagemProdutoService service;

    public ImagemProdutoController(ImagemProdutoService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Faz upload de uma imagem para o item")
    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public ResponseEntity<ImagemProdutoResponse> upload(
            @PathVariable Integer itemId,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        return ResponseEntity.status(201).body(service.upload(itemId, arquivo));
    }

    @GetMapping
    @Operation(summary = "Lista as imagens de um item")
    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public ResponseEntity<List<ImagemProdutoResponse>> listar(@PathVariable Integer itemId) {
        return ResponseEntity.ok(service.listar(itemId));
    }

    @GetMapping("/{imagemId}/download")
    @Operation(summary = "Baixa o arquivo de uma imagem")
    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public ResponseEntity<Resource> download(
            @PathVariable Integer itemId,
            @PathVariable Integer imagemId
    ) {
        ArquivoDownload arquivo = service.baixar(itemId, imagemId);
        return ResponseEntity.ok()
                .contentType(resolverMediaType(arquivo.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + arquivo.nomeArquivo() + "\"")
                .body(arquivo.conteudo());
    }

    @DeleteMapping("/{imagemId}")
    @Operation(summary = "Remove uma imagem do item")
    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public ResponseEntity<Void> deletar(
            @PathVariable Integer itemId,
            @PathVariable Integer imagemId
    ) {
        service.deletar(itemId, imagemId);
        return ResponseEntity.noContent().build();
    }

    private MediaType resolverMediaType(String mimeType) {
        try {
            return MediaType.parseMediaType(mimeType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
