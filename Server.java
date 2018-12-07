import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
	public static void main(String args[]) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] recData = new byte[1024];
		int i = 0;

		FileOutputStream file = new FileOutputStream(
				"D:/Users/cdbm/Documents/testing-received.7z");
		


		while (true) {
			

			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			serverSocket.receive(recPacket);
			file.write(recPacket.getData());
			System.out.println("\nPacket" + ++i + " written to file\n");
			file.flush();
		}
	}
}
