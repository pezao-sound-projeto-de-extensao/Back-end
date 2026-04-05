package sound.pezao.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import sound.pezao.backend.dto.unidadesDTO.UnidadeRequest;
import sound.pezao.backend.dto.unidadesDTO.UnidadeResponse;
import sound.pezao.backend.service.UnidadeService;

@RestController
@RequestMapping("/unidades")
@Tag(name = "Unidades", description = "Gerenciamento de unidades de medida")
public class UnidadeController {
    private final UnidadeService service;

    public UnidadeController(UnidadeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Retorna uma lista de todas as unidades de medida disponíveis.")
    public ResponseEntity<List<UnidadeResponse>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    @Operation(summary = "Cria uma nova unidade de medida com os dados fornecidos.")
    public ResponseEntity<UnidadeResponse> create(@RequestBody UnidadeRequest request){
        return ResponseEntity.status(201).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os detalhes de uma unidade de medida existente com base no ID fornecido e nos dados atualizados.")
    public ResponseEntity<UnidadeResponse> update(@PathVariable Integer id, @RequestBody UnidadeRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma unidade de medida específica com base no ID fornecido.")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
