package cc.cinte.blecontroller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingjikerbo on 2016/9/5.
 */
public class DeviceDetailAdapter extends BaseAdapter {

    private Context mContext;

    private BluetoothDevice mDevice;

    private List<DetailItem> mDataList;

    public DeviceDetailAdapter(Context context, BluetoothDevice device) {
        mContext = context;
        mDataList = new ArrayList<DetailItem>();
        this.mDevice = device;
    }

    private void setDataList(List<DetailItem> datas) {
        mDataList.clear();
        mDataList.addAll(datas);
        notifyDataSetChanged();
    }

    public void setGattProfile(BleGattProfile profile) {
        List<DetailItem> items = new ArrayList<DetailItem>();

        List<BleGattService> services = profile.getServices();

        for (BleGattService service : services) {
            items.add(new DetailItem(DetailItem.TYPE_SERVICE, service.getUUID(), null));
            List<BleGattCharacter> characters = service.getCharacters();
            for (BleGattCharacter character : characters) {
                items.add(new DetailItem(DetailItem.TYPE_CHARACTER, character.getUuid(), service.getUUID()));
            }
        }

        setDataList(items);
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }

    private static class ViewHolder {
        View root;
        TextView uuid;
    }
}
