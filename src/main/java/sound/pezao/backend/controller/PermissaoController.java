package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sound.pezao.backend.dto.permissaoDTO.PermissaoResponse;
import sound.pezao.backend.service.PermissaoService;

import java.util.List;

@RestController
@RequestMapping("/permissoes")
@Tag(name = "Permissões", description = "Gerenciamento de permissões")
public class PermissaoController {
  private final PermissaoService service;

  public PermissaoController(PermissaoService service) {
    this.service = service;
  }
  @GetMapping
  @Operation(summary = "Lista todos as permissões cadastradas")
  public ResponseEntity<List<PermissaoResponse>> findAll(){
    return ResponseEntity.ok(service.findAll());
  }
}
