package com.releasefetcher.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SpotifyApiService {

  private final RestClient spotifyRestClient;

  public SpotifyApiService(RestClient spotifyRestClient) {
    this.spotifyRestClient = spotifyRestClient;
  }

  public String getUserProfile() {
    return spotifyRestClient.get().uri("/me").retrieve().body(String.class);
  }

  public java.util.List<com.releasefetcher.model.Playlist> getUserPlaylists() {
    return fetchAllItems(
      "/me/playlists?limit=50",
      new org.springframework.core.ParameterizedTypeReference<
        com.releasefetcher.model.SpotifyPaging<
          com.releasefetcher.model.Playlist
        >
      >() {}
    );
  }

  public java.util.List<
    com.releasefetcher.model.PlaylistTrack
  > getPlaylistTracks(String playlistId) {
    return fetchAllItems(
      "/playlists/" + playlistId + "/tracks?limit=50",
      new org.springframework.core.ParameterizedTypeReference<
        com.releasefetcher.model.SpotifyPaging<
          com.releasefetcher.model.PlaylistTrack
        >
      >() {}
    );
  }

  public java.util.List<com.releasefetcher.model.Album> getArtistAlbums(
    String artistId
  ) {
    // Fetch both albums and singles
    return fetchAllItems(
      "/artists/" + artistId + "/albums?include_groups=album,single&limit=50",
      new org.springframework.core.ParameterizedTypeReference<
        com.releasefetcher.model.SpotifyPaging<com.releasefetcher.model.Album>
      >() {}
    );
  }

  public java.util.List<com.releasefetcher.model.Track> getAlbumTracks(
    String albumId
  ) {
    return fetchAllItems(
      "/albums/" + albumId + "/tracks?limit=50",
      new org.springframework.core.ParameterizedTypeReference<
        com.releasefetcher.model.SpotifyPaging<com.releasefetcher.model.Track>
      >() {}
    );
  }

  public void addTracksToPlaylist(
    String playlistId,
    java.util.List<String> uris
  ) {
    if (uris == null || uris.isEmpty()) return;

    java.util.Map<String, java.util.List<String>> body =
      java.util.Collections.singletonMap("uris", uris);

    try {
      spotifyRestClient
        .post()
        .uri("/playlists/" + playlistId + "/tracks")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .toBodilessEntity();
    } catch (Exception e) {
      throw new RuntimeException(
        "Failed to add tracks to playlist: " + e.getMessage()
      );
    }
  }

  public java.util.List<com.releasefetcher.model.Album> getArtistAlbums(
    String artistId,
    String accessToken
  ) {
    RestClient client = RestClient.builder()
      .baseUrl("https://api.spotify.com/v1")
      .defaultHeader("Authorization", "Bearer " + accessToken)
      .build();

    return fetchAllItems(
      "/artists/" + artistId + "/albums?include_groups=album,single&limit=50",
      new org.springframework.core.ParameterizedTypeReference<
        com.releasefetcher.model.SpotifyPaging<com.releasefetcher.model.Album>
      >() {},
      client
    );
  }

  private <T> java.util.List<T> fetchAllItems(
    String initialUri,
    org.springframework.core.ParameterizedTypeReference<
      com.releasefetcher.model.SpotifyPaging<T>
    > responseType
  ) {
    return fetchAllItems(initialUri, responseType, this.spotifyRestClient);
  }

  private <T> java.util.List<T> fetchAllItems(
    String initialUri,
    org.springframework.core.ParameterizedTypeReference<
      com.releasefetcher.model.SpotifyPaging<T>
    > responseType,
    RestClient client
  ) {
    java.util.List<T> allItems = new java.util.ArrayList<>();
    String nextUri = initialUri;

    while (nextUri != null) {
      java.net.URI uri = java.net.URI.create(nextUri);
      String pathAndQuery = uri.getPath();
      if (uri.getQuery() != null) {
        pathAndQuery += "?" + uri.getQuery();
      }

      if (pathAndQuery.startsWith("/v1")) {
        // Correct path for RestClient which already has base URL
        // However, if the client was created WITHOUT base URL (unlikely here but good to know), it would matter.
        // For the default client, it has base URL. For our new manual client, it also has base URL.
        // But the 'next' URL from Spotify is absolute.
        // We strip /v1 because we configured baseUrl ending in /v1.
        pathAndQuery = pathAndQuery.replace("/v1", "");
      }

      var response = client
        .get()
        .uri(pathAndQuery)
        .retrieve()
        .body(responseType);

      if (response != null) {
        if (response.items() != null) {
          allItems.addAll(response.items());
        }
        nextUri = response.next();
      } else {
        nextUri = null;
      }
    }
    return allItems;
  }
}
