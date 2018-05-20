package com.hurahura.ray.laporgan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemDrawableTypes;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class HistoryActivity extends AppCompatActivity {

    private Context mContext;

    private int ct=0;

    private Query query;

    private RecyclerView recyclerView;

    private List<Posts> listPosts;

    public static String KEY;

    private DatabaseReference dbReport = FirebaseDatabase.getInstance().getReference("report");
    private DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("user").child(KEY);
    private StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("imageReport");

    private User user = new User();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
        setStatusBarColor();
        mContext = this;

        PostsAdapter adapter = new PostsAdapter(listPosts);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        query = dbReport.limitToLast(30).orderByChild("time");
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        getUserData();
        getReportData();
        Toasty.success(mContext,String.valueOf(ct),2).show();
        adapter.notifyDataSetChanged();


    }

    private void getUserData() {
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                user.setName(map.get("name"));
                user.setEmail(map.get("email"));
                user.setPhone(map.get("phone"));
                user.setImage(map.get("image"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getReportData() {
        dbReport.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                for (int i=0; i<map.size(); i++) {
                    if (map.get("userID").equals(KEY)) {
                        Posts post = new Posts(user.getName(), map.get("report"), new SimpleDateFormat("EEE, dd MMM yyyy").format(map.get("time")),map.get("description"),map.get("photos"),user.getImage());
                        listPosts.add(post);
                        ct++;
                    }
                }
                Toasty.info(mContext,String.valueOf(ct),2).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setStatusBarColor() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }



    public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsHolder> {
        private List<Posts> postsList;

        PostsAdapter (List<Posts> lp) {
            this.postsList = lp;
        }


        public class PostsHolder extends RecyclerView.ViewHolder {
            LinearLayout postsLayout;
            TextView tvDescription;
            TextView tvName;
            TextView tvJenisLaporan;
            TextView tvTime;
            ImageView imageUser;

            public PostsHolder(View view) {
                super(view);

                postsLayout = findViewById(R.id.containerPosts);
                tvDescription = findViewById(R.id.tvDescriptionHistory);
                tvName = findViewById(R.id.tvNameHistory);
                tvJenisLaporan = findViewById(R.id.tvJenisLaporanHistory);
                tvTime = findViewById(R.id.tvTimeHistory);
                imageUser = findViewById(R.id.imageUserHistory);
            }
        }


        @NonNull
        @Override
        public PostsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_posts, parent, false);

            return new PostsHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PostsHolder holder, int position) {
            Posts p = postsList.get(position);
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(Uri.parse(p.getImgLaporanUri()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Drawable yourDrawable = Drawable.createFromStream(inputStream, p.getImgLaporanUri());
            holder.postsLayout.setBackground(yourDrawable);
            holder.imageUser.setImageURI(Uri.parse(p.getImgUserUri()));
            holder.tvDescription.setText(p.getDescription());
            holder.tvJenisLaporan.setText(p.getJenisLaporan());
            holder.tvName.setText(p.getName());
            holder.tvTime.setText(p.getTime());
        }

        @Override
        public int getItemCount() {
            return 2;
        }

    }



//    FirebaseRecyclerOptions<Posts> options =
//            new FirebaseRecyclerOptions.Builder<Posts>()
//                    .setQuery(query, Posts.class)
//                    .build();
//
//    FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, PostsHolder>(options) {
//        @Override
//        public PostsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.layout_posts, parent, false);
//
//            return new PostsHolder(view);
//        }
//
//        @Override
//        protected void onBindViewHolder(final PostsHolder holder, int position, Posts model) {
//            dbReport.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
//                    map.get(KEY+"_"+Pattern.compile("\\d+"));
//                    holder.tvDescription.setText(map.get("description"));
//                    Picasso.get().load(Uri.parse(map.get("photos"))).noFade().into(holder.imageLaporan);
//                    holder.tvJenisLaporan.setText(map.get("report"));
//                    holder.tvTime.setText(new SimpleDateFormat("EEE, dd MMMM yyyy").format(Date.valueOf(map.get("time"))));
//                    FirebaseDatabase.getInstance().getReference("user").child(KEY).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
//                            holder.tvName.setText(map.get("name"));
//                            Picasso.get().load(Uri.parse(map.get("image"))).noFade().into(holder.imageUser);
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//        }
//    };




//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_history);
//
//        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
//
//        // Setup swiping feature and RecyclerView
//        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(swipeMgr.createWrappedAdapter(new MyAdapter()));
//
//        swipeMgr.attachRecyclerView(recyclerView);
//    }
//
//    public void getDataCountFirebase() {
//        dbReport.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                countData = dataSnapshot.getChildrenCount();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        })
//    }
//
//    static class MyItem {

//
//        public MyItem(String name, String description, String time, String jenisLaporan, Uri imageLaporanUri, Uri imageUserUri) {
//            this.name = name;
//            this.description = description;
//            this.time = time;
//            this.jenisLaporan = jenisLaporan;
//            this.imageLaporanUri = imageLaporanUri;
//            this.imageUserUri = imageUserUri;
//        }
//    }
//
//    static class MyViewHolder extends AbstractSwipeableItemViewHolder {
//
//
//        ConstraintLayout containerView;

//
//        public MyViewHolder(View itemView) {
//            super(itemView);
//            containerView = itemView.findViewById(R.id.containerReport);
//            tvDescription = itemView.findViewById(R.id.tvDescriptionHistory);
//            tvJenisLaporan = itemView.findViewById(R.id.tvJenisLaporanHistory);
//            tvName = itemView.findViewById(R.id.tvNameHistory);
//            tvTime = itemView.findViewById(R.id.tvTimeHistory);
//            imageLaporan = itemView.findViewById(R.id.imageReportHistory);
//            imageUser = itemView.findViewById(R.id.imageUserHistory);
//        }
//
//        @Override
//        public View getSwipeableContainerView() {
//            return containerView;
//        }
//    }
//
//    static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> implements SwipeableItemAdapter<MyViewHolder> {
//        interface Swipeable extends SwipeableItemConstants {
//        }
//
//        private DatabaseReference dbReport = FirebaseDatabase.getInstance().getReference("report");
//        private StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("imageReport");
//
//        private long countData;
//
//        List<MyItem> mItems;
//
//        public MyAdapter() {
//            setHasStableIds(true); // this is required for swiping feature.
//
//            mItems = new ArrayList<>();
//            for (int i = 0; i < getCountDatabase(); i++) {
//                mItems.add(new MyItem())
//            }
//        }
//
//        @Override
//        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_history, parent, false);
//            return new MyViewHolder(v);
//        }
//
//
//        @Override
//        public long getItemId(int position) {
//            return mItems.get(position).id; // need to return stable (= not change even after position changed) value
//        }
//
//
//        @Override
//        public void onBindViewHolder(MyViewHolder holder, int position) {
//            MyItem item = mItems.get(position);
//            holder.textView.setText(item.text);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mItems.size();
//        }
//
//        @Override
//        public void onSwipeItemStarted(MyViewHolder holder, int position) {
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public SwipeResultAction onSwipeItem(MyViewHolder holder, int position, @SwipeableItemResults int result) {
//            if (result == Swipeable.RESULT_CANCELED) {
//                return new SwipeResultActionDefault();
//            } else {
//                return new MySwipeResultActionRemoveItem(this, position);
//            }
//        }
//
//        @Override
//        public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
//            return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
//        }
//
//        @Override
//        public void onSetSwipeBackground(MyViewHolder holder, int position, @SwipeableItemDrawableTypes int type) {
//        }
//
//        static class MySwipeResultActionRemoveItem extends SwipeResultActionRemoveItem {
//            private MyAdapter adapter;
//            private int position;
//
//            public MySwipeResultActionRemoveItem(MyAdapter adapter, int position) {
//                this.adapter = adapter;
//                this.position = position;
//            }
//
//            @Override
//            protected void onPerformAction() {
//                adapter.mItems.remove(position);
//                adapter.notifyItemRemoved(position);
//            }
//        }
//
//
//    }
}
