package org.sifacaii.vlcdlnaplayer.vlcplayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import org.sifacaii.vlcdlnaplayer.R;

import java.util.ArrayList;

public class PopMenuP extends PopupWindow {

    public class menu{
        public int groupid;
        public int id;
        public int orderid;
        public String name;
        public View v;
    }

    private Context context;
    private ArrayList<menu> items;
    private View attView;
    private ScrollView contentView;
    private LinearLayout itemContiner;

    private OnItemClickListener onItemClickListener;

    public PopMenuP(Context context, View attView) {
        super(context);
        this.context = context;
        items = new ArrayList<>();

        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        contentView = (ScrollView) LayoutInflater.from(context).inflate(R.layout.popmenu,null);
        setContentView(contentView);
        itemContiner = contentView.findViewById(R.id.popitemContiner);
        this.attView = attView;
        setClippingEnabled(true);
    }

    public menu add(int groupid,int id,int orderid,String name){
        menu m = new menu();
        m.groupid = groupid;
        m.id = id;
        m.orderid = orderid;
        m.name = name;
        View v = LayoutInflater.from(context).inflate(R.layout.popmenu_item,null);
        m.v = v;
        ((TextView)v).setText(name);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != onItemClickListener){
                    onItemClickListener.onClick(m);
                }
                dismiss();
            }
        });
        items.add(m);
        itemContiner.addView(v);
        return m;
    }

    public int size(){
        return items.size();
    }

    public void show(){
        if(items.size() > 12){
            setHeight(500);
        }
        contentView.measure(makeDropDownMeasureSpec(getWidth())
                ,makeDropDownMeasureSpec(getHeight()));
        int offx = 0;
        int offy = contentView.getMeasuredHeight() + attView.getHeight() + 2;
        showAsDropDown(attView,offx,-offy);
    }

    public void show(int index){
        show();
//        if(index >=0 && index < items.size()) {
//            items.get(index).v.requestFocus();
//        }
        for (menu m:items) {
            if(m.id == index){
                m.v.requestFocus();
            }
        }
    }

    public void show(String name){
        show();
        for (menu m:items) {
            if(m.name.equals(name)){
                m.v.requestFocus();
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @SuppressWarnings("ResourceType")
    private static int makeDropDownMeasureSpec(int measureSpec) {
        int mode;
        if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            mode = View.MeasureSpec.UNSPECIFIED;
        } else {
            mode = View.MeasureSpec.EXACTLY;
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode);
    }

    public interface OnItemClickListener{
        void onClick(menu m);
    }
}
