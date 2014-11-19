package com.sicomeafit.campusmovil.models;

import com.google.android.gms.maps.model.LatLng;

public class ListItem {

	private String title;
	private String subtitle;
	private String category;
	private LatLng position;

	public ListItem(String title, String subtitle, String category, LatLng position) {
		super();
		this.title = title;
		this.subtitle = subtitle;
		this.category = category;
		this.position = position;
	}

	public String getTitle() {
		return title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public String getCategory() {
		return category;
	}

	public LatLng getPosition() {
		return position;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}

}

