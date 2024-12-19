package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class clienteChat {

    static String dir = "127.0.0.1";
    public static void main(String[] args) {
        int port = 9931;
        String username;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(System.in,"ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        int interfazSeleccionada = -1; // Índice real de la interfaz seleccionada

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
        NetworkInterface ni = null;
        try {
            ni = NetworkInterface.getByIndex(interfazSeleccionada);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        if (ni == null) {
            System.out.println("El índice ingresado no corresponde a ninguna interfaz.");
            return;
        }

        try {
            InetAddress dst = InetAddress.getByName(dir);
            br = new BufferedReader(new InputStreamReader(System.in));

            try (MulticastSocket cl = new MulticastSocket(port)) {
                cl.setReuseAddress(true);
                cl.setTimeToLive(255);
                String dir= "231.1.1.1";
                InetAddress gpo = InetAddress.getByName(dir);
                SocketAddress dirm;
                System.out.println("Ingresa tu nombre de usuario:");
                username = br.readLine();
                try{
                    dirm = new InetSocketAddress(gpo,port);
                }catch(Exception e){
                    e.printStackTrace();
                    return;
                }
                cl.joinGroup(dirm,ni);
                System.out.println("Socket unido al grupo "+gpo);

                Recibe r = new Recibe(cl, username);
                Envia e = new Envia(cl, br, username);
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
        } catch (UnknownHostException e) {
            System.err.println("Host invalido");
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
        System.out.printf("\n");
    }
}



