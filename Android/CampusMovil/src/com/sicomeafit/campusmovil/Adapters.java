package com.sicomeafit.campusmovil;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
	 
public class Adapters extends ArrayAdapter<ListItem> {
	 
    private final Context context;
    private final ArrayList<ListItem> itemsArrayList;
 
    public Adapters(Context context, ArrayList<ListItem> itemsArrayList) {
        super(context, R.layout.list_item, itemsArrayList);
        this.context = context;
        this.itemsArrayList = itemsArrayList;
	}
	 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        //Se crea el Inflater.
        LayoutInflater inflater = (LayoutInflater) context
        										   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
        //Se asigna la vista que hace referencia a cada fila de la lista.
        View itemView = inflater.inflate(R.layout.list_item, parent, false);
 
        //Se asignan los dos TextViews que componen dicha vista.
        TextView titleView = (TextView) itemView.findViewById(R.id.title);
        TextView subtitleView = (TextView) itemView.findViewById(R.id.subtitle);
        ImageView categoryView = (ImageView) itemView.findViewById(R.id.category);
 
        //Se setean los textos con la información deseada.
        titleView.setText(itemsArrayList.get(position).getTitle());
        subtitleView.setText(itemsArrayList.get(position).getSubtitle());
        
        Drawable categoryIcon = null;
        switch(itemsArrayList.get(position).getCategory()){
			case "biblioteca":
				categoryIcon = context.getResources().getDrawable(R.drawable.library);
				break;
			case "bloque":
				categoryIcon = context.getResources().getDrawable(R.drawable.block); 
				break;
			case "auditorio":
				categoryIcon = context.getResources().getDrawable(R.drawable.auditorium); 
				break;
			case "idiomas":
				categoryIcon = context.getResources().getDrawable(R.drawable.language_center); 
				break;
			case "cec":
				categoryIcon = context.getResources().getDrawable(R.drawable.cec); 
				break;
			case "portería":
				categoryIcon = context.getResources().getDrawable(R.drawable.entrance); 
				break;

			default:
				break;
		}
        categoryView.setImageDrawable(categoryIcon);

        //Se retorna el item que va a ser mostrado en la fila de la lista.
        return itemView;
        
    }

}
