package Server;

import Database.Database;
import Model.Group;
import Model.Message;
import Model.User;
import Util.UserRegistration;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class CommandProcessor {
    private ClientHandler clientHandler;
    private ChatServer servidor;
    private Database database;
    private UserRegistration userRegistration;

    public CommandProcessor(ClientHandler clientHandler, ChatServer servidor, Database database) {
        this.clientHandler = clientHandler;
        this.servidor = servidor;
        this.database = database;
        this.userRegistration = new UserRegistration(database, clientHandler);
    }

    public void processarComando(String comando) {
        String[] partes = comando.split("\\s+", 2);
        String cmd = partes[0].toLowerCase();

        switch (cmd) {
            case "help":
                clientHandler.mostrarAjuda();
                break;

            case "register":
                if (partes.length < 2) {
                    userRegistration.iniciarCadastro();
                } else {
                    userRegistration.processarCadastro(comando);
                }
                break;

            case "login":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: login <usuario> <senha>");
                } else {
                    String[] loginParts = partes[1].split("\\s+", 2);
                    if (loginParts.length < 2) {
                        clientHandler.enviarMensagem("Uso: login <usuario> <senha>");
                    } else {
                        processarLogin(loginParts[0], loginParts[1]);
                    }
                }
                break;

            case "logout":
                processarLogout();
                break;

            case "recover":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: recover <email>");
                } else {
                    processarRecuperacao(partes[1]);
                }
                break;

            case "status":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: status <online|offline|ocupado|ausente>");
                } else {
                    processarMudancaStatus(partes[1]);
                }
                break;

            case "users":
                listarUsuarios();
                break;

            case "groups":
                listarGrupos();
                break;

            case "msg":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: msg <usuario> <mensagem>");
                } else {
                    String[] msgParts = partes[1].split("\\s+", 2);
                    if (msgParts.length < 2) {
                        clientHandler.enviarMensagem("Uso: msg <usuario> <mensagem>");
                    } else {
                        processarMensagemPrivada(msgParts[0], msgParts[1]);
                    }
                }
                break;

            case "accept":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: accept <usuario>");
                } else {
                    processarAceitarSolicitacao(partes[1]);
                }
                break;

            case "reject":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: reject <usuario>");
                } else {
                    processarRejeitarSolicitacao(partes[1]);
                }
                break;

            case "create":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: create <nome_grupo>");
                } else {
                    criarGrupo(partes[1]);
                }
                break;

            case "invite":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: invite <grupo> <usuario>");
                } else {
                    String[] inviteParts = partes[1].split("\\s+", 2);
                    if (inviteParts.length < 2) {
                        clientHandler.enviarMensagem("Uso: invite <grupo> <usuario>");
                    } else {
                        convidarParaGrupo(inviteParts[0], inviteParts[1]);
                    }
                }
                break;

            case "join":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: join <grupo>");
                } else {
                    solicitarEntradaGrupo(partes[1]);
                }
                break;

            case "leave":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: leave <grupo>");
                } else {
                    sairDoGrupo(partes[1]);
                }
                break;

            case "group":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: group <grupo> <mensagem> [-> <usuarios>]");
                } else {
                    processarMensagemGrupo(partes[1]);
                }
                break;

            case "accept_group":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: accept_group <grupo>");
                } else {
                    aceitarConviteGrupo(partes[1]);
                }
                break;

            case "reject_group":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: reject_group <grupo>");
                } else {
                    rejeitarConviteGrupo(partes[1]);
                }
                break;

            case "vote_yes":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: vote_yes <grupo> <usuario>");
                } else {
                    String[] voteParts = partes[1].split("\\s+", 2);
                    if (voteParts.length < 2) {
                        clientHandler.enviarMensagem("Uso: vote_yes <grupo> <usuario>");
                    } else {
                        votarSim(voteParts[0], voteParts[1]);
                    }
                }
                break;

            case "vote_no":
                if (partes.length < 2) {
                    clientHandler.enviarMensagem("Uso: vote_no <grupo> <usuario>");
                } else {
                    String[] voteParts = partes[1].split("\\s+", 2);
                    if (voteParts.length < 2) {
                        clientHandler.enviarMensagem("Uso: vote_no <grupo> <usuario>");
                    } else {
                        votarNao(voteParts[0], voteParts[1]);
                    }
                }
                break;

            case "quit":
                clientHandler.enviarMensagem("Tchau!");
                clientHandler.desconectar();
                break;

            default:
                // Verifica se é mensagem particular no grupo (formato: mensagem -> grupo@usuario)
                if (comando.contains("->") && comando.contains("@")) {
                    processarMensagemParticular(comando);
                } else {
                    clientHandler.enviarMensagem("Comando não reconhecido. Digite 'help' para ver os comandos.");
                }
                break;
        }
    }

    public void aceitarConviteGrupo(String nomeGrupo) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) {
            clientHandler.enviarMensagem("Grupo não encontrado!");
            return;
        }

        if (!grupo.temConvitePendente(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você não tem convite pendente para este grupo!");
            return;
        }

        grupo.removerConvitePendente(clientHandler.getLoginUsuario());
        grupo.adicionarMembro(clientHandler.getLoginUsuario());

        User usuario = database.getUsuario(clientHandler.getLoginUsuario());
        if (usuario != null) {
            usuario.adicionarGrupo(nomeGrupo);
        }

        clientHandler.enviarMensagem("Você aceitou o convite para o grupo '" + nomeGrupo + "'");
    }

    public void rejeitarConviteGrupo(String nomeGrupo) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) {
            clientHandler.enviarMensagem("Grupo não encontrado!");
            return;
        }

        if (!grupo.temConvitePendente(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você não tem convite pendente para este grupo!");
            return;
        }

        grupo.removerConvitePendente(clientHandler.getLoginUsuario());
        clientHandler.enviarMensagem("Você rejeitou o convite para o grupo '" + nomeGrupo + "'");
    }

    public void votarSim(String nomeGrupo, String usuario) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) {
            clientHandler.enviarMensagem("Grupo não encontrado!");
            return;
        }

        if (!grupo.temSolicitacaoPendente(usuario)) {
            clientHandler.enviarMensagem("Não há solicitação pendente de " + usuario + " para este grupo.");
            return;
        }

        grupo.adicionarVoto(usuario, clientHandler.getLoginUsuario(), true);
        clientHandler.enviarMensagem("✅ Você votou a favor da entrada de " + usuario +
                                     " no grupo '" + nomeGrupo + "'");

        if (grupo.aprovadoPorTodos(usuario)) {
            grupo.removerSolicitacaoPendente(usuario);
            grupo.adicionarMembro(usuario);

            User novoMembro = database.getUsuario(usuario);
            if (novoMembro != null) novoMembro.adicionarGrupo(nomeGrupo);

            notificarGrupoEntrada(nomeGrupo, usuario);
        }
    }

    public void votarNao(String nomeGrupo, String usuario) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) {
            clientHandler.enviarMensagem("Grupo não encontrado!");
            return;
        }

        if (!grupo.temSolicitacaoPendente(usuario)) {
            clientHandler.enviarMensagem("Não há solicitação pendente de " + usuario + " para este grupo.");
            return;
        }

        grupo.adicionarVoto(usuario, clientHandler.getLoginUsuario(), false);
        clientHandler.enviarMensagem("❌ Você votou contra a entrada de " + usuario +
                                     " no grupo '" + nomeGrupo + "'");

        grupo.removerSolicitacaoPendente(usuario);
        notificarGrupoRejeicao(nomeGrupo, usuario);
    }

    // private void processarCadastro() {
    //     if (clientHandler.getLoginUsuario() != null) {
    //         clientHandler.enviarMensagem("Você já está logado!");
    //         return;
    //     }

    //     Scanner scanner = new Scanner(System.in);

    //     clientHandler.enviarMensagem("=== CADASTRO DE USUÁRIO ===");
    //     clientHandler.enviarMensagem("Digite seu nome completo:");
    //     // Nota: Em uma implementação real, você precisaria de um mecanismo para entrada interativa
    //     // Por simplicidade, vou mostrar como seria o fluxo

    //     clientHandler.enviarMensagem("Para completar o cadastro, use: register <nome> <login> <email> <senha>");
    //     clientHandler.enviarMensagem("Exemplo: register João Silva joao joao@email.com 123456");
    // }

    private void processarLogin(String login, String senha) {
        if (clientHandler.getLoginUsuario() != null) {
            clientHandler.enviarMensagem("Você já está logado!");
            return;
        }

        User usuario = database.autenticarUsuario(login, senha);
        if (usuario == null) {
            clientHandler.enviarMensagem("Login ou senha incorretos!");
            return;
        }

        // Verifica se usuário já está conectado
        if (servidor.isClienteConectado(login)) {
            clientHandler.enviarMensagem("Este usuário já está conectado!");
            return;
        }

        clientHandler.setLoginUsuario(login);
        servidor.adicionarCliente(login, clientHandler);

        clientHandler.enviarMensagem("Login realizado com sucesso!");
        clientHandler.enviarMensagem("Bem-vindo, " + usuario.getNomeCompleto() + "!");

        // Mostra notificações pendentes
        mostrarNotificacoesPendentes();
    }

    private void processarLogout() {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você não está logado!");
            return;
        }

        servidor.removerCliente(clientHandler.getLoginUsuario());
        clientHandler.setLoginUsuario(null);
        clientHandler.enviarMensagem("Logout realizado com sucesso!");
    }

    private void processarRecuperacao(String email) {
        String senha = database.recuperarSenha(email);
        if (senha != null) {
            clientHandler.enviarMensagem("Sua senha é: " + senha);
        } else {
            clientHandler.enviarMensagem("Email não encontrado!");
        }
    }

    private void processarMudancaStatus(String novoStatus) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        User usuario = database.getUsuario(clientHandler.getLoginUsuario());
        if (usuario == null) return;

        try {
            User.StatusUsuario status = User.StatusUsuario.valueOf(novoStatus.toUpperCase());
            usuario.setStatus(status);
            clientHandler.enviarMensagem("Status alterado para: " + status);
        } catch (IllegalArgumentException e) {
            clientHandler.enviarMensagem("Status inválido! Use: online, offline, ocupado, ausente");
        }
    }

    private void listarUsuarios() {
        List<User> usuariosOnline = database.getUsuariosOnline();

        clientHandler.enviarMensagem("\n=== USUÁRIOS ONLINE ===");
        if (usuariosOnline.isEmpty()) {
            clientHandler.enviarMensagem("Nenhum usuário online.");
        } else {
            for (User user : usuariosOnline) {
                String status = user.getLogin().equals(clientHandler.getLoginUsuario()) ? " (você)" : "";
                clientHandler.enviarMensagem(String.format("• %s%s", user.toString(), status));
            }
        }
        clientHandler.enviarMensagem("========================\n");
    }

    private void listarGrupos() {
        List<Group> grupos = database.getTodosGrupos();

        clientHandler.enviarMensagem("\n=== GRUPOS DISPONÍVEIS ===");
        if (grupos.isEmpty()) {
            clientHandler.enviarMensagem("Nenhum grupo criado.");
        } else {
            for (Group grupo : grupos) {
                String membro = grupo.isMembro(clientHandler.getLoginUsuario()) ? " (membro)" : "";
                clientHandler.enviarMensagem(String.format("• %s%s", grupo.toString(), membro));
            }
        }
        clientHandler.enviarMensagem("===========================\n");
    }

    private void processarMensagemPrivada(String destinatario, String conteudo) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        if (destinatario.equals(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você não pode enviar mensagem para si mesmo!");
            return;
        }

        // Verifica se o destinatário existe
        User usuarioDestinatario = database.getUsuario(destinatario);
        if (usuarioDestinatario == null) {
            clientHandler.enviarMensagem("Usuário '" + destinatario + "' não encontrado!");
            return;
        }

        // Verifica se já existe amizade/conexão estabelecida ou solicitação aceita
        if (!database.temAmizadeEstabelecida(clientHandler.getLoginUsuario(), destinatario)) {
            // Verifica se já há solicitação pendente
            if (database.temSolicitacaoAmizade(clientHandler.getLoginUsuario(), destinatario)) {
                clientHandler.enviarMensagem("Você já enviou uma solicitação para " + destinatario + ". Aguarde a resposta.");
                return;
            }

            // Cria nova solicitação
            database.adicionarSolicitacaoAmizade(clientHandler.getLoginUsuario(), destinatario);

            // Notifica o destinatário se estiver online
            ClientHandler clienteDestinatario = servidor.getClientHandler(destinatario);
            if (clienteDestinatario != null) {
                User remetente = database.getUsuario(clientHandler.getLoginUsuario());
                String nomeRemetente = remetente != null ? remetente.getNomeCompleto() : clientHandler.getLoginUsuario();
                clienteDestinatario.enviarMensagem(
                        String.format("[SOLICITAÇÃO] %s quer conversar com você. Digite 'accept %s' ou 'reject %s'",
                                nomeRemetente, clientHandler.getLoginUsuario(), clientHandler.getLoginUsuario())
                );
            } else {
                // Usuário offline - salva notificação
                Message notificacao = new Message("SISTEMA", Arrays.asList(destinatario),
                        String.format("Solicitação de conversa de %s. Digite 'accept %s' quando estiver online.",
                                clientHandler.getLoginUsuario(), clientHandler.getLoginUsuario()),
                        Message.TipoMensagem.SISTEMA);
                database.adicionarMensagemPendente(destinatario, notificacao);
            }

            clientHandler.enviarMensagem("Solicitação de conversa enviada para " + destinatario + ". Aguarde a resposta.");

            return;
        }
        else{
            processarAceitarSolicitacao(clientHandler.getLoginUsuario());
        }

        // Se chegou aqui, a amizade está estabelecida - envia a mensagem
        boolean enviada = servidor.enviarMensagemPrivada(clientHandler.getLoginUsuario(), destinatario, conteudo);

        if (enviada) {
            clientHandler.enviarMensagem("✓ Mensagem enviada para " + destinatario);
        } else {
            clientHandler.enviarMensagem("✗ Não foi possível enviar a mensagem para " + destinatario + " (usuário offline)");
        }
    }

    private void processarAceitarSolicitacao(String solicitante) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        if (!database.temSolicitacaoAmizade(solicitante, clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Não há solicitação pendente de " + solicitante);
            return;
        }

        // Remove a solicitação e estabelece a amizade
        database.removerSolicitacaoAmizade(solicitante, clientHandler.getLoginUsuario());
        database.estabelecerAmizade(solicitante, clientHandler.getLoginUsuario());

        // Notifica o solicitante
        ClientHandler clienteSolicitante = servidor.getClientHandler(solicitante);
        if (clienteSolicitante != null) {
            User destinatario = database.getUsuario(clientHandler.getLoginUsuario());
            String nomeDestinatario = destinatario != null ? destinatario.getNomeCompleto() : clientHandler.getLoginUsuario();
            clienteSolicitante.enviarMensagem(
                    String.format("✓ %s aceitou sua solicitação de conversa! Agora vocês podem trocar mensagens.", nomeDestinatario)
            );
        } else {
            // Solicitante offline - salva notificação
            Message notificacao = new Message("SISTEMA", Arrays.asList(solicitante),
                    String.format("%s aceitou sua solicitação de conversa!", clientHandler.getLoginUsuario()),
                    Message.TipoMensagem.SISTEMA);
            database.adicionarMensagemPendente(solicitante, notificacao);
        }

        clientHandler.enviarMensagem("✓ Solicitação aceita! Agora vocês podem conversar livremente.");
    }

    private void processarRejeitarSolicitacao(String solicitante) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        if (database.temSolicitacaoAmizade(solicitante, clientHandler.getLoginUsuario())) {
            database.removerSolicitacaoAmizade(solicitante, clientHandler.getLoginUsuario());

            // Notifica o solicitante
            ClientHandler clienteSolicitante = servidor.getClientHandler(solicitante);
            if (clienteSolicitante != null) {
                User destinatario = database.getUsuario(clientHandler.getLoginUsuario());
                String nomeDestinatario = destinatario != null ? destinatario.getNomeCompleto() : clientHandler.getLoginUsuario();
                clienteSolicitante.enviarMensagem(
                        String.format("✗ %s rejeitou sua solicitação de conversa.", nomeDestinatario)
                );
            }

            clientHandler.enviarMensagem("Solicitação rejeitada.");
        } else {
            clientHandler.enviarMensagem("Não há solicitação pendente de " + solicitante);
        }
    }

    private void criarGrupo(String nomeGrupo) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        Group novoGrupo = new Group(nomeGrupo, clientHandler.getLoginUsuario());
        if (database.criarGrupo(novoGrupo)) {
            User usuario = database.getUsuario(clientHandler.getLoginUsuario());
            if (usuario != null) {
                usuario.adicionarGrupo(nomeGrupo);
            }
            clientHandler.enviarMensagem("Grupo '" + nomeGrupo + "' criado com sucesso!");
        } else {
            clientHandler.enviarMensagem("Já existe um grupo com esse nome!");
        }
    }

    private void convidarParaGrupo(String nomeGrupo, String usuarioConvidado) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) {
            clientHandler.enviarMensagem("Grupo não encontrado!");
            return;
        }

        if (!grupo.isMembro(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você não é membro deste grupo!");
            return;
        }

        User usuario = database.getUsuario(usuarioConvidado);
        if (usuario == null) {
            clientHandler.enviarMensagem("Usuário não encontrado!");
            return;
        }

        if (grupo.isMembro(usuarioConvidado)) {
            clientHandler.enviarMensagem("Este usuário já é membro do grupo!");
            return;
        }

        if (grupo.temConvitePendente(usuarioConvidado)) {
            clientHandler.enviarMensagem("Este usuário já tem um convite pendente!");
            return;
        }

        grupo.adicionarConvitePendente(usuarioConvidado);

        // Notifica o usuário convidado
        ClientHandler clienteConvidado = servidor.getClientHandler(usuarioConvidado);
        if (clienteConvidado != null) {
            User remetente = database.getUsuario(clientHandler.getLoginUsuario());
            String nomeRemetente = remetente != null ? remetente.getNomeCompleto() : clientHandler.getLoginUsuario();
            clienteConvidado.enviarMensagem(
                    String.format("[CONVITE] %s convidou você para o grupo '%s'. Digite 'accept_group %s' ou 'reject_group %s'",
                            nomeRemetente, nomeGrupo, nomeGrupo, nomeGrupo)
            );
        } else {
            // Usuário offline - adiciona à lista de mensagens pendentes
            Message convite = new Message("SISTEMA", Arrays.asList(usuarioConvidado),
                    String.format("Você foi convidado para o grupo '%s' por %s", nomeGrupo, clientHandler.getLoginUsuario()),
                    Message.TipoMensagem.SISTEMA);
            database.adicionarMensagemPendente(usuarioConvidado, convite);
        }

        clientHandler.enviarMensagem("Convite enviado para " + usuarioConvidado);
    }

    private void solicitarEntradaGrupo(String nomeGrupo) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) {
            clientHandler.enviarMensagem("Grupo não encontrado!");
            return;
        }

        if (grupo.isMembro(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você já é membro deste grupo!");
            return;
        }

        if (grupo.temSolicitacaoPendente(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você já tem uma solicitação pendente para este grupo!");
            return;
        }

        grupo.adicionarSolicitacaoPendente(clientHandler.getLoginUsuario());

        // Notifica todos os membros do grupo
        User solicitante = database.getUsuario(clientHandler.getLoginUsuario());
        String nomeSolicitante = solicitante != null ? solicitante.getNomeCompleto() : clientHandler.getLoginUsuario();

        for (String membro : grupo.getMembros()) {
            ClientHandler clienteMembro = servidor.getClientHandler(membro);
            if (clienteMembro != null) {
                clienteMembro.enviarMensagem(
                        String.format("[SOLICITAÇÃO] %s quer entrar no grupo '%s'. Digite 'vote_yes %s %s' ou 'vote_no %s %s'",
                                nomeSolicitante, nomeGrupo, nomeGrupo, clientHandler.getLoginUsuario(), nomeGrupo, clientHandler.getLoginUsuario())
                );
            }
        }

        clientHandler.enviarMensagem("Solicitação enviada! Aguarde a aprovação de todos os membros.");
    }

    private void sairDoGrupo(String nomeGrupo) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) {
            clientHandler.enviarMensagem("Grupo não encontrado!");
            return;
        }

        if (!grupo.isMembro(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você não é membro deste grupo!");
            return;
        }

        if (grupo.getCriador().equals(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você é o criador do grupo e não pode sair!");
            return;
        }

        grupo.removerMembro(clientHandler.getLoginUsuario());
        User usuario = database.getUsuario(clientHandler.getLoginUsuario());
        if (usuario != null) {
            usuario.removerGrupo(nomeGrupo);
        }

        // Notifica outros membros
        clientHandler.notificarSaidaGrupo(nomeGrupo, clientHandler.getLoginUsuario(), "saiu do grupo");

        clientHandler.enviarMensagem("Você saiu do grupo '" + nomeGrupo + "'");
    }

    private void processarMensagemGrupo(String comando) {
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        // Formato: grupo mensagem [-> usuario1,usuario2]
        String[] partes = comando.split("\\s+", 2);
        if (partes.length < 2) {
            clientHandler.enviarMensagem("Uso: group <grupo> <mensagem> [-> <usuarios>]");
            return;
        }

        String nomeGrupo = partes[0];
        String resto = partes[1];

        List<String> destinatariosEspecificos = null;
        String conteudo;

        if (resto.contains(" -> ")) {
            String[] msgPartes = resto.split(" -> ", 2);
            conteudo = msgPartes[0].trim();
            String usuarios = msgPartes[1].trim();
            destinatariosEspecificos = Arrays.asList(usuarios.split(","));
            // Remove espaços em branco
            destinatariosEspecificos.replaceAll(String::trim);
        } else {
            conteudo = resto;
        }

        servidor.enviarMensagemGrupo(clientHandler.getLoginUsuario(), nomeGrupo, conteudo, destinatariosEspecificos);
    }

    private void processarMensagemParticular(String comando) {
        // Formato: mensagem -> grupo@usuario
        if (clientHandler.getLoginUsuario() == null) {
            clientHandler.enviarMensagem("Você precisa estar logado!");
            return;
        }

        String[] partes = comando.split(" -> ", 2);
        if (partes.length != 2) {
            clientHandler.enviarMensagem("Formato: <mensagem> -> <grupo>@<usuario>");
            return;
        }

        String conteudo = partes[0].trim();
        String destino = partes[1].trim();

        if (!destino.contains("@")) {
            clientHandler.enviarMensagem("Formato: <mensagem> -> <grupo>@<usuario>");
            return;
        }

        String[] destinoParts = destino.split("@", 2);
        String nomeGrupo = destinoParts[0].trim();
        String usuario = destinoParts[1].trim();

        Group grupo = database.getGrupo(nomeGrupo);
        if (grupo == null) {
            clientHandler.enviarMensagem("Grupo não encontrado!");
            return;
        }

        if (!grupo.isMembro(clientHandler.getLoginUsuario())) {
            clientHandler.enviarMensagem("Você não é membro deste grupo!");
            return;
        }

        if (!grupo.isMembro(usuario)) {
            clientHandler.enviarMensagem("O usuário não é membro deste grupo!");
            return;
        }

        servidor.enviarMensagemGrupo(clientHandler.getLoginUsuario(), nomeGrupo, conteudo, Arrays.asList(usuario));
    }

    private void mostrarNotificacoesPendentes() {
        if (clientHandler.getLoginUsuario() == null) return;

        // Mostra solicitações de amizade pendentes
        Set<String> solicitacoes = database.getSolicitacoesAmizade(clientHandler.getLoginUsuario());
        if (!solicitacoes.isEmpty()) {
            clientHandler.enviarMensagem("\n🔔 === SOLICITAÇÕES PENDENTES ===");
            for (String solicitante : solicitacoes) {
                User user = database.getUsuario(solicitante);
                String nome = user != null ? user.getNomeCompleto() : solicitante;
                clientHandler.enviarMensagem("💬 " + nome + " quer conversar com você");
            }
            clientHandler.enviarMensagem("Use 'accept <usuario>' ou 'reject <usuario>'");
            clientHandler.enviarMensagem("================================\n");
        }

        // Mostra convites de grupo pendentes
        List<Group> grupos = database.getTodosGrupos();
        boolean temConvites = false;

        for (Group grupo : grupos) {
            if (grupo.temConvitePendente(clientHandler.getLoginUsuario())) {
                if (!temConvites) {
                    clientHandler.enviarMensagem("\n👥 === CONVITES DE GRUPO ===");
                    temConvites = true;
                }
                clientHandler.enviarMensagem("📋 Convite para o grupo: " + grupo.getNome());
            }
        }

        if (temConvites) {
            clientHandler.enviarMensagem("Use 'accept_group <grupo>' ou 'reject_group <grupo>'");
            clientHandler.enviarMensagem("============================\n");
        }
    }

    private void notificarGrupoEntrada(String nomeGrupo, String novoMembro) {
        Group grupo = database.getGrupo(nomeGrupo);
        for (String membro : grupo.getMembros()) {
            ClientHandler ch = servidor.getClientHandler(membro);
            if (ch == null) continue;

            if (membro.equals(novoMembro)) {
                ch.enviarMensagem("🎉 Sua solicitação foi aprovada! Agora você faz parte do grupo '" +
                                  nomeGrupo + "'.");
            } else {
                ch.enviarMensagem("👥 " + novoMembro + " entrou no grupo '" + nomeGrupo + "'");
            }
        }
    }

    private void notificarGrupoRejeicao(String nomeGrupo, String solicitante) {
        Group grupo = database.getGrupo(nomeGrupo);

        ClientHandler solicitanteCH = servidor.getClientHandler(solicitante);
        if (solicitanteCH != null) {
            solicitanteCH.enviarMensagem("🙁 Sua solicitação para entrar no grupo '" +
                                         nomeGrupo + "' foi rejeitada.");
        }

        for (String membro : grupo.getMembros()) {
            ClientHandler ch = servidor.getClientHandler(membro);
            if (ch != null) {
                ch.enviarMensagem("🚫 A solicitação de " + solicitante +
                                  " para entrar no grupo '" + nomeGrupo + "' foi rejeitada.");
            }
        }
    }
}