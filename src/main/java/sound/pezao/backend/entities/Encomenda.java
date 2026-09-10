package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "encomendas")
public class Encomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orcamento_item_id", nullable = false)
    private OrcamentoItem orcamentoItem;

    /**
     * Produto do catálogo movimentado por esta encomenda. Nulo enquanto a
     * encomenda for de um produto que ainda não foi cadastrado — nesse caso o
     * produto precisa ser vinculado no momento do recebimento, senão não há
     * onde dar entrada no estoque.
     */
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEncomenda status;

    private LocalDateTime criadoEm;
    private LocalDateTime recebidaEm;
    private LocalDateTime concluidaEm;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = StatusEncomenda.PENDENTE;
        }
        this.criadoEm = LocalDateTime.now();
    }
}
