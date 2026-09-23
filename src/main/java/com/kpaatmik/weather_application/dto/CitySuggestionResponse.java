package com.kpaatmik.weather_application.dto;

public record CitySuggestionResponse(  
String name,
 String state,
 String country,
 Double latitude,		
 Double longitude) {

}
