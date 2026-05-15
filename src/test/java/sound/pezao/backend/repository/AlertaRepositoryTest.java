package sound.pezao.backend.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import sound.pezao.backend.entities.Alerta;

import java.util.List;

@DataJpaTest
@DisplayName("Testes de negócio para AlertaRepository")
class AlertaRepositoryTest {

    @Autowired
    private AlertaRepository alertaRepository;

    private Alerta persistirAlerta(Integer id, String nome, String tipo) {
        Alerta alerta = new Alerta(id, nome, "Cat", "UN", 1, 5, tipo);
        return alertaRepository.save(alerta);
    }

    @Nested
    @DisplayName("Busca alertas por tipo")
    class FindAllByTipoTest {

        @Test
        @DisplayName("Deve retornar lista de alertas quando o tipo existir")
        void findAllByTipoDeveRetornarListaDeAlertasQuandoOTipoExistir() {
            persistirAlerta(1, "Módulo 400W", "estoque_baixo");

            List<Alerta> resultado = alertaRepository.findAllByTipoAlerta("estoque_baixo");

            Assertions.assertFalse(resultado.isEmpty());
            Assertions.assertEquals(1, resultado.size());
        }
    }
}