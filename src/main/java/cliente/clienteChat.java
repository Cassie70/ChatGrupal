package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class clienteChat {

    public static void main(String[] args) {
        int port = 9931;
        String username;
        HashSet<String> users = new HashSet<>();

        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.ISO_8859_1));
        Enumeration<NetworkInterface> nets;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        int interfazSeleccionada; // Índice real de la interfaz seleccionada

        for (NetworkInterface netint : Collections.list(nets)) {
            int indiceReal = netint.getIndex(); // Índice real del sistema
            System.out.printf("[Interfaz %d]:\n", indiceReal);
            try {
                despliegaInfoNIC(netint);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.print("\nElige el índice de la interfaz multicast (tal como se muestra): ");
        try {
            interfazSeleccionada = Integer.parseInt(br.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        NetworkInterface ni;
        try {
            ni = NetworkInterface.getByIndex(interfazSeleccionada);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        if (ni == null) {
            System.out.println("El índice ingresado no corresponde a ninguna interfaz.");
            return;
        }

        br = new BufferedReader(new InputStreamReader(System.in));
        try (MulticastSocket cl = new MulticastSocket(port)) {
            cl.setReuseAddress(true);
            cl.setTimeToLive(255);
            String dir= "231.1.1.1";
            InetAddress gpo = InetAddress.getByName(dir);
            SocketAddress dirm;
            System.out.println("Ingresa tu nombre de usuario:");
            while (true) {
                username = br.readLine();
                if (username.contains(" ")) {
                    System.out.println("El nombre de usuario no puede contener espacios. Inténtalo de nuevo:");
                } else {
                    break;
                }
            }
            try{
                dirm = new InetSocketAddress(gpo,port);
            }catch(Exception e){
                System.err.println("Error al crear el socket: "+e.getMessage());
                return;
            }
            cl.joinGroup(dirm,ni);
            System.out.println("Socket unido al grupo "+gpo);
            Json jsonJoin = new Json();
            jsonJoin.put("type", "join").put("username", username);
            cl.send(new DatagramPacket(jsonJoin.toString().getBytes(), jsonJoin.toString().getBytes().length, gpo, port));
            Json rqUsers = new Json();
            rqUsers.put("type", "users").put("username", username);
            cl.send(new DatagramPacket(rqUsers.toString().getBytes(), rqUsers.toString().getBytes().length, gpo, port));
            Envia e = new Envia(cl, br, username, users);
            Recibe r = new Recibe(cl, username, users, e);
            e.setPriority(10);
            r.start();
            e.start();
            try {
                r.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            try {
                e.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

        } catch (IOException e) {
            System.err.println("Error al leer el mensaje: "+e.getMessage());
        }
    }

    static void despliegaInfoNIC(NetworkInterface netint) throws SocketException {
        System.out.printf("Nombre de despliegue: %s\n", netint.getDisplayName());
        System.out.printf("Nombre: %s\n", netint.getName());
        String multicast = (netint.supportsMulticast())?"Soporta multicast":"No soporta multicast";
        System.out.printf("Multicast: %s\n", multicast);
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("Direccion: %s\n", inetAddress);
        }
        System.out.print("\n");
    }
}



