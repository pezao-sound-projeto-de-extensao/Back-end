package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orcamentos")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOrcamento status;

    @Column(length = 500)
    private String observacao;

    @Column(name = "valor_total")
    private Double valorTotal;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoItem> itens = new ArrayList<>();

    public void adicionarItem(OrcamentoItem item) {
        item.setOrcamento(this);
        this.itens.add(item);
    }

    public void limparItens() {
        this.itens.clear();
    }

    /**
     * O total do orçamento é sempre derivado dos subtotais dos itens, para não
     * existir a possibilidade de um total gravado divergir do que está na lista.
     */
    public void recalcularTotal() {
        this.valorTotal = itens.stream()
                .mapToDouble(OrcamentoItem::calcularSubtotal)
                .sum();
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = StatusOrcamento.PENDENTE;
        }
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
