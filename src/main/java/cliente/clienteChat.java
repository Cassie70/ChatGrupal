package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class clienteChat {

    static String dir = "127.0.0.1";
    public static void main(String[] args) {
        int port = 1234;
        String username;
        try {
            InetAddress dst = InetAddress.getByName(dir);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            try (DatagramSocket cl = new DatagramSocket()) {

                System.out.println("Ingresa tu nombre de usuario:");
                username = br.readLine();
                while (true) {
                    System.out.println(username+" Escribe un mensaje, <Enter> para enviar, \"/salir\" para terminar");
                    String msj = br.readLine();
                    if (msj.compareToIgnoreCase("/salir") == 0) {
                        System.out.println("fin");
                        br.close();
                        cl.close();
                        System.exit(0);
                    } else {

                        sendMessage(username,cl, dst, port, msj);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error al leer el mensaje: "+e.getMessage());
            }
        } catch (UnknownHostException e) {
            System.err.println("Host invalido");
        }
    }


    private static void sendMessage(String username,DatagramSocket cl, InetAddress dst, int port, String msg) {

        Json json = new Json();
        if(msg.charAt(0) == '/'){
            handleCommand(msg, json);
        }

        json.put("username",username).put("content", msg);

        byte[] buffer = json.toString().getBytes();

        DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, dst, port);

        try {
            cl.send(requestPacket);
        }catch (IOException e){
            System.err.println("Error al enviar el paquete: "+e.getMessage());
        }
        byte[] responseBuffer = new byte[65535];
        DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
        try {
            cl.receive(responsePacket);
        }catch (IOException e){
            System.err.println("Error al recibir el paquete: "+e.getMessage());
        }

        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        System.out.println("Respuesta del servidor: \n" + response);
    }

    private static void handleCommand(String msg, Json json) {

        HashSet<String> commands = new HashSet<>();
        commands.add("upload");
        commands.add("msg");
        commands.add("audio");

        String[] commmandParts = splitCommand(msg);
        System.out.println(Arrays.toString(commmandParts));

        if(commmandParts.length > 0){
            if(commands.contains(commmandParts[0])){
                String command = commmandParts[0];
                json
                        .put("command", new Json()
                                .put("name", commmandParts[0])
                                .put("params", List.of(Arrays.copyOfRange(commmandParts,1, commmandParts.length))));
            }
        }
    }

    static String[] splitCommand(String command) {

        List<String> parts = new ArrayList<>();
        int state = 0;
        StringBuilder part = new StringBuilder();

        for (int i = 1; i < command.length(); i++) {
            char c = command.charAt(i);

            switch (state) {
                case 0: // Outside quotes
                    if (c == '"') {
                        state = 1;
                    } else if (c == ' ') {
                        if (!part.isEmpty()) {
                            parts.add(part.toString());
                            part.setLength(0);
                        }
                    } else {
                        part.append(c);
                    }
                    break;

                case 1: // Inside quotes
                    if (c == '"') {
                        state = 0;
                    } else {
                        part.append(c);
                    }
                    break;

                default:
                    throw new IllegalStateException("Invalid state: " + state);
            }
        }

        if (!part.isEmpty()) {
            parts.add(part.toString());
        }

        return parts.toArray(new String[0]);
    }

}



