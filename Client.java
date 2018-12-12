import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;

public class Client {
	public static void main(String args[]) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(9876);
		DatagramSocket clientSocket = new DatagramSocket();
		byte[] recData = new byte[1050];
		int i = 0;
		Random num = new Random();
		InetAddress IpAddress = InetAddress.getByName("localhost");

		FileOutputStream file = new FileOutputStream("C:/Users/diani/Downloads/Lista_2-received");
		while (true) {
			recData = new byte[1050];
			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			serverSocket.receive(recPacket);
			String x  = getSeq(recPacket);
			byte[] ack = x.getBytes();
			// módulo de descarte
			int g = num.nextInt(100);
			// System.out.println(g);
			if (g > -1) {
				file.write(Arrays.copyOfRange(recPacket.getData(), 0, 1048));
				// System.out.println("\nPacket" + ++i + " written to file\n");
				DatagramPacket sendPacket = new DatagramPacket(ack, ack.length, IpAddress, 3000);
				clientSocket.send(sendPacket);
				file.flush();
				
			}

		}
	}

	public static String getSeq(DatagramPacket recPacket) {
		byte[] seq = new byte[2];
		seq[0] = recPacket.getData()[1048];
		seq[1] = recPacket.getData()[1049];
		String x = new String(seq);

		System.out.println("sequencia recebida    " + x);
		return x;
	}
}