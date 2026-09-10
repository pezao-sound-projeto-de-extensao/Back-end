package sound.pezao.backend.dto.usuarioDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou edição de um usuário")
public record UsuarioRequest (

        @Schema(description = "Nome do usuário", example = "João da Silva")
        @NotBlank String nome,

        @Schema(description = "E-mail de login", example = "joao@email.com")
        @NotBlank @Email String email,

        @Schema(description = "ID do cargo", example = "1")
        @Positive int cargo_id,

        @Schema(description = "Senha do usuário. Opcional: quando não informada no cadastro, o "
                + "usuário recebe a senha padrão e precisa trocá-la no primeiro acesso. Na edição, "
                + "só é alterada se vier preenchida.", example = "SenhaSegura123")
        @Size(min = 8, max = 20, message = "Senha deve ter entre 8 e 20 caracteres")
        String senha,

        @Schema(description = "Usuário ativo ou inativo. Quando omitido, o usuário é criado ativo.",
                example = "true")
        Boolean ativo
) {

    public boolean temSenha() {
        return senha != null && !senha.isBlank();
    }
}
