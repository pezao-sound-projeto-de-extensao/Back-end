package sound.pezao.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(description = "Retorna uma lista de todas as unidades de medida disponíveis.")
    public ResponseEntity<List<UnidadeResponse>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    public ResponseEntity<UnidadeResponse> findById(Integer id){
        return ResponseEntity.ok(service.findById(id));
    }
}
