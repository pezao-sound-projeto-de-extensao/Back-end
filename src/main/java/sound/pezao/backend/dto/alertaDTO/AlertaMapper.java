package sound.pezao.backend.dto.alertaDTO;

import sound.pezao.backend.entities.Alerta;

public class AlertaMapper {
    public static AlertaResponse toResponse(Alerta alerta){
        return new AlertaResponse(
                alerta.getItemId(),
                alerta.getItemNome(),
                alerta.getQuantidadeAtual(),
                alerta.getQuantidadeMinima(),
                alerta.getTipoAlerta()
        );
    }
}
