package tehohrana.ru.cathodmon;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by AG on 18.04.2016.
 */
public class CathodesListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] event;
    private final String[] date;
    private final Integer[] icon;
    //private final Integer[] idEvent;

    public CathodesListAdapter(Activity context, String[] event, String[] date, Integer[] icon) {
        super(context, R.layout.archieve_listview, event);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.event=event;
        this.icon=icon;
        this.date=date;
        //this.idEvent=idEvent;

    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.archieve_listview, null, true);

        //TextView idText = (TextView) rowView.findViewById(R.id.idArchieveList);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.itemArchieveList);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.iconArchieveList);
        TextView extratxt = (TextView) rowView.findViewById(R.id.textViewArchieveList);


        if (
                event[position]== null ||
                        icon[position] == null ||
                        date[position] == null)
        {
            //idText.setText("");
            txtTitle.setText("");
            imageView.setImageResource(R.drawable.back_icon);
            extratxt.setText("");
        }
        else {
            //idText.setText(idEvent[position].toString());
            txtTitle.setText(event[position]);
            imageView.setImageResource(icon[position]);
            extratxt.setText(date[position]);
        }
        //idText.setTextColor(Color.WHITE);
        txtTitle.setTextColor(Color.WHITE);
        extratxt.setTextColor(Color.GRAY);
        return rowView;

    }
}