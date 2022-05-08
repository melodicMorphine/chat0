package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager extends Thread {

    private BufferedReader reader;
    private PrintWriter writer;
    private final Socket client;
    private String name;
    private static final Map<String, Manager> clients = new HashMap<String, Manager>();

    public Manager(Socket client) {
        this.client = client;
        start();
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            writer = new PrintWriter(client.getOutputStream(), true);
            doLogin();
            String message;
            while (true) {
                message = reader.readLine();
                if (message.equalsIgnoreCase(Commands.SAIR)) {
                    this.client.close();
                } else if (message.startsWith(Commands.MESSAGE)) {
                    String receiverName = message.substring(Commands.MESSAGE.length(), message.length());
                    System.out.println("Enviando para " + receiverName + "...");
                    Manager receiver = clients.get(receiverName);
                    if (receiver == null) {
                        writer.println("Cliente Não Existe");
                    } else {
                        receiver.getWriter().println(this.name + " disse: " + reader.readLine());
                    }
                } else if (message.equals(Commands.USER_LIST)) {
                    updateListUsers(this);
                } else {
                    writer.println(this.name + ", Você disse: " + message);
                }
            }
        } catch (IOException ex) {
            System.err.println("Cliente Fechou Conexão");
            clients.remove(this.name);
            for (String client : clients.keySet()) {
                    updateListUsers(clients.get(client));
                }
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doLogin() throws IOException {
        while (true) {
            writer.println(Commands.LOGIN);
            this.name = reader.readLine().toLowerCase().replaceAll(",", "");
            if (this.name.equalsIgnoreCase("null") || this.name.isEmpty()) {
                writer.println(Commands.LOGIN_DENIED);
            } else if (clients.containsKey(this.name)) {
                writer.println(Commands.LOGIN_DENIED);
            } else {
                writer.println(Commands.LOGIN_ACCEPTED);
                writer.println("Oi, " + this.name);
                clients.put(this.name, this);
                for (String client : clients.keySet()) {
                    updateListUsers(clients.get(client));
                }
                break;
            }
        }
    }

    private void updateListUsers(Manager manager) {
        StringBuffer str = new StringBuffer();
        for (String c : clients.keySet()) {
            if (manager.Name().equals(c)) continue;
            str.append(c);
            str.append(",");
        }
        if (str.length() > 0) 
            str.delete(str.length() - 1, str.length());
        manager.getWriter().println(Commands.USER_LIST);
        manager.getWriter().println(str.toString());
    }

    public String Name() {
        return name;
    }
    
    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }
}
