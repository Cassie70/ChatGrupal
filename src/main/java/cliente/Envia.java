package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Envia extends Thread{
    MulticastSocket socket;
    BufferedReader br;
    String username;

    public Envia(MulticastSocket m, BufferedReader br, String username){
        this.socket=m;
        this.br=br;
        this.username=username;

    }
    public void run(){
        try{
            String dir = "231.1.1.1";
            String dir6 = "ff3e::1234:1";
            int pto=9931;
            InetAddress gpo = InetAddress.getByName(dir);

            for(;;){
                System.out.println("Escribe un mensaje para ser enviado:");
                String msg= br.readLine();
                sendMessage(username,socket,gpo,pto,msg);
            }//for
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//run

    private static void sendMessage(String username, MulticastSocket cl, InetAddress dst, int port, String msg) {

        JsonObject json = new JsonObject();

        if(msg.charAt(0) == '/'){
            handleCommand(msg,json);
        }else {
            json.put("type", "msg").put("username", username).put("destination", "all").put("content", msg);
        }
        byte[] buffer = json.toString().getBytes();

        DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, dst, port);

        try {
            cl.send(requestPacket);
        }catch (IOException e){
            System.err.println("Error al enviar el paquete: "+e.getMessage());
        }
    }

    private static  void handleCommand(String msg, JsonObject json) {

        String regex = "^/[a-z]+ +\"([^\"]+)\"$";

        HashSet<String> commandList = new HashSet<>();
        commandList.add("upload");
        commandList.add("msg");
        commandList.add("audio");
        //commandList.add("private");

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(msg);

        if (matcher.matches()) {
            if (matcher.groupCount() != 1) {
                System.out.println("Comando invalido: " + msg);
                return;
            }
            String command = matcher.group(0);
            String argument = matcher.group(1);

            if(commandList.contains(command)){
                json.put("command", List.of(command.substring(1),argument));

            }

        } else {
            System.out.println("Entrada inválida: " + msg);

        }
    }


}//class
