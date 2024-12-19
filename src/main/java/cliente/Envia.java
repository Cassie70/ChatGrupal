package cliente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;

class Envia extends Thread {
    MulticastSocket socket;
    BufferedReader br;
    static String username;
    static HashSet<String> users;

    public Envia(MulticastSocket m, BufferedReader br, String username, HashSet<String> users) {
        this.socket = m;
        this.br = br;
        Envia.username = username;
        Envia.users = users;
    }

    public void run() {
        try {
            String dir = "231.1.1.1";
            String dir6 = "ff3e::1234:1";
            int pto = 9931;
            InetAddress gpo = InetAddress.getByName(dir);
            for (;;) {
                String msg = br.readLine();
                if (msg.equals("/users")) {
                    printUsers();
                    continue;
                }
                if(msg.equals("/leave")){
                    Json json = new Json();
                    json.put("type", "leave").put("username", username);
                    socket.send(new DatagramPacket(json.toString().getBytes(), json.toString().getBytes().length, gpo, pto));
                    System.exit(0);
                }
                if (msg.startsWith("/sendfile")) {
                    if (msg.split(" ").length < 2) {
                        System.out.println("Error: Se debe proporcionar una ruta de archivo.");
                        return;
                    }

                    String filePath = msg.split(" ", 2)[1].replace("\"", "");
                    sendFile(filePath, username, socket, gpo, pto);
                }
                if (msg.startsWith("/msg")) {

                    int firstSpace = msg.indexOf(' ', 5);
                    if (firstSpace == -1) {
                        System.out.println("Error: Se debe proporcionar un destinatario y un mensaje.");
                        continue;
                    }

                    String destination = msg.substring(5, firstSpace);

                    String message = msg.substring(firstSpace + 1).trim();

                    if (message.startsWith("\"") && message.endsWith("\"")) {
                        message = message.substring(1, message.length() - 1).trim();
                    } else {
                        System.out.println("Error: El mensaje debe ir entre comillas.");
                        continue;
                    }

                    sendMessage(username, socket, gpo, pto, message, destination);
                    continue;
                }

                sendMessage(username, socket, gpo, pto, msg, "all");

            }
        } catch (Exception e) {
            System.err.println("Error al leer el mensaje: " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    private static void sendMessage(String username, MulticastSocket cl, InetAddress dst, int port, String msg, String dest) {
        Json json = new Json();

        json.put("type", "msg").put("username", username).put("destination", dest).put("content", msg);
        byte[] buffer = json.toString().getBytes();

        DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, dst, port);
        if (json.get("type").equals("leave")) {
            System.out.println("Saliendo del chat...");
        }
        try {
            cl.send(requestPacket);
        } catch (IOException e) {
            System.err.println("Error al enviar el paquete: " + e.getMessage());
        }
    }


    private static void printUsers() {
        System.out.println("Usuarios conectados:");
        for (String user : users) {
            System.out.println(user);
        }
    }

    public void sendUsersList(String username) {
        Json json = new Json();
        json.put("type", "listUsers").put("username", username).put("users", new ArrayList<>(users));
        byte[] buffer = json.toString().getBytes();

        DatagramPacket requestPacket;
        try {
            requestPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("231.1.1.1"), 9931);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        try {
            socket.send(requestPacket);
        } catch (IOException e) {
            System.err.println("Error al enviar la lista de usuarios: " + e.getMessage());
        }
    }

    private static void sendFile(String filePath, String username, MulticastSocket socket, InetAddress dst, int port) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());

                String base64File = Base64.getEncoder().encodeToString(fileData);

                Json json = new Json();
                json.put("type", "file")
                        .put("username", username)
                        .put("destination", "all")
                        .put("content", base64File)
                        .put("filename", file.getName());

                byte[] buffer = json.toString().getBytes();
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, dst, port);
                System.out.println(json);
                socket.send(requestPacket);

                System.out.println("Archivo enviado: " + filePath);
            } catch (IOException e) {
                System.err.println("Error al leer o enviar el archivo: " + e.getMessage());
            }
        } else {
            System.out.println("Archivo no encontrado: " + filePath);
        }
    }

}


