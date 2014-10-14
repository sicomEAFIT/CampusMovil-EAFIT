package com.sicomeafit.campusmovil.models;

public class ListItem {

    private String title;
    private String subtitle;
    private String category;
    
    public ListItem(String title, String subtitle, String category) {
        super();
        this.title = title;
        this.subtitle = subtitle;
        this.category = category;
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
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public void setCategory(String category) {
		this.category = category;
	}
     
}

