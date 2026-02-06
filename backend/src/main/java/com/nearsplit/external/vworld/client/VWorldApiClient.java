package com.nearsplit.external.vworld.client;

import com.nearsplit.external.vworld.dto.VWorldGeocodingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * packageName  : com.nearsplit.external.vworld.client
 * fileName     : VWorldApiClient
 * author       : user
 * date         : 2026-02-04(수)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-04(수)                user            최초 생성
 */
@Component
public class VWorldApiClient {
    private final RestClient restClient;
    @Value(value = "${vworld.api.key}")
    private String apiKey;

    public VWorldApiClient(@Value("${vworld.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public VWorldGeocodingResponse getCoordinate(String address) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("service", "address")
                        .queryParam("request", "getcoord")
                        .queryParam("version", "2.0")
                        .queryParam("crs", "epsg:4326")
                        .queryParam("address", address)
                        .queryParam("format", "json")
                        .queryParam("type", "road")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(VWorldGeocodingResponse.class);
    }
}
    /*
    GET https://api.vworld.kr/req/address
      ?service=address
      &request=getcoord
      &version=2.0
      &crs=epsg:4326
      &address={주소}
      &format=json
      &type=road
      &key={API_KEY}


    {
    "response": {
      "status": "OK",
      "result": {
        "crs": "epsg:4326",
        "point": {
          "x": "127.0396",   // 경도 (longitude)
          "y": "37.5012"     // 위도 (latitude)
        }
      }
    }
  }
     */
