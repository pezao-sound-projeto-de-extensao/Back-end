package sound.pezao.backend.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Table(name = "vw_alertas_estoque")
@Immutable
public class Alerta {

    @Id
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "item_nome")
    private String itemNome;

    @Column(name = "categoria_nome")
    private String categoriaNome;

    @Column(name = "unidade_medida")
    private String unidadeMedida;

    @Column(name = "quantidade_atual")
    private Integer quantidadeAtual;

    @Column(name = "quantidade_minima")
    private Integer quantidadeMinima;

    @Column(name = "tipo_alerta")
    private String tipoAlerta;

    protected Alerta() {}

    public Integer getItemId() { return itemId; }

    public String getItemNome() { return itemNome; }

    public String getCategoriaNome() { return categoriaNome; }

    public String getUnidadeMedida() { return unidadeMedida; }

    public Integer getQuantidadeAtual() { return quantidadeAtual; }

    public Integer getQuantidadeMinima() { return quantidadeMinima; }

    public String getTipoAlerta() { return tipoAlerta; }
}