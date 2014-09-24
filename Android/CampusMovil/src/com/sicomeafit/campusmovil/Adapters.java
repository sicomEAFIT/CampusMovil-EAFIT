package com.sicomeafit.campusmovil;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
	 
public class Adapters extends ArrayAdapter<ListItem> implements Filterable {
	 
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
			case "no resultados":
				categoryIcon = context.getResources().getDrawable(R.drawable.no_results); 
				break;

			default:
				break;
		}
        categoryView.setImageDrawable(categoryIcon);

        //Se retorna el item que va a ser mostrado en la fila de la lista.
        return itemView;
        
    }
    
    /*
     * Código de apoyo: 
     * http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html
     */
    @Override
    public android.widget.Filter getFilter() {
        return new PlacesFilter();
    }
    
    @SuppressLint("DefaultLocale")
	private class PlacesFilter extends android.widget.Filter {
    	
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
        	ArrayList<ListItem> itemsArrayListCopy = new ArrayList<ListItem>();
        	itemsArrayListCopy = Places.generateData();  //Esta sería la lista original.
        	FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
            	results.values = new ArrayList<ListItem>();
            	results.count = 0;
            	results.values = itemsArrayListCopy;
                results.count = itemsArrayListCopy.size();
            }
            else {
                // We perform filtering operation
                List<ListItem> fPlacesList = new ArrayList<ListItem>();
                 
                for (ListItem p : itemsArrayListCopy) {
                	
                	String titleWithoutSpCh = p.getTitle().replaceAll("á", "a").replaceAll("é", "e")
                     	   					  .replaceAll("í", "i").replaceAll("ó", "o")
                     	   					  .replaceAll("ú", "u");
                	String subtitleWithoutSpCh = p.getSubtitle().replaceAll("á", "a").replaceAll("é", "e")
   	   					  						 .replaceAll("í", "i").replaceAll("ó", "o")
   	   					  						 .replaceAll("ú", "u");

                    if (p.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())
                    	|| p.getSubtitle().toLowerCase().contains(constraint.toString().toLowerCase())
                    	|| titleWithoutSpCh.toLowerCase().contains(constraint.toString().toLowerCase())
                    	|| subtitleWithoutSpCh.toLowerCase().contains(constraint.toString().toLowerCase())
                    	){
                    	fPlacesList.add(p);
                    }
                    
                }
                
                results.values = new ArrayList<ListItem>();
            	results.count = 0;
                results.values = fPlacesList;
                results.count = fPlacesList.size();
         
            }
            return results;
        }
     
        @SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
	    	// Now we have to inform the adapter about the new list filtered
	    	if (results.count == 0){
	    		ListItem noResultsFound = new ListItem(context.getResources()
	    											   .getString(R.string.no_results_found), "", 
	    											   "no resultados");
	    		itemsArrayList.clear();
	    		itemsArrayList.add(noResultsFound);
	    		notifyDataSetChanged();
	    		//notifyDataSetInvalidated();
	    	}else {
	    		itemsArrayList.clear();
                itemsArrayList.addAll((ArrayList<ListItem>) results.values);
	    	    notifyDataSetChanged();
	    	}
        }
        
    }

}
