package sound.pezao.backend.controller;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sound.pezao.backend.dto.ususarioDTO.UsuarioRequest;
import sound.pezao.backend.dto.ususarioDTO.UsuarioResponse;
import sound.pezao.backend.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(){
        return ResponseEntity.ok(usuarioService.listar());
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
}
