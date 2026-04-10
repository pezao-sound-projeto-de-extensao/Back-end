package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import sound.pezao.backend.dto.cargoDTO.CargoRequest;
import sound.pezao.backend.dto.cargoDTO.CargoResponse;
import sound.pezao.backend.service.CargoService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cargos")
@Tag(name = "Cargos", description = "Gerenciamento de cargos e permissões")
public class CargoController {
  private final CargoService service;

  public CargoController(CargoService service) {
    this.service = service;
  }

  @GetMapping
  @Operation(summary = "Lista todos os cargos")
  public ResponseEntity<List<CargoResponse>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Busca um cargo pelo ID")
  public ResponseEntity<CargoResponse> findById(@PathVariable Integer id) {
    return ResponseEntity.ok(service.findById(id));
  }

  @PostMapping
  @Operation(summary = "Cadastra um novo cargo")
  public ResponseEntity<CargoResponse> create(
      @RequestBody @Valid CargoRequest request,
      UriComponentsBuilder uriBuilder
  ) {
    CargoResponse response = service.create(request);

    URI uri = uriBuilder
        .path("/api/v1/cargos/{id}")
        .buildAndExpand(response.id())
        .toUri();

    return ResponseEntity.created(uri).body(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualiza um cargo existente")
  public ResponseEntity<CargoResponse> update(
      @PathVariable Integer id,
      @RequestBody @Valid CargoRequest request
  ) {
    return ResponseEntity.ok(service.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remove um cargo")
  public ResponseEntity<Void> delete(@PathVariable Integer id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
