package sound.pezao.backend.dto.movimentacaoDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ItemRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class MovimentacaoMapper {
    private final ItemRepository itemRepository;

    public MovimentacaoMapper(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public MovimentacaoResponse toResponse(Movimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                new ItemResumoResponse(
                        movimentacao.getItem().getId(),
                        movimentacao.getItem().getNome()
                ),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getEstoqueAntes(),
                movimentacao.getEstoqueDepois(),
                movimentacao.getData(),
                movimentacao.getObservacao(),
                movimentacao.getCriadoEm()
        );
    }

    public List<MovimentacaoResponse> toResponseList(List<Movimentacao> movimentacoes) {
        return movimentacoes.stream()
                .map(this::toResponse)
                .toList();
    }

    public Movimentacao toEntity(MovimentacaoRequest request) {
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado: ", request.itemId()));

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setItem(item);
        movimentacao.setTipo(request.tipo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setData(request.data() != null ? request.data() : LocalDate.now());
        movimentacao.setObservacao(request.observacao());
        movimentacao.setCriadoEm(LocalDateTime.now());

        return movimentacao;
    }
}