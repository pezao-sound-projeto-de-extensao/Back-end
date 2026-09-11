package sound.pezao.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orcamento_itens")
public class OrcamentoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    /**
     * Nulo quando o produto ainda não existe no catálogo: o orçamento pode
     * incluir algo que será comprado do fornecedor sem estar cadastrado.
     */
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    /**
     * Nome do produto no momento do orçamento. Para item do catálogo é uma cópia
     * do nome, para que o orçamento não mude se o produto for renomeado depois.
     */
    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false)
    private Double precoUnitario;

    public boolean isProdutoNovo() {
        return item == null;
    }

    public double calcularSubtotal() {
        if (quantidade == null || precoUnitario == null) {
            return 0.0;
        }
        return quantidade * precoUnitario;
    }
}
