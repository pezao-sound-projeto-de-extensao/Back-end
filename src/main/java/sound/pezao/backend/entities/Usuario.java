package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@DynamicUpdate
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nome;
    private String email;
    private String senhaHash;
    private boolean ativo;
    private LocalDateTime ultimoAcesso;
    private LocalDateTime criadoEm;
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @PrePersist
    public void prePersist () {
        this.criadoEm = LocalDateTime.now();
        this.ativo = true;
    }
}
