package sound.pezao.backend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sound.pezao.backend.dto.usuarioDTO.UsuarioRequest;
import sound.pezao.backend.dto.usuarioDTO.UsuarioResponse;
import sound.pezao.backend.service.UsuarioService;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UsuarioController {

    final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Busca paginada de todos os usuários")
    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable
            ){
        System.out.println("oii");
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @Operation(summary = "Cadastro de novo usuário")
    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(
            @Valid @RequestBody UsuarioRequest usuarioRequest
            ){
        UsuarioResponse usuarioResponse = usuarioService.cadastrar(usuarioRequest);
        return ResponseEntity.status(201).body(usuarioResponse);
    }

    @Operation(summary = "Buscar informações do usuário baseado no ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> listar(@PathVariable int id){
        return ResponseEntity.ok(usuarioService.listar(id));
    }

    @Operation(summary = "Atualizar informações do usuário")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable int id,
            @Valid @RequestBody UsuarioRequest usuarioRequest
    ){
        return ResponseEntity.ok(usuarioService.atualizar(id, usuarioRequest));
    }

    @Operation(summary = "Ativar ou desativar o usuário")
    @PatchMapping("/ativo/{id}")
    public ResponseEntity<Void> ativar(@PathVariable int id){
        usuarioService.ativar(id);
        return ResponseEntity.noContent().build();
    }
}
