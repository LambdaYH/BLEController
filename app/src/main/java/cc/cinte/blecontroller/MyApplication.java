package cc.cinte.blecontroller;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothContext;
import com.inuker.bluetooth.library.Code;

public class MyApplication extends Application {
        private static MyApplication instance;
        public static Application getInstance() {
            return instance;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            instance = this;
            BluetoothContext.set(this);
        }


}
