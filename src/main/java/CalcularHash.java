import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

public class CalcularHash {

    public static void main(String[] args) {
        try {
            File file = new File("C:\\Users\\Cass\\Desktop\\ChatGrupal\\bolas.jpg");
            String checksum = getFileChecksum(file);
            System.out.println("Checksum SHA-256: " + checksum);
        } catch (Exception e) {
            System.err.println("Error al calcular el checksum: " + e.getMessage());
        }
    }


    public static String getFileChecksum(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] byteArray = Files.readAllBytes(file.toPath());
        byte[] hashBytes = digest.digest(byteArray);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
