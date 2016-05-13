package ru.tehohrana.smsreceiver.service;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import ru.tehohrana.smsreceiver.R;

/**
 * Created by AG on 25.12.2015.
 */
public class ConfigListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] config;
    private final Integer[] icon;
    //private final Integer[] idEvent;

    public ConfigListAdapter(Activity context, String[] config, Integer[] icon) {
        super(context, R.layout.config_listview, config);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.config=config;
        this.icon=icon;
        //this.idEvent=idEvent;

    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.config_listview, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.itemConfigList);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.iconConfigList);


        if (
                config[position]== null ||
                        icon[position] == null)
        {
            //idText.setText("");
            txtTitle.setText("");
            imageView.setImageResource(R.drawable.nullpic);
        }
        else {
            //idText.setText(idEvent[position].toString());
            txtTitle.setText(config[position]);
            imageView.setImageResource(icon[position]);
        }
        //idText.setTextColor(Color.WHITE);
        txtTitle.setTextColor(Color.WHITE);
        return rowView;

    }
}