import java.io.Serializable;
import java.io.*;

public class Message implements Serializable{
	/*
		-1 = Error
		1 = Connecting To Network
		2 = ConnectionEstablished
		3 = Put
		4 = Get
		5 = Success
	*/
	private int code; 
	private String content;
	private int port = -1;

	public Message(int code, String content){
		this.code = code;
		this.content = content;
	}

	public Message(int code, String content, int port){
		this(code, content);
		this.port = port;
	}

	public int getPort(){
		return port;
	}

	public int getCode(){
		return code;
	}

	public String getContent(){
		return content;
	}

	public byte[] Serialize(){
		try{
	      	byte[] bytes = null;
	        ByteArrayOutputStream b = new ByteArrayOutputStream();
	        ObjectOutputStream out = new ObjectOutputStream(b);
	         
	        out.writeObject(this);
	        return b.toByteArray();
	    } catch(IOException e1){
	        System.out.println(e1);
	      	return null;
	    }
	}

	public static Message Deserialize(byte[] b){
		try{	
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(b);
			ObjectInputStream o = new ObjectInputStream(byteInputStream);
			return (Message) o.readObject();
		}catch(IOException e1){
			System.out.println(e1);
			return null;
		}catch(ClassNotFoundException e2){
			System.out.println(e2);
			return null;
		}
	}

	@Override
	public String toString(){
		return code + ": " + content + " - " + port;
	}

	public static void main(String [] args)
   	{
		Message mes = new Message(2, "Works!");
		byte[] b = mes.Serialize();
		Message mes2 = Message.Deserialize(b);
		System.out.println(mes2.getCode() + ": " + mes.getContent());
	}
}