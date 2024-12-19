package cliente;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;

class Recibe extends Thread{
    MulticastSocket socket;
    String username;
    HashSet<String> users;
    Envia enviaThread;
    public Recibe(MulticastSocket m, String username, HashSet<String> users, Envia enviaThread){
        this.socket=m;
        this.username=username;
        this.users = users;
        this.enviaThread = enviaThread;
    }
    public void run(){
        try{

            for (;;) {
                DatagramPacket p = new DatagramPacket(new byte[65535], 65535);
                socket.receive(p);

                String msj = new String(p.getData(), 0, p.getLength());

                Json json = new Json(msj);

                if (json.containsKey("type") && json.containsKey("username")) {
                    handleUserMessages(json);
                }

                if (json.containsKey("type") && json.containsKey("username") && json.containsKey("content")) {
                    handleFileMessages(json);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//run

    private void handleUserMessages(Json json) {
        String username = (String) json.get("username");

        switch ((String) json.get("type")) {
            case "join":
                handleJoin(username);
                break;
            case "leave":
                handleLeave(username);
                break;
            case "users":
                handleUsers(json);
                break;
            case "listUsers":
                handleListUsers(json);
                break;
                case "msg":
                if(!username.equals(this.username)){
                    if(json.get("destination").equals("all")){
                        System.out.println(username + ": " + json.get("content"));
                    }else if(json.get("destination").equals(this.username)){
                        System.out.println(username + " (susurro): " + json.get("content"));
                    }
                }
        }
    }

    private void handleJoin(String newUser) {
        users.add(newUser);
        System.out.println(newUser + " se ha unido al chat.");
    }

    private void handleLeave(String user) {
        users.remove(user);
        System.out.println(user + " ha abandonado el chat.");
    }

    private void handleUsers(Json json) {
        if (!json.get("username").equals(username)) {
            enviaThread.sendUsersList(json.get("username").toString());
        }
    }

    private void handleListUsers(Json json) {
        if (json.get("username").equals(username)) {
            List<String> listUsers = (List<String>) json.get("users");
            users.addAll(listUsers);
        }
    }

    private void handleFileMessages(Json json) {
        if (json.get("type").equals("file") && !json.get("username").equals(username)) {
            String filename = (String) json.get("filename");
            String base64Content = (String) json.get("content");

            saveFile(filename, base64Content);
        }
    }

    private void saveFile(String filename, String base64Content) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
        File file = new File(filename);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decodedBytes);
            System.out.println("Archivo recibido: " + filename);
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
        }
    }

}//class