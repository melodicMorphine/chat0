package chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Screen extends JFrame {

    private final JFrame frame = new JFrame();
    private final JTextArea taeditor = new JTextArea();
    private final JTextArea taview = new JTextArea();
    private final JList liuser = new JList();
    private PrintWriter writer;
    private BufferedReader reader;
    private JScrollPane scrollTaView = new JScrollPane(taview);

    public Screen() {
        frame.setTitle("Chat");
        frame.setLayout(new BorderLayout());
        taview.setEditable(false);

        liuser.setBackground(Color.DARK_GRAY);
        liuser.setForeground(Color.white);
        taeditor.setBackground(Color.LIGHT_GRAY);
        taview.setBackground(Color.GRAY);
        taview.setForeground(Color.white);

        liuser.setPreferredSize(new Dimension(80, 140));
        taeditor.setPreferredSize(new Dimension(400, 40));
        frame.setSize(600, 400);

        frame.add(taeditor, BorderLayout.SOUTH);
        frame.add(scrollTaView, BorderLayout.CENTER);
        frame.add(new JScrollPane(liuser), BorderLayout.WEST);

        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        String[] users = new String[]{""};
        this.fillListUser(users);
    }
    
    private void fillListUser(String[] users) {
        DefaultListModel model = new DefaultListModel();
        liuser.setModel(model);
        for (String user : users) {
            model.addElement(user);
        }
    }

    private void startWriter() {
        taeditor.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // escrevendo para o servidor
                    if (taview.getText().isEmpty()) {
                        return;
                    }
                    Object user = liuser.getSelectedValue();
                    if (user != null) {
                        taview.append("Eu: ");
                        taview.append(taeditor.getText());
                        taview.append("\n");
                        writer.println(Commands.MESSAGE + user);
                        writer.println(taeditor.getText());
                        taeditor.setText("");
                        e.consume();
                    } else {
                        if (taview.getText().equalsIgnoreCase(Commands.SAIR)) {
                            System.exit(0);
                        }
                        JOptionPane.showMessageDialog(Screen.this, "Selecione um Usuário: ");
                        return;
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    private void startReader() {
        // lendo mensagens do servidor
        try {
            while (true) {
                String message = reader.readLine();
                if (message == null || message.length() == 0) {
                    continue;
                }
                if (message.startsWith(Commands.USER_LIST)) {
                    String[] users = reader.readLine().split(",");
                    fillListUser(users);
                } else if (message.equals(Commands.LOGIN)) {
                    String login = JOptionPane.showInputDialog("Nome: ");
                    writer.println(login);
                } else if (message.equals(Commands.LOGIN_DENIED)) {
                    JOptionPane.showMessageDialog(Screen.this, "Login Inválido");
                } else if (message.equals(Commands.LOGIN_ACCEPTED)) {
                    updateListUsers();
                } else {
                    taview.append(message);
                    taview.append("\n");
                    taview.setCaretPosition(taview.getDocument().getLength());
                }
            }
        } catch (IOException ex) {
            System.out.println("Impossível Ler Mensagem");
        }
    }

    private void updateListUsers() {
        writer.println(Commands.USER_LIST);
    }

    public void startChat() {
        try {
            final Socket client = new Socket("127.0.0.1", 9999);
            writer = new PrintWriter(client.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (UnknownHostException ex) {
            System.out.println("Endereço Inválido");
        } catch (IOException ex) {
            System.out.println("Servidor Fora do Ar");
        }
    }

    public static void main(String[] args) {
        final Screen client = new Screen();
        client.startChat();
        client.startWriter();
        client.startReader();
    }
}
