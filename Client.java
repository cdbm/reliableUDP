import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

public class Client {
	public static void main(String args[]) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] recData = new byte[1024];
		int i = 0;
		Random num = new Random();

		FileOutputStream file = new FileOutputStream("C:/Users/C. Davi/Documents/Lista_2-received.txt");

		while (true) {

			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			serverSocket.receive(recPacket);
			// módulo de descarte
			int g = num.nextInt(100);
			System.out.println(g);
			if (g > 50) {
				file.write(recPacket.getData());
				System.out.println("\nPacket" + ++i + " written to file\n");
				file.flush();
			}

		}
	}
}
