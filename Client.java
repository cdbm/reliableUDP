import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
public class Client {
	public static void main(String args[]) throws IOException {
		Scanner in = new Scanner(System.in);
		DatagramSocket serverSocket = new DatagramSocket(9876);
		DatagramSocket clientSocket = new DatagramSocket();
		byte[] recData = new byte[1050];
		byte[][] janela = new byte[10][1048];
		boolean[] recebidos;
		int base = 0, i =0, windowMax, seqMax, percentage;
		Random num = new Random();
		InetAddress IpAddress = InetAddress.getByName("localhost");
		
		byte[] query = new byte[100];
		DatagramPacket queryPacket = new DatagramPacket(query, query.length);
		clientSocket.receive(queryPacket);
		String question = new String (queryPacket.getData(), "UTF-8");
		System.out.println(question);
		windowMax = -1;
		windowMax = in.nextInt();
		while(windowMax < 1 || windowMax > 99) {
			System.out.println("Numero invalido, tente novamente");
			windowMax = in.nextInt();
		}
		String resposta = Integer.toString(windowMax);
		byte[] answer = resposta.getBytes();
		DatagramPacket answerPacket = new DatagramPacket(answer, answer.length, IpAddress, 3000);
		clientSocket.send(answerPacket);
		recebidos = new boolean[windowMax];
		seqMax = windowMax*2;
		System.out.println("Escolha o percentual de erro do modulo de descarte, digitando um valor de 0 a 99");
		percentage = in.nextInt();
		while(percentage < 0 || percentage > 99) {
			System.out.println("Numero invalido, tente novamente");
			percentage = in.nextInt();
		}
		
		FileOutputStream file = new FileOutputStream("C:/Users/diani/Downloads/Alyssa-received.jpg");
		while (true) {
			recData = new byte[1050];
			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			serverSocket.receive(recPacket);
			String x = getSeq(recPacket);
			byte[] ack = x.getBytes();
			// módulo de descarte
			int g = num.nextInt(100);
			int nseq = Integer.parseInt(x);
			System.out.println(g);
			if (g > percentage) {
				System.out.println("sequencia recebida    " + x);
				if (base == nseq % windowMax) {
					file.write(Arrays.copyOfRange(recPacket.getData(), 0, 1048));
					//System.out.println("escrito base" + nseq);
					for (int j = (base + 1) % windowMax; j < windowMax; j++) {
						//System.out.println("entrou");
						if (recebidos[j]) {
							file.write(janela[j]);
							//System.out.println("escrito outro" + nseq);
							recebidos[j] = false;
						} else {
							base = j;
							break;
						}
					}
					//System.out.println(base);
				} else {
					for (int k = 0; k < 1048; k++) {
						janela[nseq % windowMax][k] = recData[k];
						recebidos[nseq % windowMax] = true;
					}
				}
				// System.out.println("\nPacket" + ++i + " written to file\n");
				DatagramPacket sendPacket = new DatagramPacket(ack, ack.length, IpAddress, 3000);
				clientSocket.send(sendPacket);
				file.flush();
				// System.out.println(ack.length);
				String w = new String(sendPacket.getData());
				//System.out.println(w);

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
}