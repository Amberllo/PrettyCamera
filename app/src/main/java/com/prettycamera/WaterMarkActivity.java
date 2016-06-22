package com.prettycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.prettycamera.model.TTFModel;
import com.prettycamera.util.PhotoUtils;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class WaterMarkActivity extends AppCompatActivity{

    ImageView img_watermark;
    RecyclerView recyclerView;
    WatermarkAdapter adapter;
    List<TTFModel> data = new ArrayList<>();
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watermark);
        img_watermark = (ImageView)findViewById(R.id.watermark_img);
        recyclerView = (RecyclerView)findViewById(R.id.watermark_recyclerview);
        //设置布局管理器  
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new WatermarkAdapter(this,data);
        recyclerView.setAdapter(adapter);
        
        path = getIntent().getStringExtra("path");
        Picasso.with(this).load(new File(path)).into(img_watermark);


        Observable.create(new Observable.OnSubscribe<List<TTFModel>>() {
            @Override
            public void call(Subscriber<? super List<TTFModel>> subscriber) {
                subscriber.onNext(getData());
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Action1<List<TTFModel>>() {
            @Override
            public void call(List<TTFModel> models) {
                data.addAll(models);
                adapter.notifyDataSetChanged();
            }
        });

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public <T> void onItemClick(View view, final T model) {

                Observable.create(new Observable.OnSubscribe<Bitmap>() {
                    @Override
                    public void call(Subscriber<? super Bitmap> subscriber) {
                        subscriber.onNext(PhotoUtils.decodeUriAsBitmap(path, 400));
                        subscriber.onCompleted();
                    }
                }).map(new Func1<Bitmap, Bitmap>() {
                    @Override
                    public Bitmap call(Bitmap bitmap) {
                        Typeface typeFace = Typeface.createFromAsset(getAssets(),"ttf/"+((TTFModel)model).name);
                        return PhotoUtils.addWaterMark(bitmap,"A",typeFace);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        img_watermark.setImageBitmap(bitmap);
                    }
                });

            }
        });
        
    }

    public List<TTFModel> getData(){
        List<TTFModel> data = new ArrayList<>();

        try {
            String[] files = getAssets().list("ttf");
            for(String ttf:files){
                data.add(new TTFModel(ttf));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }


    class TTFViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView textView;
        public TTFViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_tv_ttf);
            this.itemView = itemView;
        }

        public void setValue(TTFModel model){

            //将字体文件保存在assets/fonts/目录下，创建Typeface对象
            Typeface typeFace = Typeface.createFromAsset(getAssets(),"ttf/"+model.name);

            //使用字体
            textView.setTypeface(typeFace);
            itemView.setTag(model);

        }

    }

    class WatermarkAdapter extends RecyclerView.Adapter<TTFViewHolder>{

        OnItemClickListener onItemClickListener;
        Context context;
        List<TTFModel> data;

        WatermarkAdapter(Context context,List<TTFModel> data){
            this.context = context;
            this.data = data;
        }

        @Override
        public TTFViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.item_ttf,null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener!=null){
                        onItemClickListener.onItemClick(view,view.getTag());
                    }
                }
            });

            return new TTFViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TTFViewHolder holder, int position) {
            holder.setValue(data.get(position));

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }
    }

    interface OnItemClickListener{
        <T extends Object> void onItemClick(View view,T model);
    }

}
