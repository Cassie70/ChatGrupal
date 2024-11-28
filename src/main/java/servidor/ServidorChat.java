package servidor;

import cliente.JsonObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServidorChat {

    public static void main(String[] args) {
        int port = 1234;

        try(DatagramSocket s = new DatagramSocket(port)){
            s.setReuseAddress(true);
            System.out.println("Servidor inciado esperando datagramas...");

            String msg;

            while (true) {
                byte[] b = new byte[65535];
                DatagramPacket p = new DatagramPacket(b,b.length);
                try{
                    s.receive(p);
                    msg = new String(p.getData(),0,p.getLength());
                    System.out.println("se ha recibido datagrama desde"+p.getAddress()+":"+p.getPort());
                    System.out.println(msg);
                    s.send(p);

                    JsonObject json = new JsonObject(msg);
                    System.out.println("json recibido: "+ json);
                    System.out.println("del usuario"+ json.get("username"));
                }catch (IOException e){
                    System.err.println("Error al recibir el paquete: "+e.getMessage());
                }
            }
        }catch (SocketException e){
            System.err.println("Error al iniciar el servidor: "+ e.getMessage());
        }
    }
}
