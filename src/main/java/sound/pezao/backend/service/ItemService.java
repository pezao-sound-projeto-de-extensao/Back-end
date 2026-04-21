package sound.pezao.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.itemDTO.ItemMapper;
import sound.pezao.backend.dto.itemDTO.ItemRequest;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.exception.EntityInativaException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.repository.CategoriaRepository;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.UnidadeRepository;

@Service
public class ItemService {

    private final ItemRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadeRepository unidadeRepository;

    public ItemService(ItemRepository repository,
                       CategoriaRepository categoriaRepository,
                       UnidadeRepository unidadeRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.unidadeRepository = unidadeRepository;
    }

    @PreAuthorize("hasAuthority('CADASTRAR_ITENS')")
    public ItemResponse create(ItemRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new EntityNomeJaExisteException("Item", request.nome());
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria", request.categoriaId()));

        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new EntityNotFoundException("Unidade", request.unidadeId()));

        Item item = ItemMapper.toEntity(request, categoria, unidade);
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

    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
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

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria", request.categoriaId()));

        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new EntityNotFoundException("Unidade", request.unidadeId()));

        item.setNome(request.nome());
        item.setCategoria(categoria);
        item.setUnidade(unidade);
        item.setQuantidadeAtual(request.quantidadeAtual());
        item.setQuantidadeMinima(request.quantidadeMinima());
        item.setPrecoCusto(request.precoCusto());
        item.setPrecoVenda(request.precoVenda());

        return ItemMapper.toResponse(repository.save(item));
    }

    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public void inativar(Integer id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        item.setAtivo(false);
        repository.save(item);
    }

    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public void reativar(Integer id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        item.setAtivo(true);
        repository.save(item);
    }
}