package sound.pezao.backend.dto.categoriaDTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
@Schema(description = "Dados enviados para criar ou atualizar uma categoria")
public class CategoriaRequest {
    @Schema(description = "Nome da categoria", example = "Som automotivo")
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    public CategoriaRequest(@NotBlank(message = "Nome é obrigatório") String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }    
}
