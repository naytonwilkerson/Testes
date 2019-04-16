package com.example.naytonwilkerson.acionawifi;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    Button btnOnOff;
    Button btnDiscovery;
     Button btnOrder;
    ListView listView;
    TextView connectionStatus;


    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    OrderActivity send = new OrderActivity();

    static final int MESSAGE_READ = 1;

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
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                } else {
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
                        Toast.makeText(getApplicationContext(), "Conectado a " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "Não Conectado", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });

    }


    private void initialWork() {
        btnOnOff = findViewById(R.id.onOff);
        btnDiscovery = findViewById(R.id.discover);
        btnOrder = findViewById(R.id.OrderButton);
        listView = findViewById(R.id.peerListView);
        connectionStatus = findViewById(R.id.Status);


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();


        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            if (!peerList.getDeviceList().equals(peers)) {

                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "Nenhum Dispositivo Encontrado", Toast.LENGTH_SHORT).show();
            }

        }
    };


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


}