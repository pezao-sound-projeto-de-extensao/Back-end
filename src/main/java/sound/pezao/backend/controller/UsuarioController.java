package sound.pezao.backend.controller;


import jakarta.validation.Valid;
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
public class UsuarioController {

    final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(
            @PageableDefault(sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
            ){
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(
            @Valid @RequestBody UsuarioRequest usuarioRequest
            ){
        UsuarioResponse usuarioResponse = usuarioService.cadastrar(usuarioRequest);
        return ResponseEntity.status(201).body(usuarioResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> listar(@PathVariable int id){
        return ResponseEntity.ok(usuarioService.listar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable int id,
            @Valid @RequestBody UsuarioRequest usuarioRequest
    ){
        return ResponseEntity.ok(usuarioService.atualizar(id, usuarioRequest));
    }
    
    @PatchMapping("/ativo/{id}")
    public ResponseEntity<Void> ativar(@PathVariable int id){
        usuarioService.ativar(id);
        return ResponseEntity.noContent().build();
    }
}
