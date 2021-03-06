import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
	static int serverPort;
	static String filename;
	static boolean[] window;
	static int sendBase = 0, lastSent, windowMax, seqMax;
	static int MAX_SIZE = 1048;
	static String sn;
	static Timer timeout = new Timer();
	public static void main(String args[]) throws SocketException, IOException {
		int count = 0, ackNum;
		DatagramSocket clientSocket = new DatagramSocket();
		DatagramSocket serverSocket = new DatagramSocket(3000);
		InetAddress IpAddress = InetAddress.getByName("localhost");
		
		String q = "Qual devera ser o tamanho da janela? Digite um numero de 0 a 49"; //processo de query sobre o tamanho de janela desejado
		byte[] query = q.getBytes();
		DatagramPacket queryPacket = new DatagramPacket(query, query.length, IpAddress, 9876);
		clientSocket.send(queryPacket);
		byte[] answer = new byte[2];
		DatagramPacket answerPacket = new DatagramPacket(answer, answer.length);
		serverSocket.receive(answerPacket);
		String windowSize = new String(answerPacket.getData(), "UTF-8");
		windowMax = Integer.parseInt(windowSize);
		seqMax = windowMax*2;
		window = new boolean[windowMax];
		byte[] recData = new byte[2];
		byte[] sendData = new byte[MAX_SIZE];
		String ack;

		String filePath = "C:/Users/diani/Downloads/Alyssa.jpg";
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);

		int totLength = 0;

		while ((count = fis.read(sendData)) != -1) // calculate total length of file
		{
			totLength += count;
		}

		System.out.println("Total Length :" + totLength);

		int noOfPackets = totLength / MAX_SIZE;
		System.out.println("No of packets : " + noOfPackets);

		int off = noOfPackets * MAX_SIZE; // calculate offset. it total length of file is 1048 and array size is 1000
											// den starting position of last packet is 1001. this value is stored in
											// off.

		int lastPackLen = totLength - off;
		System.out.println("\nLast packet Length : " + lastPackLen);

		byte[] lastPack = new byte[lastPackLen]; // create new array without redundant information

		fis.close();

		FileInputStream fis1 = new FileInputStream(file);
		// while((count = fis1.read(sendData)) != -1 && (noOfPackets!=0))
		int sequenceNum = 0;
		boolean b = true;
		int confirmations = noOfPackets;
		// byte[] trusend = Arrays.copyOf(sendData, MAX_SIZE+5);
		for (int i = 0; i < windowMax; i++) {
			count = fis1.read(sendData);
			if (noOfPackets <= 0)
				break;
			send(sendData, sequenceNum, IpAddress);
			sequenceNum++;
			if (sequenceNum > seqMax)
				sequenceNum = 0;
			noOfPackets--;
		}
		while ((count = fis1.read(sendData)) != -1 || confirmations > 0) {

			recData = new byte[2];
			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			ackNum = -1;
			while (ackNum != sendBase && confirmations > 0) {// enquanto nao receber um ack respecitivo a base da janela, continue
				serverSocket.receive(recPacket); // receives ack
				ack = new String(recPacket.getData(), "UTF-8");// extracts acknumber
				ackNum = Integer.parseInt(ack);
				System.out.println("ACK RECEBIDO: " + ackNum);
				
				window[ackNum % windowMax] = true; // seta a casa do acknumber na janela como true(pacote # foi recebido)
				
				if (window[sendBase]) { //se o ack eh correspondente ao sendBase, atualiza o sendBase e envia novo pacote
					confirmations--;
					sendBase++;
					if (sendBase > windowMax-1) //se SendBase ultrapassar maior # de sequencia, resete sendBase
						sendBase = 0;

					if((count = fis1.read(sendData)) != -1)	
				{
					send(sendData, sequenceNum, IpAddress);
					window[sequenceNum % windowMax] = false;
					sequenceNum++;
				}
					
					if (sequenceNum > seqMax-1)
						sequenceNum = 0;
					noOfPackets--;
				}
				}
			}


		// check
		System.out.println("\nlast packet\n");
		System.out.println(new String(sendData));

		lastPack = Arrays.copyOf(sendData, lastPackLen-1);

		System.out.println("\nActual last packet\n");
		System.out.println(new String(lastPack));
		// send the correct packet now. but this packet is not being send.

		send(lastPack, sequenceNum--, IpAddress);
		ackNum = -1;
		while(!window[sendBase])
		{
			recData = new byte[2];
			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			serverSocket.receive(recPacket);
			ack = new String(recPacket.getData(), "UTF-8"); 
			ackNum = Integer.parseInt(ack);
			System.out.println("ACK RECEBIDO: " + ackNum);

			window[ackNum % windowMax] = true;
		}



	}

	public static byte[] mountPacket(byte[] sendData, int sequenceNum) {
		String seqData;
		byte checkSum = 0;
		if (sequenceNum < 10)
			seqData = "0" + sequenceNum;
		else
			seqData = "" + sequenceNum;
		byte[] seq = seqData.getBytes();
		sn = seqData;
		sendData = Arrays.copyOf(sendData, MAX_SIZE + 3);
		sendData[MAX_SIZE] = seq[0];
		sendData[MAX_SIZE + 1] = seq[1];
		for (int i = 0; i < MAX_SIZE + 2; i++) {
			checkSum = (byte) (checkSum + sendData[i]);
		}
		sendData[MAX_SIZE + 2] = checkSum;
		System.out.println("CHECKSUM DO PACOTE " + sequenceNum + ":             " + checkSum);
		return sendData;

	}

	public static void send(byte[] sendData, int seqNum, InetAddress IpAddress) throws SocketException, IOException {
		DatagramSocket clientSocket = new DatagramSocket();
		sendData = mountPacket(sendData, seqNum);
		System.out.println(new String(sendData));
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IpAddress, 9876);
		clientSocket.send(sendPacket);
		timeout.schedule(new PacketTimeout(seqNum, sendData, IpAddress), 3500);
		window[seqNum % windowMax] = false;
		System.out.println(sendPacket.hashCode());
		System.out.println("========");
		System.out.println("last pack sent     " + new String(sendPacket.getData()));
		clientSocket.close();
	}

	static class PacketTimeout extends TimerTask {
		private int seq;
		private byte[] message;
		private InetAddress IpAddress;

		public PacketTimeout(int seq, byte[] message, InetAddress IpAddress) {
			this.seq = seq;
			this.message = message;
			this.IpAddress = IpAddress;
		}

		public void run() {
			try {
				if (window[seq % windowMax] == false) {
					System.out.println("**PACKET TIMEOUT (seq: " + seq + ")**");

					try {
						send(this.message, this.seq, this.IpAddress);
					} catch (Exception e) {
					}
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

	}
}
