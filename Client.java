import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class Client {
	static int serverPort;
	static String filename;

	public static void main(String args[]) throws SocketException, IOException {
		int count = 0;
		int MAX_SIZE = 1048;

		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IpAddress = InetAddress.getByName("localhost");

		byte[] sendData = new byte[MAX_SIZE];

		String filePath = "D:/Users/cdbm/Documents/testing.7z";
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

		byte[] lastPack = new byte[lastPackLen ]; // create new array without redundant information

		fis.close();

		FileInputStream fis1 = new FileInputStream(file);
		// while((count = fis1.read(sendData)) != -1 && (noOfPackets!=0))
		while ((count = fis1.read(sendData)) != -1) {
			if (noOfPackets <= 0)
				break;
			System.out.println(new String(sendData));
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IpAddress, 9876);
			clientSocket.send(sendPacket);
			System.out.println("========");
			System.out.println("last pack sent" + sendPacket);
			noOfPackets--;
		}

		// check
		System.out.println("\nlast packet\n");
		System.out.println(new String(sendData));

		lastPack = Arrays.copyOf(sendData, lastPackLen);

		System.out.println("\nActual last packet\n");
		System.out.println(new String(lastPack));
		// send the correct packet now. but this packet is not being send.
		DatagramPacket sendPacket1 = new DatagramPacket(lastPack, lastPack.length, IpAddress, 9876);
		clientSocket.send(sendPacket1);
		System.out.println("last pack sent" + sendPacket1);

	}
}
