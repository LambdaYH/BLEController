package cc.cinte.blecontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;


import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;




public class MainActivity extends AppCompatActivity { ;
    ArrayList<String> list = new ArrayList();
    ArrayList listAds = new ArrayList();
    ArrayAdapter adapter;
    String address;

    public ImageView Blestatus;

    private Button mConnectButton;
    public SeekBar R_SB;
    public SeekBar G_SB;
    public SeekBar B_SB;

    private boolean isConnected=false;

    private int bleposition=0;

    private BluetoothClient mClient;

    private byte[] bytes;
    public Switch mSwitch;
    private String R_Value = String.valueOf(255);
    private String G_Value = String.valueOf(255);
    private String B_Value = String.valueOf(255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner devices = findViewById(R.id.BLElist);

        mClient=ClientManager.getClient();

        mConnectButton=findViewById(R.id.isconnect);

        R_SB=findViewById(R.id.R_Pixel);
        G_SB=findViewById(R.id.G_Pixel);
        B_SB=findViewById(R.id.B_Pixel);
        R_SB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                R_Value= String.valueOf(progress);
                sendMsg();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        G_SB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                G_Value= String.valueOf(progress);
                sendMsg();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        B_SB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                B_Value= String.valueOf(progress);
                sendMsg();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devices.setAdapter(adapter);
        devices.setOnItemSelectedListener(myItemSelectedListener);
        requestPosition();
        Blestatus=findViewById(R.id.BleStatus);
        mSwitch=findViewById(R.id.switchLight);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    bytes= new byte[]{'O', 'N','\n'};
                    switchMsg();
                }else {
                    bytes=new byte[]{'O','F','F','\n'};
                    switchMsg();
                }
            }
        });
    }
    public void scanBLE(View view){
        list.clear();
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 1)   // 先扫BLE设备3次，每次3s
                .build();

        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Beacon beacon = new Beacon(device.scanRecord);
                BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));
                if(device.getName().contains("LED")) {
                    list.add(device.getName() + "\n" + device.getAddress());
                    listAds.add(device.getAddress());
                    listAds = new ArrayList<>(new HashSet<>(listAds));
                    list  = new ArrayList<>(new HashSet<>(list));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSearchStopped() {

            }

            @Override
            public void onSearchCanceled() {

            }
        });
    }
    public void sendMsg(){
        UUID serviceUUID= UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
        UUID characterUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        String byteTemp=G_Value+"|"+B_Value+"|"+R_Value+"\n";
        bytes=byteTemp.getBytes();
        mClient.write(address, serviceUUID, characterUUID, bytes, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == Constants.REQUEST_SUCCESS) {
                }
            }
        });
    }
    public void switchMsg(){
        UUID serviceUUID= UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
        UUID characterUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        mClient.write(address, serviceUUID, characterUUID, bytes, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == Constants.REQUEST_SUCCESS) {
                }
            }
        });
    }

    private AdapterView.OnItemSelectedListener myItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            bleposition=position;
            if (!isConnected) {
                return;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    public void connect_Button(View v){
        if(isConnected){
            disconnect();
        }
        if(!isConnected){
             connect();
        }
    }
    public void connect(){
        if(listAds.isEmpty()){
        }else{
            address = listAds.get(bleposition).toString();
            mClient.registerConnectStatusListener(address, mBleConnectStatusListener);  //register bluetooth status callback function
            mClient.connect(address, new BleConnectResponse() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(int code, BleGattProfile data) {
                    if(code == Constants.REQUEST_SUCCESS){
                    }
                }
            });
        }
    }
    public void disconnect(){
        if(listAds.isEmpty()){
        }else{
            address = listAds.get(bleposition).toString();
            mClient.disconnect(address);
        }
    }

    public void White_Button(View v){
        R_Value= String.valueOf(255);
        G_Value= String.valueOf(255);
        B_Value= String.valueOf(255);
        sendMsg();
    }
    public void Green_Button(View v){
        R_Value= String.valueOf(0);
        G_Value= String.valueOf(255);
        B_Value= String.valueOf(0);
        sendMsg();
    }
    public void Blue_Button(View v){
        R_Value= String.valueOf(0);
        G_Value= String.valueOf(0);
        B_Value= String.valueOf(255);
        sendMsg();
    }

    //bluetooth callback function
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @SuppressLint("SetTextI18n")
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == Constants.STATUS_CONNECTED) {
                Blestatus.setImageResource(R.drawable.connected);
                mConnectButton.setText("Disconnect");
                isConnected=true;
            } else if (status == Constants.STATUS_DISCONNECTED) {
                Blestatus.setImageResource(R.drawable.disconnected);
                mConnectButton.setText("Connect");
                isConnected=false;
            }
        }
    };

    //get position permission
    private void requestPosition(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }


}
