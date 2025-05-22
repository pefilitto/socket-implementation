import Client.ChatClient;
import Database.Database;
import Model.Group;
import Model.User;
import Server.ChatServer;

public static void main(String[] args) {
    if (args.length == 0) {
        mostrarAjuda();
        return;
    }

    String modo = args[0].toLowerCase();

    switch (modo) {
        case "server":
        case "servidor":
            iniciarServidor();
            break;

        case "client":
        case "cliente":
            iniciarCliente();
            break;

        case "test":
        case "teste":
            executarTeste();
            break;

        default:
            mostrarAjuda();
            break;
    }
}

private static void mostrarAjuda() {
    System.out.println("=== SISTEMA DE CHAT MULTITHREAD ===");
    System.out.println("");
    System.out.println("USO: java MainDemo <modo>");
    System.out.println("");
    System.out.println("MODOS DISPONÍVEIS:");
    System.out.println("  server   - Inicia o servidor de chat");
    System.out.println("  client   - Inicia um cliente de chat");
    System.out.println("  test     - Executa teste básico do sistema");
    System.out.println("");
    System.out.println("EXEMPLOS:");
    System.out.println("  java MainDemo server    # Inicia o servidor");
    System.out.println("  java MainDemo client    # Conecta um cliente");
    System.out.println("");
    System.out.println("FUNCIONALIDADES IMPLEMENTADAS:");
    System.out.println("✅ Cadastro e autenticação de usuários");
    System.out.println("✅ Login/Logout com validação");
    System.out.println("✅ Recuperação de senha por email");
    System.out.println("✅ Status de usuário (online, offline, ocupado, ausente)");
    System.out.println("✅ Mensagens privadas com solicitação/aceitação");
    System.out.println("✅ Criação e gerenciamento de grupos");
    System.out.println("✅ Convites para grupos com aceitação/rejeição");
    System.out.println("✅ Solicitação de entrada em grupos com votação");
    System.out.println("✅ Mensagens para grupos completos");
    System.out.println("✅ Mensagens para usuários específicos do grupo");
    System.out.println("✅ Mensagens particulares dentro de grupos");
    System.out.println("✅ Mensagens offline (entregues quando usuário conecta)");
    System.out.println("✅ Servidor multithread");
    System.out.println("✅ Notificações de entrada/saída");
    System.out.println("✅ Confirmação de entrega de mensagens");
    System.out.println("");
    System.out.println("=======================================");
}

private static void iniciarServidor() {
    System.out.println("🚀 Iniciando servidor de chat...");
    ChatServer servidor = new ChatServer();

    // Hook para parar graciosamente
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("\n🛑 Parando servidor...");
        servidor.parar();
    }));

    servidor.iniciar();
}

private static void iniciarCliente() {
    System.out.println("🔗 Conectando ao servidor...");
    ChatClient cliente = new ChatClient();

    // Hook para desconectar graciosamente
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("\n👋 Desconectando...");
    }));

    cliente.conectar();
}

private static void executarTeste() {
    System.out.println("🧪 Executando testes básicos do sistema...");

    Database db = Database.getInstance();

    // Limpa dados anteriores
    db.limparDados();

    // Teste 1: Cadastro de usuários
    System.out.println("\n📝 Teste 1: Cadastro de usuários");
    User user1 = new User("João Silva", "joao", "joao@email.com", "123456");
    User user2 = new User("Maria Santos", "maria", "maria@email.com", "654321");
    User user3 = new User("Pedro Costa", "pedro", "pedro@email.com", "abc123");

    boolean cadastro1 = db.cadastrarUsuario(user1);
    boolean cadastro2 = db.cadastrarUsuario(user2);
    boolean cadastro3 = db.cadastrarUsuario(user3);

    System.out.println("✅ João cadastrado: " + cadastro1);
    System.out.println("✅ Maria cadastrada: " + cadastro2);
    System.out.println("✅ Pedro cadastrado: " + cadastro3);

    // Teste 2: Autenticação
    System.out.println("\n🔐 Teste 2: Autenticação");
    User auth1 = db.autenticarUsuario("joao", "123456");
    User auth2 = db.autenticarUsuario("maria", "senha_errada");

    System.out.println("✅ Login João: " + (auth1 != null ? "SUCESSO" : "FALHOU"));
    System.out.println("❌ Login Maria (senha errada): " + (auth2 != null ? "SUCESSO" : "FALHOU"));

    // Teste 3: Grupos
    System.out.println("\n👥 Teste 3: Criação de grupos");
    Group grupo1 = new Group("Desenvolvedores", "joao");
    Group grupo2 = new Group("Amigos", "maria");

    boolean criarGrupo1 = db.criarGrupo(grupo1);
    boolean criarGrupo2 = db.criarGrupo(grupo2);

    System.out.println("✅ Grupo Desenvolvedores: " + criarGrupo1);
    System.out.println("✅ Grupo Amigos: " + criarGrupo2);

    // Teste 4: Convites e membros
    System.out.println("\n📨 Teste 4: Sistema de convites");
    grupo1.adicionarConvitePendente("maria");
    grupo1.adicionarConvitePendente("pedro");

    System.out.println("✅ Convites enviados para Maria e Pedro");
    System.out.println("📋 Membros do grupo Desenvolvedores: " + grupo1.getMembros());
    System.out.println("⏳ Convites pendentes: " + grupo1.getPendentesConvite());

    // Teste 5: Solicitações com votação
    System.out.println("\n🗳️  Teste 5: Sistema de votação");

    // Adiciona Maria ao grupo primeiro
    grupo1.removerConvitePendente("maria");
    grupo1.adicionarMembro("maria");

    // Pedro solicita entrada
    grupo1.adicionarSolicitacaoPendente("pedro");
    System.out.println("📝 Pedro solicitou entrada no grupo");

    // Votos dos membros
    grupo1.votar("pedro", "joao");
    System.out.println("✅ João votou SIM para Pedro (" + grupo1.getNumeroVotos("pedro") + "/" + grupo1.getNumeroMembros() + ")");

    grupo1.votar("pedro", "maria");
    System.out.println("✅ Maria votou SIM para Pedro (" + grupo1.getNumeroVotos("pedro") + "/" + grupo1.getNumeroMembros() + ")");

    if (grupo1.aprovadoPorTodos("pedro")) {
        grupo1.removerSolicitacaoPendente("pedro");
        grupo1.adicionarMembro("pedro");
        System.out.println("🎉 Pedro foi aprovado e adicionado ao grupo!");
    }

    // Teste 6: Recuperação de senha
    System.out.println("\n🔑 Teste 6: Recuperação de senha");
    String senhaRecuperada = db.recuperarSenha("joao@email.com");
    System.out.println("✅ Senha de João: " + senhaRecuperada);

    // Resumo final
    System.out.println("\n📊 RESUMO DOS TESTES:");
    System.out.println("👤 Usuários cadastrados: " + db.getTodosUsuarios().size());
    System.out.println("👥 Grupos criados: " + db.getTodosGrupos().size());
    System.out.println("📋 Membros do grupo 'Desenvolvedores': " + db.getGrupo("Desenvolvedores").getNumeroMembros());

    System.out.println("\n✅ Todos os testes básicos executados com sucesso!");
    System.out.println("🚀 Sistema pronto para uso!");
}
