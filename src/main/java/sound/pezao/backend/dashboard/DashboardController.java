package sound.pezao.backend.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sound.pezao.backend.dashboard.dto.DashboardResponse;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Visão consolidada da tela inicial")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Retorna os indicadores do estoque e os produtos que precisam de atenção",
            description = "Os produtos zerados vêm primeiro na lista de atenção, seguidos pelos de "
                    + "estoque baixo, do menor saldo para o maior.")
    public ResponseEntity<DashboardResponse> carregar() {
        return ResponseEntity.ok(service.carregar());
    }
}
