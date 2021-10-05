package com.nic.Inspection.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;

import java.util.List;

/**
 * Created by shanmugapriyan on 25/05/16.
 */
public class CommonAdapter extends BaseAdapter {
    private List<BlockListValue> BlockList;
    private Context mContext;
    private String type;


    public CommonAdapter(Context mContext, List<BlockListValue> BlockListValue, String type) {
        this.BlockList = BlockListValue;
        this.mContext = mContext;
        this.type = type;
    }


    public int getCount() {
        return BlockList.size();
    }


    public Object getItem(int position) {
        return position;
    }


    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = inflater.inflate(R.layout.spinner_drop_down_item, parent, false);
//        TextView tv_type = (TextView) view.findViewById(R.id.tv_spinner_item);
        View view = inflater.inflate(R.layout.spinner_value, parent, false);
        TextView tv_type = (TextView) view.findViewById(R.id.spinner_list_value);
        BlockListValue BlockListValue = BlockList.get(position);
        if(type.equalsIgnoreCase("DistrictList")) {
            tv_type.setText(BlockListValue.getDistrictName());
        } else if (type.equalsIgnoreCase("BlockList")) {
            tv_type.setText(BlockListValue.getBlockName());
        }else if(type.equalsIgnoreCase("VillageList")){
            tv_type.setText(BlockListValue.getVillageListPvName());
        }else if(type.equalsIgnoreCase("SchemeList")){
            tv_type.setText(BlockListValue.getSchemeName());
        }else if(type.equalsIgnoreCase("FinYearList")){
            tv_type.setText(BlockListValue.getFinancialYear());
        }else if(type.equalsIgnoreCase("StageList")){
            tv_type.setText(BlockListValue.getWorkStageName());
        }else if(type.equalsIgnoreCase("ObservationList")){
            tv_type.setText(BlockListValue.getObservationName());
        }else if(type.equalsIgnoreCase("AEList")){
            tv_type.setText(BlockListValue.getAEName());
        }
        return view;
    }
}
