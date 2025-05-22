package Database;

import Model.Group;
import Model.Message;
import Model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Database {
    private static Database instance;

    // Armazenamento em memória (em uma implementação real, seria em banco de dados)
    private ConcurrentHashMap<String, User> usuarios; // login -> Model.User
    private ConcurrentHashMap<String, Group> grupos; // nome -> Model.Group
    private CopyOnWriteArrayList<Message> mensagens;
    private ConcurrentHashMap<String, List<Message>> mensagensPendentes; // login -> List<Model.Message>
    private ConcurrentHashMap<String, Set<String>> solicitacoesAmizade; // solicitante -> Set<destinatarios>

    private Database() {
        usuarios = new ConcurrentHashMap<>();
        grupos = new ConcurrentHashMap<>();
        mensagens = new CopyOnWriteArrayList<>();
        mensagensPendentes = new ConcurrentHashMap<>();
        solicitacoesAmizade = new ConcurrentHashMap<>();
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // Métodos para Usuários
    public boolean cadastrarUsuario(User usuario) {
        if (usuarios.containsKey(usuario.getLogin())) {
            return false; // Usuário já existe
        }

        // Verifica se já existe usuário com mesmo nome completo
        for (User u : usuarios.values()) {
            if (u.getNomeCompleto().equals(usuario.getNomeCompleto())) {
                return false;
            }
        }

        usuarios.put(usuario.getLogin(), usuario);
        mensagensPendentes.put(usuario.getLogin(), new CopyOnWriteArrayList<>());
        return true;
    }

    public User autenticarUsuario(String login, String senha) {
        User usuario = usuarios.get(login);
        if (usuario != null && usuario.getSenha().equals(senha)) {
            return usuario;
        }
        return null;
    }

    public String recuperarSenha(String email) {
        for (User usuario : usuarios.values()) {
            if (usuario.getEmail().equals(email)) {
                return usuario.getSenha();
            }
        }
        return null;
    }

    public User getUsuario(String login) {
        return usuarios.get(login);
    }

    public List<User> getUsuariosOnline() {
        List<User> online = new ArrayList<>();
        for (User usuario : usuarios.values()) {
            if (usuario.isOnline()) {
                online.add(usuario);
            }
        }
        return online;
    }

    public List<User> getTodosUsuarios() {
        return new ArrayList<>(usuarios.values());
    }

    // Métodos para Grupos
    public boolean criarGrupo(Group grupo) {
        if (grupos.containsKey(grupo.getNome())) {
            return false; // Grupo já existe
        }
        grupos.put(grupo.getNome(), grupo);
        return true;
    }

    public Group getGrupo(String nome) {
        return grupos.get(nome);
    }

    public List<Group> getTodosGrupos() {
        return new ArrayList<>(grupos.values());
    }

    public boolean removerGrupo(String nome) {
        return grupos.remove(nome) != null;
    }

    // Métodos para Mensagens
    public void salvarMensagem(Message mensagem) {
        mensagens.add(mensagem);
    }

    public List<Message> getMensagensUsuario(String login) {
        List<Message> mensagensUsuario = new ArrayList<>();
        for (Message msg : mensagens) {
            if (msg.getRemetente().equals(login) ||
                    msg.getDestinatarios().contains(login)) {
                mensagensUsuario.add(msg);
            }
        }
        return mensagensUsuario;
    }

    public List<Message> getMensagensGrupo(String nomeGrupo) {
        List<Message> mensagensGrupo = new ArrayList<>();
        for (Message msg : mensagens) {
            if (nomeGrupo.equals(msg.getGrupo())) {
                mensagensGrupo.add(msg);
            }
        }
        return mensagensGrupo;
    }

    // Métodos para Mensagens Pendentes
    public void adicionarMensagemPendente(String login, Message mensagem) {
        mensagensPendentes.computeIfAbsent(login, k -> new CopyOnWriteArrayList<>()).add(mensagem);
    }

    public List<Message> getMensagensPendentes(String login) {
        return mensagensPendentes.getOrDefault(login, new ArrayList<>());
    }

    public void limparMensagensPendentes(String login) {
        mensagensPendentes.getOrDefault(login, new ArrayList<>()).clear();
    }

    // Métodos para Solicitações de Amizade
    public void adicionarSolicitacaoAmizade(String solicitante, String destinatario) {
        solicitacoesAmizade.computeIfAbsent(solicitante, k -> new HashSet<>()).add(destinatario);
    }

    public void removerSolicitacaoAmizade(String solicitante, String destinatario) {
        Set<String> solicitacoes = solicitacoesAmizade.get(solicitante);
        if (solicitacoes != null) {
            solicitacoes.remove(destinatario);
            if (solicitacoes.isEmpty()) {
                solicitacoesAmizade.remove(solicitante);
            }
        }
    }

    public boolean temSolicitacaoAmizade(String solicitante, String destinatario) {
        Set<String> solicitacoes = solicitacoesAmizade.get(solicitante);
        return solicitacoes != null && solicitacoes.contains(destinatario);
    }

    public Set<String> getSolicitacoesAmizade(String destinatario) {
        Set<String> solicitantes = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : solicitacoesAmizade.entrySet()) {
            if (entry.getValue().contains(destinatario)) {
                solicitantes.add(entry.getKey());
            }
        }
        return solicitantes;
    }

    // Método para limpar dados (útil para testes)
    public void limparDados() {
        usuarios.clear();
        grupos.clear();
        mensagens.clear();
        mensagensPendentes.clear();
        solicitacoesAmizade.clear();
    }
}