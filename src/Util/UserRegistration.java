package Util;

import Database.Database;
import Model.User;
import Server.ClientHandler;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class UserRegistration {
    private Database database;
    private ClientHandler clientHandler;

    // Regex para validação de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    public UserRegistration(Database database, ClientHandler clientHandler) {
        this.database = database;
        this.clientHandler = clientHandler;
    }

    public void iniciarCadastro() {
        clientHandler.enviarMensagem("\n=== CADASTRO DE USUÁRIO ===");
        clientHandler.enviarMensagem("Para se cadastrar, forneça as seguintes informações:");
        clientHandler.enviarMensagem("Formato: register <nome_completo> <login> <email> <senha>");
        clientHandler.enviarMensagem("\nExemplo: register João Silva joao joao@email.com minhasenha");
        clientHandler.enviarMensagem("\nRegras:");
        clientHandler.enviarMensagem("• Nome completo: mínimo 2 palavras");
        clientHandler.enviarMensagem("• Login: único, sem espaços, mínimo 3 caracteres");
        clientHandler.enviarMensagem("• Email: formato válido");
        clientHandler.enviarMensagem("• Senha: mínimo 6 caracteres");
        clientHandler.enviarMensagem("===============================");
    }

    public boolean processarCadastro(String comando) {
        // Remove o comando "register" do início
        String dados = comando.substring(8).trim();
        String[] partes = dados.split("\\s+");

        if (partes.length < 4) {
            clientHandler.enviarMensagem("❌ Dados insuficientes!");
            clientHandler.enviarMensagem("Uso: register <nome_completo> <login> <email> <senha>");
            return false;
        }

        // Reconstrói o nome completo (pode ter espaços)
        StringBuilder nomeBuilder = new StringBuilder();
        int i = 0;

        // Procura pelo login (sem espaços e contém @ indica que chegou no email)
        while (i < partes.length - 3) {
            if (i > 0) nomeBuilder.append(" ");
            nomeBuilder.append(partes[i]);
            i++;
        }

        String nomeCompleto = nomeBuilder.toString();
        String login = partes[i];
        String email = partes[i + 1];
        String senha = partes[i + 2];

        // Validações
        if (!validarNomeCompleto(nomeCompleto)) {
            return false;
        }

        if (!validarLogin(login)) {
            return false;
        }

        if (!validarEmail(email)) {
            return false;
        }

        if (!validarSenha(senha)) {
            return false;
        }

        // Cria o usuário
        User novoUsuario = new User(nomeCompleto, login, email, senha);

        if (database.cadastrarUsuario(novoUsuario)) {
            clientHandler.enviarMensagem("✅ Usuário cadastrado com sucesso!");
            clientHandler.enviarMensagem("Agora você pode fazer login com: login " + login + " " + senha);
            return true;
        } else {
            clientHandler.enviarMensagem("❌ Erro ao cadastrar usuário!");
            clientHandler.enviarMensagem("Possíveis causas:");
            clientHandler.enviarMensagem("• Login já existe");
            clientHandler.enviarMensagem("• Nome completo já cadastrado");
            return false;
        }
    }

    private boolean validarNomeCompleto(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            clientHandler.enviarMensagem("❌ Nome completo não pode estar vazio!");
            return false;
        }

        String[] palavras = nome.trim().split("\\s+");
        if (palavras.length < 2) {
            clientHandler.enviarMensagem("❌ Nome completo deve ter pelo menos 2 palavras!");
            return false;
        }

        if (nome.length() < 5) {
            clientHandler.enviarMensagem("❌ Nome completo muito curto!");
            return false;
        }

        return true;
    }

    private boolean validarLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            clientHandler.enviarMensagem("❌ Login não pode estar vazio!");
            return false;
        }

        if (login.length() < 3) {
            clientHandler.enviarMensagem("❌ Login deve ter pelo menos 3 caracteres!");
            return false;
        }

        if (login.contains(" ")) {
            clientHandler.enviarMensagem("❌ Login não pode conter espaços!");
            return false;
        }

        if (!login.matches("[a-zA-Z0-9_]+")) {
            clientHandler.enviarMensagem("❌ Login deve conter apenas letras, números e sublinhado!");
            return false;
        }

        return true;
    }

    private boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            clientHandler.enviarMensagem("❌ Email não pode estar vazio!");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            clientHandler.enviarMensagem("❌ Email inválido!");
            return false;
        }

        return true;
    }

    private boolean validarSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            clientHandler.enviarMensagem("❌ Senha não pode estar vazia!");
            return false;
        }

        if (senha.length() < 6) {
            clientHandler.enviarMensagem("❌ Senha deve ter pelo menos 6 caracteres!");
            return false;
        }

        if (senha.contains(" ")) {
            clientHandler.enviarMensagem("❌ Senha não pode conter espaços!");
            return false;
        }

        return true;
    }
}