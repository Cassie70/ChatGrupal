package cliente;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

class Recibe extends Thread{
    MulticastSocket socket;
    String username;
    public Recibe(MulticastSocket m, String username){
        this.socket=m;
        this.username=username;
    }
    public void run(){
        try{

            for(;;){
                DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                System.out.println("Listo para recibir mensajes...");
                socket.receive(p);
                String msj = new String(p.getData(),0,p.getLength());
                JsonObject json = new JsonObject(msj);
                if(json.containsKey("username") && json.containsKey("content") && !json.get("username").equals(username)){
                    System.out.println(json.get("username")+": "+json.get("content"));
                }
            } //for
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//run
}//class