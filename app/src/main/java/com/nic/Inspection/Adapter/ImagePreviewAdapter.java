package com.nic.Inspection.Adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.session.PrefManager;

import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.MyViewHolder> {

    private Context context;
    private PrefManager prefManager;
    private List<BlockListValue> imagePreviewlistvalues;

    public ImagePreviewAdapter(Context context, List<BlockListValue> imagePreviewlistvalues) {

        this.context = context;
        prefManager = new PrefManager(context);
        this.imagePreviewlistvalues = imagePreviewlistvalues;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_image, parent, false);
        return new  MyViewHolder(itemView);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView preview_image_view;
        private MyCustomTextView description;
        private CheckBox checkbox;
        private View itemView;



        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView   = itemView;
            preview_image_view = (ImageView) itemView.findViewById(R.id.preview_image_view);
            description = (MyCustomTextView) itemView.findViewById(R.id.description);
//            checkbox        = (CheckBox) itemView.findViewById(R.id.checkBox);
//            checkbox.setClickable(false);

        }
        public void setOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.description.setText(imagePreviewlistvalues.get(position).getDescription());
        holder.preview_image_view.setImageBitmap(imagePreviewlistvalues.get(position).getImage());
//        holder.checkbox.setChecked(imagePreviewlistvalues.get(position).isSetItemSelected());

//        holder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                holder.checkbox.setChecked(!(holder.checkbox.isChecked()));
//
//                if (holder.checkbox.isChecked()) {
//                    List<String> checked_value = new ArrayList<>();
//                    for(int i=0; i<imagePreviewlistvalues.size(); i++ ) {
//                        if(imagePreviewlistvalues.get(i).isSetItemSelected()) {
////                            checked_value.add(imagePreviewlistvalues.get(i).getName());
//                        }
//                    }
//                    if(imagePreviewlistvalues.size() > 0){
//
//                    }
//                    //onItemClick.onItemCheck(currentItem);
//                    Utils.showAlert(context,"check");
//                } else {
//                    // onItemClick.onItemUncheck(currentItem);
//                    Utils.showAlert(context,"Notcheck");
//                }
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return imagePreviewlistvalues.size();
    }
}

