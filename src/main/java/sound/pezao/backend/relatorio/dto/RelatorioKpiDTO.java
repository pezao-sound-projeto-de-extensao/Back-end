package sound.pezao.backend.relatorio.dto;

public record RelatorioKpiDTO (
    Long totalItens,
    Long itensOk,
    Long itensAlerta,
    Long itensZerados
) {
}
