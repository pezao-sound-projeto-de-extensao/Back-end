package sound.pezao.backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import sound.pezao.backend.dto.categoriaDTO.CategoriaRequest;
import sound.pezao.backend.dto.categoriaDTO.CategoriaResponse;
import sound.pezao.backend.service.CategoriaService;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Gerenciamento de categorias de itens")
public class CategoriaController {
    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todas as categorias")
    public ResponseEntity<List<CategoriaResponse>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    @Operation(summary = "Cria uma nova categoria")
    public ResponseEntity<CategoriaResponse> create(@RequestBody @Valid CategoriaRequest request){
        return ResponseEntity.status(201).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma categoria por id")
    public ResponseEntity<CategoriaResponse> update (@PathVariable Integer id,@RequestBody @Valid CategoriaRequest request){
        return ResponseEntity.ok(service.update(request, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma categoria por id")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
