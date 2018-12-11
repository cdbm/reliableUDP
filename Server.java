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

		String filePath = "C:/Users/C. Davi/Documents/Lista_2.txt";
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
		while ((count = fis1.read(sendData)) != -1) {
			if (noOfPackets <= 0)
				break;
			//byte[] trusend = Arrays.copyOf(sendData, MAX_SIZE+5);

			sendData = mountPacket(sendData, ++sequenceNum);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IpAddress, 9876);
			clientSocket.send(sendPacket);
			System.out.println(sendPacket.hashCode());
			
			System.out.println("========");
			System.out.println("last pack sent     " + new String(sendPacket.getData()));
			noOfPackets--; 
		}

		// check
		System.out.println("\nlast packet\n");
		System.out.println(new String(sendData));

		lastPack = Arrays.copyOf(sendData, lastPackLen-1);

		System.out.println("\nActual last packet\n");
		System.out.println(new String(lastPack));
		lastPack = mountPacket(lastPack, ++sequenceNum);
		// send the correct packet now. but this packet is not being send.
		DatagramPacket sendPacket1 = new DatagramPacket(lastPack, lastPack.length, IpAddress, 9876);
		clientSocket.send(sendPacket1);
		System.out.println("last pack sent" + sendPacket1);

	}

	public static byte[] mountPacket(byte[] sendData, int sequenceNum){
		String seqData;
		if(sequenceNum < 10)
			seqData = "0"+sequenceNum;
		else
			seqData = ""+sequenceNum;
		byte[] seq = seqData.getBytes();
		sendData = Arrays.copyOf(sendData, 1050);
		sendData[1048] = seq[0];
		sendData[1048+1] = seq[1];
		return sendData;
		
	}
}

class sendPacket extends Thread{
	private DatagramPacket send;
	private String sequenceNum; 
	private byte[] recData = new byte[2];
	//private byte[] dale = Packet.mount(this.recData,7);

	public sendPacket(DatagramPacket send, String sequenceNum)
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
			String ack = "";
			long sent = System.currentTimeMillis();
			while(!ack.equals(sequenceNum))
			{
				sent = System.currentTimeMillis();
				if(sent-elapse > 3500)
				{
					System.out.println("timeout no pacote de sequenceNum: "+ sequenceNum);
					clientSocket.send(this.send);
					elapse = System.currentTimeMillis();
				}
				serverSocket.receive(recPacket);
				ack = new String (recData, "UTF-8");
				//ack = Integer.parseInt(ackmsg);
			}
			
		}catch(ConnectException e){
			System.out.println("Deu ruim no destino");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
