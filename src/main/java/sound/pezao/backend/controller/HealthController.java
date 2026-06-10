package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Verifica a saúde da aplicação")
public class HealthController {

    @GetMapping
    @Operation(summary = "Informar a saúde da aplicação")
    public ResponseEntity<Void> health(){
        return ResponseEntity.ok().build();
    }
    
}
