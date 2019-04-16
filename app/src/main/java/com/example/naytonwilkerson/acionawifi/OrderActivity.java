package com.example.naytonwilkerson.acionawifi;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class OrderActivity extends AppCompatActivity {

    Button btnMandar;
   static TextView read_msg_box;
    TextView Status;
    EditText writeMsg;
    static final int MESSAGE_READ = 1;
    ClientClass clientClass;
    ServerClass serverClass;

    SendReceiver sendReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        btnMandar = findViewById(R.id.sendButton2);
        read_msg_box =  findViewById(R.id.readMsg);
        writeMsg =  findViewById(R.id.writeMsg2);
        Status =  findViewById(R.id.Status);



        btnMandar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsg.getText().toString();
                read_msg_box.append("\n"+"You"+" : "+writeMsg.getText().toString());
                writeMsg.setText("");
               SendReceiver.write(msg.getBytes());
            }
        });

    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            if (info.groupFormed && info.isGroupOwner) {

                serverClass = new OrderActivity.ServerClass();
              //  Status.setText("Professor");
                serverClass.start();
            } else if (info.groupFormed) {

                clientClass = new OrderActivity.ClientClass(groupOwnerAddress);
               // Status.setText("Aluno");
                clientClass.start();
            }
        }
    };

    static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MainActivity.MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);


                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    read_msg_box.append("\n"+"User"+" : "+tempMsg);

                    break;
            }
            return true;
        }
    });

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run(){
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceiver = new OrderActivity.SendReceiver(socket);
                sendReceiver.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static   class SendReceiver extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private static OutputStream outputStream;

        SendReceiver(Socket s) {
            socket = s;

            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket!=null){
                try {
                    bytes=inputStream.read(buffer);
                    if (bytes>0){
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

         static void write(byte[] bytes) {

            try {
                outputStream.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;

        ClientClass(InetAddress hostAddress)
        {
            hostAdd=hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceiver = new OrderActivity.SendReceiver(socket);
                sendReceiver.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}





