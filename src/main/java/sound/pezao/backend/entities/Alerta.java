package sound.pezao.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "quantidade_atual")
    private Integer quantidadeAtual;

    @Column(name = "quantidade_minima")
    private Integer quantidadeMinima;

    @Column(name = "tipo_alerta")
    private String tipoAlerta;

    @Column(name = "data_ocorrencia")
    private LocalDateTime dataOcorrencia;


    public Integer getItemId() {
        return itemId;
    }

    public String getItemNome() {
        return itemNome;
    }

    public Integer getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public Integer getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public String getTipoAlerta() {
        return tipoAlerta;
    }

    public LocalDateTime getDataOcorrencia() {
        return dataOcorrencia;
    }
}