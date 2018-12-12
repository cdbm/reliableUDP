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
	static boolean[] window = new boolean[10];
	static int sendBase = 0, lastSent;
	static int MAX_SIZE = 1048;
	static String sn;
	static Timer timeout;

	public static void main(String args[]) throws SocketException, IOException {
		int count = 0, ackNum;
		DatagramSocket clientSocket = new DatagramSocket();
		DatagramSocket serverSocket = new DatagramSocket(3000);
		byte[] recData = new byte[2];
		byte[] sendData = new byte[MAX_SIZE];
		String ack;

		String filePath = "C:/Users/diani/Downloads/Lista_2.txt";
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
		
		// byte[] trusend = Arrays.copyOf(sendData, MAX_SIZE+5);
		for (int i = 0; i < 10; i++) {
			if (noOfPackets <= 0)
				break;
			send(sendData, sequenceNum);
			sequenceNum++;
			if (sequenceNum > 20)
				sequenceNum = 0;
			noOfPackets--;
		}
		while ((count = fis1.read(sendData)) != -1) {
			if (noOfPackets <= 0)
				break;
			recData = new byte[2];
			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
			if (noOfPackets <= 0)
				break;
			serverSocket.receive(recPacket);
			ack = new String(recPacket.getData(), "UTF-8");
			ackNum = Integer.parseInt(ack);
			window[ackNum % 10] = true;
			if (ackNum == sendBase) {
				sendBase++;
				if(sendBase > 20)
						sendBase= 0;
				send(sendData, sequenceNum);
				sequenceNum++;
				if(sequenceNum > 20)
						sequenceNum++;
				noOfPackets--;
			}

		}

		// check
		System.out.println("\nlast packet\n");
		System.out.println(new String(sendData));

		lastPack = Arrays.copyOf(sendData, lastPackLen - 1);

		System.out.println("\nActual last packet\n");
		System.out.println(new String(lastPack));
		// send the correct packet now. but this packet is not being send.
		send(lastPack, sequenceNum);

	}

	public static byte[] mountPacket(byte[] sendData, int sequenceNum) {
		String seqData;
		if (sequenceNum < 10)
			seqData = "0" + sequenceNum;
		else
			seqData = "" + sequenceNum;
		byte[] seq = seqData.getBytes();
		sn = seqData;
		sendData = Arrays.copyOf(sendData, MAX_SIZE + 2);
		sendData[MAX_SIZE] = seq[0];
		sendData[MAX_SIZE + 1] = seq[1];
		return sendData;

	}

	public static void send(byte[] sendData, int seqNum) throws SocketException, IOException {
		InetAddress IpAddress = InetAddress.getByName("localhost");
		DatagramSocket clientSocket = new DatagramSocket();
		sendData = mountPacket(sendData, seqNum);
		System.out.println(new String(sendData));
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IpAddress, 9876);
		clientSocket.send(sendPacket);
		timeout.schedule(new PacketTimeout(seqNum, sendData), 3500);
		window[seqNum % 10] = false;
		System.out.println(sendPacket.hashCode());
		System.out.println("========");
		System.out.println("last pack sent     " + new String(sendPacket.getData()));
		clientSocket.close();
	}

	static class PacketTimeout extends TimerTask {
		private int seq;
		private byte[] message;

		public PacketTimeout(int seq, byte[] message) {
			this.seq = seq;
			this.message = message;
		}

		public void run() {
			if (window[seq] == false) {
				System.out.println("**PACKET TIMEOUT (seq: " + seq + ")**");

				try {
					send(message, seq);
				} catch (Exception e) {
				}
			}
		}

	}
}
