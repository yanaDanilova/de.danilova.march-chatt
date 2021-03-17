import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;

        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/login ")) {
                        String usernameFromLogin = msg.split(" ")[1];

                        if (server.isNickBusy(usernameFromLogin)) {
                            sendMessage("/login_failed Current nickname is busy.");
                            continue;
                        }

                        username = usernameFromLogin;
                        sendMessage("/login_ok " + username);
                        server.subscribe(this);
                        break;

                    }
                }
                while (true) {
                    String msg = in.readUTF();
                    if(msg.startsWith("/")){
                        executeCommand(msg);
                        continue;
                    }
                    server.broadcast(username + ":" + msg + "\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    private void executeCommand(String cmd)  {
        if(cmd.startsWith("/w ")){
            String[] tokens = cmd.split("\\s",3);
            server.sendPrivateMsg(this, tokens[1], tokens[2]);
        }
        if(cmd.startsWith("/logout ")){
            disconnect();
        }
    }

    public void sendMessage(String message)  {
           try{
               out.writeUTF(message);
           }catch (IOException e){
               disconnect();
           }
        }

        public void disconnect(){
        server.unsubscribe(this);
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
