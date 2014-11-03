import java.io.Serializable;
import java.io.*;
import java.net.Socket;
import java.util.Date;

public class Message implements Serializable{

    public static final String SEPERATOR = "#####";
	private CodeType code; 
	private String content;
	private int key = -1;
	private int port = -1;
	private Date time = new Date();

	public Message(CodeType code, String content){
		this.code = code;
		this.content = content;
	}

	public Message(CodeType code, String content, int port){
		this(code, content);
		this.port = port;
	}

	public Message(CodeType code, String content, int port, int key){
		this(code, content, port);
		this.key = key;
	}

	public int getPort(){
		return port;
	}

	public int getKey(){
		return key;
	}

	public CodeType getCode(){
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

	@Override
    public int hashCode() {
        return key * port * content.hashCode() * time.hashCode();
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
		Message mes = new Message(CodeType.Success, "Works!");
		byte[] b = mes.Serialize();
		Message mes2 = Message.Deserialize(b);
		System.out.println(mes2.getCode() + ": " + mes.getContent());
	}
}