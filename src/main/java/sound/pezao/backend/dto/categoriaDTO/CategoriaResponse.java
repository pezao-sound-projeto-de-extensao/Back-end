package sound.pezao.backend.dto.categoriaDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados retornados de uma categoria")
public class CategoriaResponse {
    @Schema(description = "Nome da categoria", example = "Som automotivo")
    private String nome;

    public CategoriaResponse(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}