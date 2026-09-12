package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "movimentacoes")
public class Movimentacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo;
    private Integer quantidade;
    private Integer estoqueAntes;
    private Integer estoqueDepois;
    private LocalDate data;
    private String observacao;
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @PrePersist
    public void setCriadoSalvo() {
        this.criadoEm = LocalDateTime.now();
    }
}