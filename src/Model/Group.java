package Model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Group {
    private String nome;
    private String criador;
    private Set<String> membros;
    private Set<String> pendentesConvite; // Usuários que receberam convite mas ainda não responderam
    private Set<String> pendentesSolicitacao; // Usuários que solicitaram entrada
    private LocalDateTime dataCriacao;
    private ConcurrentHashMap<String, Set<String>> votosAprovacao; // Para solicitações de entrada

    public Group(String nome, String criador) {
        this.nome = nome;
        this.criador = criador;
        this.membros = new HashSet<>();
        this.pendentesConvite = new HashSet<>();
        this.pendentesSolicitacao = new HashSet<>();
        this.dataCriacao = LocalDateTime.now();
        this.votosAprovacao = new ConcurrentHashMap<>();

        // Criador é automaticamente membro
        this.membros.add(criador);
    }

    public boolean adicionarMembro(String usuario) {
        return membros.add(usuario);
    }

    public boolean removerMembro(String usuario) {
        // Não pode remover o criador
        if (usuario.equals(criador)) {
            return false;
        }
        return membros.remove(usuario);
    }

    public boolean isMembro(String usuario) {
        return membros.contains(usuario);
    }

    public void adicionarConvitePendente(String usuario) {
        pendentesConvite.add(usuario);
    }

    public void removerConvitePendente(String usuario) {
        pendentesConvite.remove(usuario);
    }

    public boolean temConvitePendente(String usuario) {
        return pendentesConvite.contains(usuario);
    }

    public void adicionarSolicitacaoPendente(String usuario) {
        pendentesSolicitacao.add(usuario);
        // Inicializa os votos para este usuário
        votosAprovacao.put(usuario, new HashSet<>());
    }

    public void removerSolicitacaoPendente(String usuario) {
        pendentesSolicitacao.remove(usuario);
        votosAprovacao.remove(usuario);
    }

    public boolean temSolicitacaoPendente(String usuario) {
        return pendentesSolicitacao.contains(usuario);
    }

    public void votar(String solicitante, String votante) {
        if (votosAprovacao.containsKey(solicitante)) {
            votosAprovacao.get(solicitante).add(votante);
        }
    }

    public boolean aprovadoPorTodos(String solicitante) {
        if (!votosAprovacao.containsKey(solicitante)) {
            return false;
        }
        return votosAprovacao.get(solicitante).size() == membros.size();
    }

    public int getNumeroVotos(String solicitante) {
        if (!votosAprovacao.containsKey(solicitante)) {
            return 0;
        }
        return votosAprovacao.get(solicitante).size();
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCriador() {
        return criador;
    }

    public Set<String> getMembros() {
        return new HashSet<>(membros);
    }

    public Set<String> getPendentesConvite() {
        return new HashSet<>(pendentesConvite);
    }

    public Set<String> getPendentesSolicitacao() {
        return new HashSet<>(pendentesSolicitacao);
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public int getNumeroMembros() {
        return membros.size();
    }

    public void adicionarVoto(String solicitante, String votante, boolean aprovado) {
        if(!votosAprovacao.containsKey(solicitante)) {
            votosAprovacao.put(solicitante, new HashSet<>());
        }
        if (aprovado) {
            votosAprovacao.get(solicitante).add(votante);
        } else {
            votosAprovacao.get(solicitante).remove(votante);
        }
    }

    @Override
    public String toString() {
        return String.format("Grupo: %s (Criador: %s, Membros: %d)",
                nome, criador, membros.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Group group = (Group) obj;
        return nome.equals(group.nome);
    }

    @Override
    public int hashCode() {
        return nome.hashCode();
    }
}