package chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    TextField msgField, usernameField;

    @FXML
    TextArea msgArea;

    @FXML
    HBox loginPanel, msgPanel;

    @FXML
    ListView <String> clientsList;

    @FXML
    Button loBtn;



    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

   public void setUsername(String username){
       this.username = username;
       if(username != null){
           loginPanel.setVisible(false);
           loginPanel.setManaged(false);
           msgPanel.setVisible(true);
           msgPanel.setManaged(true);
           clientsList.setVisible(true);
           clientsList.setManaged(true);
           loBtn.setVisible(true);
           loBtn.setManaged(true);

       }
       else{
           loginPanel.setVisible(true);
           loginPanel.setManaged(true);
           msgPanel.setVisible(false);
           msgPanel.setManaged(false);
           clientsList.setVisible(false);
           clientsList.setManaged(false);
           loBtn.setVisible(false);
           loBtn.setManaged(false);
       }

    }

    public void login() {

       if(socket == null || socket.isClosed()){
           connect();
       }
        try {
            out.writeUTF("/login " + usernameField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(usernameField.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nickname cannot be empty!");
            alert.showAndWait();
            return;
        }
    }

    public void logout(){
        try {
            out.writeUTF("/logout ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        login();
        msgArea.clear();
        usernameField.clear();



    }

    private void connect() {

       try {
          socket = new Socket("localHost", 8189);
          in = new DataInputStream(socket.getInputStream());
          out = new DataOutputStream(socket.getOutputStream());

           Thread thread = new Thread(() -> {
               try {
                   while (true){
                       String msg = in.readUTF();
                       if(msg.startsWith("/login_ok ")){
                           setUsername(msg.split(" ")[1]);
                           break;
                       }
                       if(msg.startsWith("/login_failed ")){
                           String couse = msg.split(" ", 2)[1];
                           msgArea.appendText(couse + "/n");
                       }
                   }
                   while (true){
                       String msg = in.readUTF();
                       if(msg.startsWith("/")){
                           if(msg.startsWith("/clients_list ")){
                               String[] tokens = msg.split("\\s");

                               Platform.runLater(()->{
                                   clientsList.getItems().clear();
                                   for (int i = 1; i < tokens.length; i++) {
                                       clientsList.getItems().add(tokens[i]);

                                   }
                               });
                           }
                           continue;
                       }
                       msgArea.appendText(msg + "\n");
                   }
               }catch (IOException e){
                   e.printStackTrace();
               } finally {
                   disconnect();
               }
           });
           thread.start();

       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    public void disconnect(){
       setUsername(null);
        try {
            if(socket !=null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Unable to send message", ButtonType.OK);
            alert.showAndWait();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUsername(null);
    }
}

