package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Token do link de "esqueci minha senha". Segue o mesmo desenho do RefreshToken:
 * o usuário recebe "id.segredo" e o banco guarda apenas o hash do segredo.
 */
@Entity
@Table(
        name = "tokens_recuperacao_senha",
        indexes = @Index(name = "idx_token_recuperacao_usuario", columnList = "usuario_id")
)
@NoArgsConstructor
@Getter
@Setter
public class TokenRecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiraEm;

    @Column
    private Instant usadoEm;

    @Column(nullable = false)
    private Instant criadoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        this.criadoEm = Instant.now();
    }

    public boolean isValido() {
        return usadoEm == null && expiraEm.isAfter(Instant.now());
    }
}
