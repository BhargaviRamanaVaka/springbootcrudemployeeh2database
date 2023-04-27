package com.example.main.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    
    private final HttpClient httpClient;

    public WeatherController() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @GetMapping("/{latitude},{longitude}")
    public ResponseEntity<String> getWeather(@PathVariable String latitude, @PathVariable String longitude) throws IOException, InterruptedException {
        String url = String.format("https://api.weather.gov/points/%s,%s", latitude, longitude);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/geo+json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        String forecastUrl = null;
        if (response.statusCode() == 200) {
            JsonNode rootNode = new ObjectMapper().readTree(response.body());
            forecastUrl = rootNode.at("/properties/forecast").asText();
        } else {
            return ResponseEntity.status(response.statusCode()).body(response.body());
        }
        
        request = HttpRequest.newBuilder()
                .uri(URI.create(forecastUrl))
                .header("Accept", "application/json")
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return ResponseEntity.status(response.statusCode()).body(response.body());
    }
}
