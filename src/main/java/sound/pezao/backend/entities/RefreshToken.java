package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table (
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_usuario", columnList = "usuario_id"),
                @Index(name = "idx_refresh_token_expira_em", columnList = "expira_em")
        }
)
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String tokenHash;
    @Column(nullable = false)
    private Instant expiraEm;
    @Column
    private Instant revogadoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public boolean isAtvio(){
        return revogadoEm == null && expiraEm.isAfter(Instant.now());
    }
}
