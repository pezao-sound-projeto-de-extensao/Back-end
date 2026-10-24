package sound.pezao.backend.dto.usuarioDTO;

import java.time.LocalDateTime;

public record UsuarioCadastroResponse(
    Integer id,
    String nome,
    String email,
    boolean conviteEnviado,
    LocalDateTime criadoEm
) { }
