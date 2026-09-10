package sound.pezao.backend.dto.clienteDTO;

import sound.pezao.backend.entities.Cliente;

public class ClienteMapper {

    public static ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getCriadoEm()
        );
    }

    public static Cliente toEntity(ClienteRequest request) {
        return new Cliente(request.nome(), request.telefone());
    }
}
