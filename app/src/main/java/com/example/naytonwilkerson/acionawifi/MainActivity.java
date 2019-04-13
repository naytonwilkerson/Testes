package com.example.naytonwilkerson.acionawifi;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {



    Button btnOnOff, btnDiscovery, btnSend;
    ListView listView;
    TextView  connectionStatus;


    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers =new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    ClientClass clientClass;
    ServerClass serverClass;

    SendReceiver sendReceiver;


    static final int MESSAGE_READ=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialWork();

        exqListener();

    }



    private void exqListener() {
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wifiManager.isWifiEnabled())
                {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                }else{
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("OFF");
                }
            }
        });

        btnDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Iniciando Buscas", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "Impossível Buscar no Momento", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = deviceArray[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Conectado a "+device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "Não Conectado", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });

    }


    private void initialWork(){
        btnOnOff =  findViewById(R.id.onOff);
        btnDiscovery= findViewById(R.id.discover);
        btnSend =  findViewById(R.id.sendButton);
        listView =  findViewById(R.id.peerListView);

        connectionStatus = findViewById(R.id.connectionStatus);


        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel=mManager.initialize(this,getMainLooper(),null);

        mReceiver=new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();


        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener(){
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            if(!peerList.getDeviceList().equals(peers)){

                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;

                for(WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_expandable_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }

            if(peers.size() == 0)
            {
                Toast.makeText(getApplicationContext(), "Nenhum Dispositivo Encontrado", Toast.LENGTH_SHORT).show();
            }

        }
    };


    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            if(info.groupFormed && info.isGroupOwner)
            {
                connectionStatus.setText("Professor");
                serverClass = new MainActivity.ServerClass();
                serverClass.start();
            }else if(info.groupFormed)
            {
                connectionStatus.setText("Aluno");
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MainActivity.MESSAGE_READ:
                    byte[] readBuff = (byte[])msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    String read_msg_box =tempMsg;
                    Toast.makeText(getApplicationContext(), read_msg_box, Toast.LENGTH_SHORT).show();
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
                sendReceiver = new SendReceiver(socket);
                sendReceiver.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public class SendReceiver extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

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

       public void write(byte[] bytes)
        {
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
                sendReceiver = new MainActivity.SendReceiver(socket);
                sendReceiver.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}