import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
	public static void main(String args[]) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] recData = new byte[1024];
		int i = 0;

		FileWriter file = new FileWriter(
				"D:/Users/cdbm/Documents/testing-received.7z");
		PrintWriter out = new PrintWriter(file);

		// BufferedOutputStream bos = new BufferedOutputStream(fos);

		while (true) {
			// PrintWriter out = new PrintWriter(file);

			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			serverSocket.receive(recPacket);
			String line = new String(recPacket.getData());
			System.out.println("\n Data: " + line);
			out.println(line);
			System.out.println("\nPacket" + ++i + " written to file\n");
			out.flush();
		}
	}
}
