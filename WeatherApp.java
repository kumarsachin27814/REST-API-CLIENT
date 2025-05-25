import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class WeatherApp {
    public static void main(String[] args) {
        try {
            
            double latitude = 35.0;
            double longitude = 139.0;

            
            String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" +
                            latitude + "&longitude=" + longitude + "&current_weather=true";

           
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
            JSONObject currentWeather = jsonResponse.getJSONObject("current_weather");

            // Display weather data
            System.out.println("\n==== Current Weather Report ====");
            System.out.println("Temperature: " + currentWeather.getDouble("temperature") + " °C");
            System.out.println("Wind Speed: " + currentWeather.getDouble("windspeed") + " km/h");
            System.out.println("Wind Direction: " + currentWeather.getDouble("winddirection") + "°");
            System.out.println("Time: " + currentWeather.getString("time"));

        } catch (Exception e) {
            System.err.println("Error fetching weather data: " + e.getMessage());
        }
    }
    
}

