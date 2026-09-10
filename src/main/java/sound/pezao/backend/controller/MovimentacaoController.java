package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoRequest;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoResponse;
import sound.pezao.backend.facade.MovimentacaoFacade;

import java.time.LocalDate;

@RestController
@RequestMapping("/movimentacoes")
@Tag(name = "Movimentações", description = "Registro e histórico de entradas e saídas de estoque")
public class MovimentacaoController {

    /**
     * Quem movimenta estoque tem entrada, saída ou as duas. A checagem fina de
     * qual das duas é exigida fica no facade, que só conhece o tipo em tempo de
     * execução; aqui basta barrar quem não movimenta estoque de forma alguma.
     */
    private static final String PODE_MOVIMENTAR =
            "hasAnyAuthority('REGISTRAR_ENTRADA', 'REGISTRAR_SAIDA')";

    private final MovimentacaoFacade facade;

    public MovimentacaoController(MovimentacaoFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @Operation(summary = "Registra uma entrada ou saída de estoque")
    @PreAuthorize(PODE_MOVIMENTAR)
    public ResponseEntity<MovimentacaoResponse> registrar(
            @RequestBody @Valid MovimentacaoRequest request
    ) {
        return ResponseEntity.status(201)
                .body(facade.registrar(request));
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de movimentações com filtros opcionais",
            description = "Filtra por produto (itemId), nome do produto (search), tipo, usuário e "
                    + "período (dataInicio e dataFim no formato aaaa-MM-dd). O resultado é paginado "
                    + "e ordenado da movimentação mais recente para a mais antiga.")
    @PreAuthorize(PODE_MOVIMENTAR)
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
    @PreAuthorize(PODE_MOVIMENTAR)
    public ResponseEntity<MovimentacaoResponse> buscarPorId(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                facade.buscarPorId(id)
        );
    }

    @PostMapping(
            value = "/{id}/nota",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Anexa a nota fiscal da movimentação")
    @PreAuthorize(PODE_MOVIMENTAR)
    public ResponseEntity<MovimentacaoResponse> uploadNota(
            @PathVariable Integer id,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        return ResponseEntity.status(201)
                .body(facade.uploadNota(id, arquivo));
    }

    @GetMapping("/{id}/nota/download")
    @Operation(summary = "Baixa a nota fiscal anexada à movimentação")
    @PreAuthorize(PODE_MOVIMENTAR)
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
    @Operation(summary = "Remove a nota fiscal da movimentação")
    @PreAuthorize(PODE_MOVIMENTAR)
    public ResponseEntity<Void> deletarNota(
            @PathVariable Integer id
    ) {
        facade.deletarNota(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma movimentação e reverte o estoque automaticamente")
    @PreAuthorize(PODE_MOVIMENTAR)
    public ResponseEntity<Void> deletar(
            @PathVariable Integer id
    ) {
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
