package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

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

        if(msg.charAt(0) == '/'){
            handleCommand(msg);
        }

        JsonObject.put("username",username);
        JsonObject.put("type","msg");
        JsonObject.put("content",msg);
        JsonObject.printJson();
        byte[] buffer = JsonObject.getJson().getBytes();
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

    private static void handleCommand(String msg) {
    }


}



