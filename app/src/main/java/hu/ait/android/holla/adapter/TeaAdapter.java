package hu.ait.android.holla.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import hu.ait.android.holla.R;
import hu.ait.android.holla.data.Place;

public class TeaAdapter extends RecyclerView.Adapter<TeaAdapter.TeaViewHolder> {

    private Context context;
    private List<Place> placeList;
    private List<String> placeKeys;

    private String uID;
    private ScaleAnimation scaleAnimation;
    private BounceInterpolator bounceInterpolator;

    @NonNull
    @Override
    public TeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_place, parent, false);
        TeaAdapter.TeaViewHolder vh = new TeaAdapter.TeaViewHolder(v);
        return vh;
    }

    public TeaAdapter(Context context, String uID) {
        this.context = context;
        this.uID = uID;
        placeKeys = new ArrayList<String>();
        placeList = new ArrayList<Place>();
    }

    @Override
    public void onBindViewHolder(@NonNull final TeaViewHolder holder, int position) {
        holder.tvName.setText(placeList.get(holder.getAdapterPosition()).getName());
        holder.tvRating.setText(placeList.get(holder.getAdapterPosition()).getRating());
        holder.tvAddress.setText(placeList.get(holder.getAdapterPosition()).getAddress());
        boolean open = placeList.get(holder.getAdapterPosition()).getDescription();
        if (open) {
            holder.tvDetails.setText(context.getResources().getString(R.string.open));
        } else {
            holder.tvDetails.setText(context.getResources().getString(R.string.closed));
        }

        scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(500);
        bounceInterpolator = new BounceInterpolator();
        scaleAnimation.setInterpolator(bounceInterpolator);
        if(placeList.get(holder.getAdapterPosition()).isFav()){
            holder.btnFav.setChecked(true);
            holder.tvDetails.setVisibility(View.INVISIBLE);
        }

        holder.btnFav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.startAnimation(scaleAnimation);
                placeList.get(holder.getAdapterPosition()).setFav(isChecked);
                if (isChecked) {
                    Log.d("ADD PLACE", "onCheckedChanged: ADDDDDDDD");
                    placeList.get(holder.getAdapterPosition()).setFav(true);
                    String key = FirebaseDatabase.getInstance()
                            .getReference().child("favs").push().getKey();
                    Place newPlace = new Place(placeList.get(holder.getAdapterPosition()).getName(),
                            placeList.get(holder.getAdapterPosition()).getRating(), placeList.get(holder.getAdapterPosition()).isOpen(),
                            placeList.get(holder.getAdapterPosition()).getAddress(), placeList.get(holder.getAdapterPosition()).getImg(),
                            placeList.get(holder.getAdapterPosition()).isFav());
                    FirebaseDatabase.getInstance().getReference().child("favs").child(key).setValue(newPlace);
                } else {
                    //placeList.get(holder.getAdapterPosition()).setFav(true);
                    //removeFav(holder.getAdapterPosition());
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public void addPlace(Place place) {
        placeList.add(place);
        notifyDataSetChanged();
    }


    public static class TeaViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivPlace;
        public TextView tvName;
        public TextView tvRating;
        public TextView tvDetails;
        public TextView tvAddress;
        public ToggleButton btnFav;

        public TeaViewHolder(View itemView) {
            super(itemView);

            ivPlace = itemView.findViewById(R.id.ivPlace);
            tvName = itemView.findViewById(R.id.tvName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            btnFav = itemView.findViewById(R.id.btnFav);
        }
    }
}
