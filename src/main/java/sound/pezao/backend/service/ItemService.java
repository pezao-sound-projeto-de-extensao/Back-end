package sound.pezao.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.itemDTO.ItemMapper;
import sound.pezao.backend.dto.itemDTO.ItemRequest;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.exception.EntityInativaException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.repository.ItemRepository;

@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public ItemResponse create(ItemRequest request) {
        if (repository.existsByNomeIgnoreCase((request.nome()))) {
            throw new EntityNomeJaExisteException("Item", request.nome());
        }
        Item item = ItemMapper.toEntity(request);
        return ItemMapper.toResponse(repository.save(item));
    }

    public Page<ItemResponse> findAll(Boolean ativo, String search, Pageable pageable) {
        return repository.findAllFiltered(ativo, search, pageable)
                .map(ItemMapper::toResponse);
    }

    public ItemResponse findById(Integer id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        return ItemMapper.toResponse(item);
    }

    public ItemResponse update(Integer id, ItemRequest request) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));

        if (!item.getAtivo()) {
            throw new EntityInativaException("Item", id);
        }

        if (!item.getNome().equalsIgnoreCase(request.nome())
                && repository.existsByNomeIgnoreCase(request.nome())) {
            throw new EntityNomeJaExisteException("Item", request.nome());
        }

        item.setNome(request.nome());
        item.setQuantidadeAtual(request.quantidadeAtual());
        item.setQuantidadeMinima(request.quantidadeMinima());
        item.setPrecoCusto(request.precoCusto());
        item.setPrecoVenda(request.precoVenda());

        return ItemMapper.toResponse(repository.save(item));
    }

    public void inativar(Integer id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        item.setAtivo(false);
        repository.save(item);
    }

    public void reativar(Integer id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        item.setAtivo(true);
        repository.save(item);
    }
}