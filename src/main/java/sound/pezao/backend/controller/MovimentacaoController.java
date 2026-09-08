package sound.pezao.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoRequest;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoResponse;
import sound.pezao.backend.facade.MovimentacaoFacade;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/movimentacoes")
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
        return ResponseEntity.ok(
                facade.listar(
                        itemId,
                        tipo,
                        usuarioId,
                        dataInicio,
                        dataFim
                )
        );
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
        return ResponseEntity.status(201)
                .body(facade.registrar(request));
    }

    @GetMapping("/{id}/nota/download")
    public ResponseEntity<Resource> baixarNota(
            @PathVariable Integer id
    ) {
        MovimentacaoResponse movimentacao =
                facade.buscarPorId(id);

        if (movimentacao.nota() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource arquivo = facade.baixarNota(id);

        MediaType mediaType = resolverMediaType(
                movimentacao.nota().mimeType()
        );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                movimentacao.nota().nomeArquivo() +
                                "\""
                )
                .body(arquivo);
    }

    @DeleteMapping("/{id}/nota")
    public ResponseEntity<Void> deletarNota(
            @PathVariable Integer id
    ) {
        facade.deletarNota(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma movimentação e reverte o estoque automaticamente")
    @PreAuthorize("hasAuthority('REGISTRAR_ENTRADA_SAIDA')")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        facade.deletar(id);
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