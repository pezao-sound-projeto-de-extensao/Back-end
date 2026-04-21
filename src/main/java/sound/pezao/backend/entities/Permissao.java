package sound.pezao.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "permissoes")
public class Permissao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String nome;
  private String descricao;

  @ManyToMany(mappedBy = "permissoes")
  @JsonIgnore
  private Set<Cargo> cargos;

  public Permissao(){}

  public Permissao(Integer id, String nome, String descricao) {
    this.id = id;
    this.nome = nome;
    this.descricao = descricao;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public Set<Cargo> getCargos() {
    return cargos;
  }

  public void setCargos(Set<Cargo> cargos) {
    this.cargos = cargos;
  }
}
