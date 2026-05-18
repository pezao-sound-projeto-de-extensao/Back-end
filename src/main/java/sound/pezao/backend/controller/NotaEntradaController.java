package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.arquivoDTO.ArquivoDownload;
import sound.pezao.backend.dto.notaEntradaDTO.NotaEntradaResponse;
import sound.pezao.backend.service.NotaEntradaService;

import java.util.List;

@RestController
@RequestMapping("/movimentacoes/{movimentacaoId}/notas")
@Tag(name = "Notas de Entrada", description = "Upload e gerenciamento de arquivos de movimentações")
public class NotaEntradaController {

    private final NotaEntradaService service;

    public NotaEntradaController(NotaEntradaService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Faz upload de um arquivo (imagem ou nota fiscal) para a movimentação")
    public ResponseEntity<NotaEntradaResponse> upload(
            @PathVariable Integer movimentacaoId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("tipo")
            @Parameter(schema = @Schema(allowableValues = {"imagem", "nota_fiscal"}))
            String tipo
    ) {
        return ResponseEntity.status(201).body(service.upload(movimentacaoId, arquivo, tipo));
    }

    @GetMapping
    @Operation(summary = "Lista os arquivos de uma movimentação")
    public ResponseEntity<List<NotaEntradaResponse>> listar(@PathVariable Integer movimentacaoId) {
        return ResponseEntity.ok(service.listar(movimentacaoId));
    }

    @GetMapping("/{notaId}/download")
    @Operation(summary = "Baixa o arquivo de uma nota")
    public ResponseEntity<Resource> download(
            @PathVariable Integer movimentacaoId,
            @PathVariable Integer notaId
    ) {
        ArquivoDownload arquivo = service.baixar(movimentacaoId, notaId);
        return ResponseEntity.ok()
                .contentType(resolverMediaType(arquivo.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + arquivo.nomeArquivo() + "\"")
                .body(arquivo.conteudo());
    }

    @DeleteMapping("/{notaId}")
    @Operation(summary = "Remove um arquivo da movimentação")
    public ResponseEntity<Void> deletar(
            @PathVariable Integer movimentacaoId,
            @PathVariable Integer notaId
    ) {
        service.deletar(movimentacaoId, notaId);
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
