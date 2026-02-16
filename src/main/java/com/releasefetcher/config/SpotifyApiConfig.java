package com.releasefetcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class SpotifyApiConfig {

  @Bean
  public RestClient spotifyRestClient(
    OAuth2AuthorizedClientManager authorizedClientManager
  ) {
    OAuth2ClientHttpRequestInterceptor requestInterceptor =
      new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
    requestInterceptor.setClientRegistrationIdResolver(request -> "spotify");

    return RestClient.builder()
      .baseUrl("https://api.spotify.com/v1")
      .requestInterceptor(requestInterceptor)
      .build();
  }
}
