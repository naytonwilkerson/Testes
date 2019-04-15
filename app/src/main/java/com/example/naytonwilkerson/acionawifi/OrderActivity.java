package com.example.naytonwilkerson.acionawifi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class OrderActivity extends AppCompatActivity {

    Button btnMandar;
   public static TextView read_msg_box, connectionStatus;
    EditText writeMsg;


    MainActivity.SendReceiver send = new MainActivity.SendReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        btnMandar = findViewById(R.id.sendButton2);
        read_msg_box =  findViewById(R.id.readMsg);
        writeMsg =  findViewById(R.id.writeMsg2);
        connectionStatus =  findViewById(R.id.connectionStatus);



        btnMandar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsg.getText().toString();
                read_msg_box.append("\n"+"You"+" : "+writeMsg.getText().toString());
                writeMsg.setText("");
               MainActivity.SendReceiver.write(msg.getBytes());
            }
        });

    }

}





