package sound.pezao.backend.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
@Converter
public class TipoMovimentacaoConverter
        implements AttributeConverter<TipoMovimentacao, String> {

    @Override
    public String convertToDatabaseColumn(TipoMovimentacao tipo) {
        return tipo != null ? tipo.getValor() : null;
    }

    @Override
    public TipoMovimentacao convertToEntityAttribute(String valor) {
        return valor != null ? TipoMovimentacao.fromValor(valor) : null;
    }
}
