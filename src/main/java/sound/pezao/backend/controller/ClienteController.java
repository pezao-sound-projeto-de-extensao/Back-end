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
import sound.pezao.backend.dto.clienteDTO.ClienteRequest;
import sound.pezao.backend.dto.clienteDTO.ClienteResponse;
import sound.pezao.backend.service.ClienteService;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Clientes usados nos orçamentos")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista clientes com busca opcional por nome ou telefone")
    public ResponseEntity<Page<ClienteResponse>> listar(
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(service.listar(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um cliente pelo ID")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um cliente")
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest request) {
        return ResponseEntity.status(201).body(service.cadastrar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um cliente")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid ClienteRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }
}
