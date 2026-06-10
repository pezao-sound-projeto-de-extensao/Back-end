package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/no-health")
@Tag(name = "Health", description = "Endpoint de teste")
public class NoHealthController {
    @GetMapping
    @Operation(summary = "Simula erro 500")
    public ResponseEntity<Void> noHealth(){
        return ResponseEntity.internalServerError().build();
    }
}
