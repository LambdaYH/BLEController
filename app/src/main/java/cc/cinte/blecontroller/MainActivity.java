package cc.cinte.blecontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Code;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity { ;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    ArrayList<String> list = new ArrayList();
    ArrayAdapter adapter;
    private Spinner deviceslist;
    String address;
    public static final int REQUEST_SUCCESS = Code.REQUEST_SUCCESS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deviceslist = findViewById(R.id.BLElist);
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceslist.setAdapter(adapter);
        deviceslist.setOnItemSelectedListener(myItemSelectedListener);
        requestPosition();
    }
    public void scanBLE(View view){
        list.clear();
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 1)   // 先扫BLE设备3次，每次3s
                .build();

        ClientManager.getClient().search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Beacon beacon = new Beacon(device.scanRecord);
                BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));
                if(device.getName().contains("LED")) {
                    list.add(device.getName() + "\n" + device.getAddress());
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
    public void sendMsg(View v){
        byte[] bytes={'s','b','h','h','q'};
        UUID serviceUUID= UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
        UUID characterUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        ClientManager.getClient().write(address, serviceUUID, characterUUID, bytes, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {

                }
            }
        });
    }
    private AdapterView.OnItemSelectedListener myItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            address = info.substring(info.length()-17);
            ClientManager.getClient().connect(address, new BleConnectResponse() {
                @Override
                public void onResponse(int code, BleGattProfile profile) {
                    if (code == REQUEST_SUCCESS) {
                        list.add("fine");
                        adapter.notifyDataSetChanged();
                    }
                        }
            });
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

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
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }


}
