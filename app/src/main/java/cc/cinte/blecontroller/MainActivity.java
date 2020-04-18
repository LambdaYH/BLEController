package cc.cinte.blecontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    ArrayList list = new ArrayList();
    ArrayAdapter adapter;
    ListView deviceslist;
    String address;
    public static final int REQUEST_SUCCESS = Code.REQUEST_SUCCESS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deviceslist = findViewById(R.id.listv);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        deviceslist.setAdapter(adapter);
        deviceslist.setOnItemClickListener(myListClickListener);
    }
    public void scanBLE(View view){
        list.clear();
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(6000, 3)   // 先扫BLE设备3次，每次3s
                .build();

        ClientManager.getClient().search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Beacon beacon = new Beacon(device.scanRecord);
                BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));
                list.add(device.getName() + "\n" + device.getAddress());
                adapter.notifyDataSetChanged();
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
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
    };
}
