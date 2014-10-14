package com.sicomeafit.campusmovil.models;

import java.util.ArrayList;


public class MapData {
	
	public static ArrayList<String> markersTitles;
	public static ArrayList<String> markersSubtitles;
	public static ArrayList<String> markersCategories;
	
	public MapData(ArrayList<String> markersTitlesCreated, ArrayList<String> markersSubtitlesCreated,
				   ArrayList<String> markersCategoriesCreated){
		markersTitles = new ArrayList<String>();
		markersSubtitles = new ArrayList<String>();
		markersCategories = new ArrayList<String>();
		markersTitles = markersTitlesCreated;
		markersSubtitles = markersSubtitlesCreated;
		markersCategories = markersCategoriesCreated;
	}

	public static ArrayList<String> getMarkersTitles(){
		ArrayList<String> markersTitlesCopied = new ArrayList<String>();
		for(String title : markersTitles){
			markersTitlesCopied.add(title);
		}
		return markersTitlesCopied;
	}

	public static ArrayList<String> getMarkersSubtitles(){
		ArrayList<String> markersSubtitlesCopied = new ArrayList<String>();
		for(String subtitle : markersSubtitles){
			markersSubtitlesCopied.add(subtitle);
		}
		return markersSubtitlesCopied;
	}

	public static ArrayList<String> getMarkersCategories() {
		ArrayList<String> markersCategoriesCopied = new ArrayList<String>();
		for(String category : markersCategories){
			markersCategoriesCopied.add(category);
		}
		return markersCategoriesCopied;
	}
	
}
