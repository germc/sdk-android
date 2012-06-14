package com.playhaven.sampleapp;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.playhaven.androidsdk.R;

/** Simple customized adapter to handle rows with title and detail view. You should provide objects
 * which conform to the {@link DetailObject} protocol. 
 * 
 * If the {@link #DetailObject} returns null for either title or detail, we simply hide that field.*/
public class DetailAdapter<T extends DetailAdapter.DetailObject> extends ArrayAdapter<T> {
	/** Simple delegate interface for the objects which we are displaying.
	 * Return null from either to hide the associated field.*/
	public interface DetailObject {
		
		public String getTitle();
		public String getDetail();
		public View getView();
	}
	
	private ArrayList<T> details;
	
	public DetailAdapter(Context context, int textViewResId, ArrayList<T> details) {
		super(context, textViewResId, details);
		this.details = details;
	}
	
	/** For optimization of listviews. 
	 * It is a static class to avoid implicit context reference memory leak*/
	private static class ViewHolder {
		TextView topTxt;
		TextView detailTxt;
		LinearLayout detailLinearLayout;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder; // speedup lookup..
		
		if(v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row, null);
			
			holder = new ViewHolder();
			holder.topTxt = (TextView)v.findViewById(R.id.mainText);;
			holder.detailTxt = (TextView)v.findViewById(R.id.detailText);
			holder.detailLinearLayout = (LinearLayout)v.findViewById(R.id.detailLinearLayout);
			
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		DetailObject detail = details.get(position);
		if(detail != null) {
			
			TextView topTxt = holder.topTxt;
			TextView detailTxt = holder.detailTxt;
			LinearLayout detailLinearLayout = holder.detailLinearLayout;
			
			if(topTxt != null) {
				String title = detail.getTitle();
				if(title != null)
					topTxt.setText(title);
				else
					topTxt.setVisibility(View.GONE);
			}
			
			if(detailLinearLayout != null) {
				View detailView = detail.getView();
				if(detailView != null) {
					detailLinearLayout.setVisibility(View.VISIBLE);
					
					//remove all previous views
					detailLinearLayout.removeAllViews();
					
					//if it has another parent, remove and re-add (issue with view recyling) 
					//TODO: bit hacky, find out why we recycle and re-instantiate.
					if (detailView.getParent() != null) {
						ViewGroup detailParent = (ViewGroup)detailView.getParent();
						detailParent.removeView(detailView);
					}
					
					detailLinearLayout.addView(detailView);
					
				} else {
					detailLinearLayout.setVisibility(View.GONE);
				}
			}
			
			if(detailTxt != null) {
				String detail_str= detail.getDetail();
				if(detail_str != null)
					detailTxt.setText(detail_str);
				else
					detailTxt.setVisibility(View.GONE);
			}
		}
		
		return v;
	}
	
	@Override
	public int getItemViewType(int position) {
		return 0; //one type..
	}
	
	@Override
	public int getViewTypeCount() {
		return 1; //only one type..
	}
}
