import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Server {
	static int serverPort;
	static String filename;

	public static void main(String args[]) throws SocketException, IOException {
		int count = 0;
		int MAX_SIZE = 1048;

		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IpAddress = InetAddress.getByName("localhost");

		byte[] sendData = new byte[MAX_SIZE];

		String filePath = "C:/Users/bergc/Documents/google.jpg";
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
		int sequenceNum = 0;
		while ((count = fis1.read(sendData)) != -1) {
			if (noOfPackets <= 0)
				break;
			System.out.println(new String(sendData));
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IpAddress, 9876);
			clientSocket.send(sendPacket);
			sequenceNum++;
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

class sendPacket extends Thread{
	private DatagramPacket send;
	private int sequenceNum; 
	private byte[] recData = new byte[1024];

	public sendPacket(DatagramPacket send, int sequenceNum)
	{
		this.send = send;
		this.sequenceNum = sequenceNum;
	}
	public void run(){
		try{
			long elapse = System.currentTimeMillis();
			DatagramSocket clientSocket = new DatagramSocket();

			DatagramSocket serverSocket = new DatagramSocket(9876);
			DatagramPacket recPacket = new DatagramPacket(recData, recData.length);

			clientSocket.send(this.send);
			int ack = -1;
			long sent = System.currentTimeMillis();
			while(ack != this.sequenceNum)
			{
				sent = System.currentTimeMillis();
				if(sent-elapse > 3500)
				{
					System.out.println("timeout no pacote de sequenceNum: "+sequenceNum);
					clientSocket.send(this.send);
					elapse = System.currentTimeMillis();
				}
				serverSocket.receive(recPacket);
				String ackmsg = new String (recData, "UTF-8");
				ack = Integer.parseInt(ackmsg);
			}
			
		}catch(ConnectException e){
			System.out.println("Deu ruim no destino");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
