package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            System.out.println("Iniciando Servidor...");
            server = new ServerSocket(9999);
            System.out.println("Servidor Iniciado");
            while(true) {
                Socket client = server.accept();
                new Manager(client);
            }
        } catch (IOException ex) {
            try {if (server != null) server.close();} catch (IOException ex1) {}
            System.err.println("Porta Ocupada / Servidor Fechado");
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
