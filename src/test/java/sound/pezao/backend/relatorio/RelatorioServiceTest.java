package sound.pezao.backend.relatorio;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import sound.pezao.backend.relatorio.dto.ItemCriticoDTO;
import sound.pezao.backend.relatorio.dto.ItemMaisMovimentadoDTO;
import sound.pezao.backend.relatorio.dto.MovimentacaoHistoricoDTO;
import sound.pezao.backend.relatorio.dto.RelatorioKpiDTO;
import sound.pezao.backend.relatorio.dto.RelatorioResponseDTO;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.MovimentacaoRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de negócio para RelatorioService")
class RelatorioServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @InjectMocks
    private RelatorioService service;

    private final LocalDate inicio = LocalDate.of(2026, 9, 1);
    private final LocalDate fim = LocalDate.of(2026, 9, 30);
    private final Pageable pageable = PageRequest.of(0, 20);

    @Test
    @DisplayName("Deve reunir KPIs, itens críticos, mais movimentados e histórico")
    void deveReunirTodasAsSecoes() {
        when(itemRepository.buscarKpis()).thenReturn(new RelatorioKpiDTO(42L, 30L, 9L, 3L));
        when(itemRepository.buscarItensCriticos())
                .thenReturn(List.of(new ItemCriticoDTO("Bateria 60Ah", 1, 3, "BAIXO")));
        when(movimentacaoRepository.buscarMaisMovimentados(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new ItemMaisMovimentadoDTO("Cabo RCA 5m", 12L))));
        when(movimentacaoRepository.buscarHistorico(inicio, fim, null, pageable))
                .thenReturn(new PageImpl<>(List.of(new MovimentacaoHistoricoDTO(
                        inicio, "Cabo RCA 5m", "Cabos e Conectores", "entrada", 5, 10, 15, "NF 1042"))));

        RelatorioResponseDTO resposta = service.buscar(inicio, fim, null, pageable);

        assertEquals(42L, resposta.kpis().totalItens());
        assertEquals(1, resposta.itensCriticos().size());
        assertEquals(1, resposta.maisMovimentados().size());
        assertEquals(1, resposta.historico().getTotalElements());
    }

    @Test
    @DisplayName("Deve repassar o filtro de categoria para as consultas do período")
    void deveRepassarCategoria() {
        when(movimentacaoRepository.buscarMaisMovimentados(any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(movimentacaoRepository.buscarHistorico(inicio, fim, 2, pageable)).thenReturn(Page.empty());

        service.buscar(inicio, fim, 2, pageable);

        verify(movimentacaoRepository).buscarHistorico(inicio, fim, 2, pageable);
    }
}

@SpringJUnitConfig(RelatorioServiceSecurityTest.TestConfig.class)
@DisplayName("Testes de segurança da RelatorioService")
class RelatorioServiceSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        RelatorioService relatorioService(ItemRepository itemRepository,
                                         MovimentacaoRepository movimentacaoRepository) {
            return new RelatorioService(itemRepository, movimentacaoRepository);
        }
    }

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private MovimentacaoRepository movimentacaoRepository;

    @Resource
    private RelatorioService relatorioService;

    private final Pageable pageable = PageRequest.of(0, 20);

    @Nested
    @DisplayName("Acesso ao método")
    class AcessoTest {

        @Test
        @WithMockUser(authorities = "VER_RELATORIOS")
        @DisplayName("Deve permitir quando tem a permissão")
        void devePermitirQuandoTemPermissao() {
            when(movimentacaoRepository.buscarMaisMovimentados(any(), any(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());
            when(movimentacaoRepository.buscarHistorico(any(), any(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            assertDoesNotThrow(() -> relatorioService.buscar(
                    LocalDate.now().minusDays(30), LocalDate.now(), null, pageable));
        }

        @Test
        @WithMockUser(authorities = "OUTRA_PERMISSAO")
        @DisplayName("Deve negar quando não tem a permissão")
        void deveNegarQuandoNaoTemPermissao() {
            assertThrows(AccessDeniedException.class, () -> relatorioService.buscar(
                    LocalDate.now().minusDays(30), LocalDate.now(), null, pageable));

            verify(itemRepository, never()).buscarKpis();
        }

        @Test
        @WithAnonymousUser
        @DisplayName("Deve negar quando anônimo")
        void deveNegarQuandoAnonimo() {
            assertThrows(AccessDeniedException.class, () -> relatorioService.buscar(
                    LocalDate.now().minusDays(30), LocalDate.now(), null, pageable));

            verify(itemRepository, never()).buscarKpis();
        }
    }
}
