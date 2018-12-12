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
		byte[][] janela = new byte[10][1048];
		boolean[] recebidos = new boolean[10];
		int base = 0;
		int i = 0;
		Random num = new Random();
		InetAddress IpAddress = InetAddress.getByName("localhost");

		FileOutputStream file = new FileOutputStream("C:/Users/diani/Downloads/Alyssa-received.jpg");
		while (true) {
			recData = new byte[1051];
			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			serverSocket.receive(recPacket);
			if (checkSum(recPacket.getData())) {
				String x = getSeq(recPacket);
				byte[] ack = x.getBytes();
				// módulo de descarte
				int g = num.nextInt(100);
				int nseq = Integer.parseInt(x);
				System.out.println(g);
				if (g > 30) {
					System.out.println("sequencia recebida    " + x);
					if (base == nseq % 10) {
						file.write(Arrays.copyOfRange(recPacket.getData(), 0, 1048));
						// System.out.println("escrito base" + nseq);
						for (int j = (base + 1) % 10; j < 10; j++) {
							// System.out.println("entrou");
							if (recebidos[j]) {
								file.write(janela[j]);
								// System.out.println("escrito outro" + nseq);
								recebidos[j] = false;
							} else {
								base = j;
								break;
							}
						}
						// System.out.println(base);
					} else {
						for (int k = 0; k < 1048; k++) {
							janela[nseq % 10][k] = recData[k];
							recebidos[nseq % 10] = true;
						}
					}
					// System.out.println("\nPacket" + ++i + " written to file\n");
					DatagramPacket sendPacket = new DatagramPacket(ack, ack.length, IpAddress, 3000);
					clientSocket.send(sendPacket);
					file.flush();
					// System.out.println(ack.length);
					String w = new String(sendPacket.getData());
					// System.out.println(w);

				}
			}
		}
	}

	public static String getSeq(DatagramPacket recPacket) {
		byte[] seq = new byte[2];
		seq[0] = recPacket.getData()[1048];
		seq[1] = recPacket.getData()[1049];
		String x = new String(seq);

		return x;
	}

	public static boolean checkSum(byte[] data) {
		byte checkSum = 0;
		for (int i = 0; i < 1050; i++) {
			checkSum = (byte) (checkSum + data[i]);
		}
		if (checkSum == data[1050]) {
			return true;
		} else {
			System.out.println("Pacote Corrompido");
			return false;
		}

	}
}