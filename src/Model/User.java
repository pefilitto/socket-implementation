package Model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class User {
    private String nomeCompleto;
    private String login;
    private String email;
    private String senha;
    private StatusUsuario status;
    private boolean online;
    private LocalDateTime ultimoAcesso;
    private Set<String> gruposParticipando;

    public enum StatusUsuario {
        ONLINE, OFFLINE, OCUPADO, AUSENTE
    }

    public User(String nomeCompleto, String login, String email, String senha) {
        this.nomeCompleto = nomeCompleto;
        this.login = login;
        this.email = email;
        this.senha = senha;
        this.status = StatusUsuario.OFFLINE;
        this.online = false;
        this.ultimoAcesso = LocalDateTime.now();
        this.gruposParticipando = new HashSet<>();
    }

    // Getters e Setters
    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
        if (online) {
            this.status = StatusUsuario.ONLINE;
        } else {
            this.status = StatusUsuario.OFFLINE;
        }
        this.ultimoAcesso = LocalDateTime.now();
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    public Set<String> getGruposParticipando() {
        return gruposParticipando;
    }

    public void adicionarGrupo(String nomeGrupo) {
        this.gruposParticipando.add(nomeGrupo);
    }

    public void removerGrupo(String nomeGrupo) {
        this.gruposParticipando.remove(nomeGrupo);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", nomeCompleto, login, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return login.equals(user.login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }
}