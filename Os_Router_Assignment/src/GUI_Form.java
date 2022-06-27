import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Writer {
    private static final Writer writer = new Writer();

    private Writer(){
        super();
    }

    public synchronized void write(String  message){
        try{
            FileWriter myWriter=new FileWriter("result.txt" , true);
            myWriter.write(message);
            myWriter.close();
        }catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static Writer getInstance(){
        return writer;
    }
}

class semaphore {

    protected int value  ;

    public int getValue() {
        return value;
    }

    protected semaphore() { value = 0 ; }

    protected semaphore(int initial) { value = initial ; }

    public synchronized void P(Device device) {
        value-- ;
        if (value < 0) {//full
            try {
                Writer.getInstance().write("(" + device.getDName()+") " + " (" + device.getDType()+") "+" arrived and waiting"+ '\n');
                device.setArrived(false);

                wait();
            } catch (InterruptedException e) {
                System.out.println("error happened");
            }
        }
        if(device.getArrived() == true && device.isReleased()==false){
            Writer.getInstance().write("(" + device.getDName()+") " + " (" + device.getDType()+") "+ " arrived"+ '\n');
        }
    }

    public synchronized void V( Device device) {
        value++ ;
        if (value <= 0) {
            notify();//empty place
        }
    }
}

class Router {
    public ArrayList<Device> devices=new ArrayList<>();

    private int size;

    public semaphore avaiableConnections;
    public semaphore connectedDev= new semaphore(0);

    public Router(int size){
        this.size=size;
        avaiableConnections=new semaphore(size);

    }

    public void occupy(Device device) {
        // decrease number of available connection
        avaiableConnections.P(device);

        if(devices.size() != 0) {
            boolean f = false;
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getDName() == " ") {
                    devices.set(i, device);
                    f = true;
                }
            }
            if(f == false)
                devices.add(device);
        }
        else {
            devices.add(device);
        }

        connectedDev.V(device);
    }

    public void release(Device device){
        device.setReleased(true);
        connectedDev.P(device);
        for(int i = 0; i < devices.size(); i++){
            if(devices.get(i) == device)
                devices.set(devices.indexOf(device),new Device(null , " " , " ",null));
        }
        avaiableConnections.V(device);
    }
}

class Device extends Thread {
    private String name;
    private String type;
    private Router router;
    private boolean arrived;
    private boolean released;
    private JPanel mainPanel;

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    public boolean getArrived() {
        return arrived;
    }

    public Device(Router router, String name, String type,JPanel mainPanel){
        this.name=name;
        this.type=type;
        this.router=router;
        this.arrived = true;
        this.released = false;
        this.mainPanel=mainPanel;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public String getDName() {
        return name;
    }

    public String getDType() {
        return type;
    }

    public void connect(){
        int index  = router.devices.indexOf(this) + 1;

        Writer.getInstance().write("Connection " + index +" : "+name+" login" + '\n');
    }

    public void perform_operations(){
        int index  = router.devices.indexOf(this) + 1;
        Writer.getInstance().write("Connection " + index +" : "+name+" perform operations" + '\n');
    }

    public void  log_out (){
        int index  = router.devices.indexOf(this) + 1;
        Writer.getInstance().write("Connection " + index +" : "+name+" log out"+ '\n');
    }

    @Override
    public void run(){
        router.occupy(this);
        int index  = router.devices.indexOf(this) + 1;
        Writer.getInstance().write("Connection " + index +" : "+name+" occupied" + '\n');
        ImageIcon image1=new ImageIcon("phone.jpg");

        JLabel label1= new JLabel(image1);
        Border b1 = new BevelBorder(BevelBorder.LOWERED, Color.green, Color.green);

        label1.setBorder(b1);
        label1.setFont(label1.getFont().deriveFont(20.0F));
        label1.setText(name+" Occupied");
        mainPanel.setVisible(true);
        mainPanel.add(label1);
        connect();
        try {
            Thread.sleep((long)(Math.random() * 3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        perform_operations();
        try {
            Thread.sleep((long)(Math.random() * 4000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log_out();
        try {
            Thread.sleep((long)(Math.random() * 5000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Border b2 = new BevelBorder(BevelBorder.LOWERED, Color.red, Color.red);
        label1.setBorder(b2);
        label1.setFont(label1.getFont().deriveFont(25.0F));
        label1.setText(name+" Released");
        router.release(this);

    }
}

class Network extends JFrame {

    int max_connections ;
    int total_devices;
    static JPanel mainPanel=new JPanel();
    public static Router router;

    ArrayList<Device> devices = new ArrayList<>();

    public Network(int max_connections , int total_devices){
        this.max_connections = max_connections;
        this.total_devices = total_devices;
        router = new Router(max_connections);
    }

    public void addDevice(Device device){
        devices.add(device);
    }

    public void process(){
        for (int i = 0; i < total_devices ; i++){
            devices.get(i).start();
        }
    }

    public static void main(String[] args) {
        JFrame jFrame=new JFrame();
        jFrame.setContentPane(mainPanel);
        jFrame.setSize(2000,1000);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLayout(new FlowLayout());


        Scanner input= new Scanner(System.in);

        System.out.println("What is the number of WI-FI Connections?");
        int max_connections = input.nextInt();

        System.out.println("What is the number of devices Clients want to connect?");
        int total_devices = input.nextInt();

        Network network=new Network(max_connections , total_devices );

        for(int i = 0; i < total_devices ; i++){
            System.out.println("Please Enter The Name Of The Device ");
            String name = input.next();
            System.out.println("Please Enter The Type Of The Device ");
            String type = input.next();
            Device device = new Device(router,name, type,mainPanel);
            network.addDevice(device);
        }
        network.process();
        System.out.println("Sequence written on the file and represented on the screen.");
        Writer.getInstance().write("******************************************************" + '\n');

    }
}
