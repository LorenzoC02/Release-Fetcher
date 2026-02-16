package com.releasefetcher.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

  private final com.releasefetcher.service.SpotifyApiService spotifyApiService;
  private final com.releasefetcher.service.ReleaseLogicService releaseLogicService;

  public HomeController(
    com.releasefetcher.service.SpotifyApiService spotifyApiService,
    com.releasefetcher.service.ReleaseLogicService releaseLogicService
  ) {
    this.spotifyApiService = spotifyApiService;
    this.releaseLogicService = releaseLogicService;
  }

  @GetMapping("/")
  public String home(
    @AuthenticationPrincipal OAuth2User principal,
    Model model
  ) {
    if (principal != null) {
      model.addAttribute("name", principal.getAttribute("display_name"));
      try {
        // Fetch Playlists
        var playlists = spotifyApiService.getUserPlaylists();
        model.addAttribute("playlists", playlists);
      } catch (Exception e) {
        model.addAttribute("error", "Error fetching data: " + e.getMessage());
      }
    }
    return "home";
  }

  @GetMapping("/playlist/{id}")
  public String playlist(
    @org.springframework.web.bind.annotation.PathVariable String id,
    @AuthenticationPrincipal OAuth2User principal,
    Model model
  ) {
    if (principal != null) {
      try {
        var tracks = spotifyApiService.getPlaylistTracks(id);
        var artists = releaseLogicService.extractUniqueMainArtists(tracks);

        model.addAttribute("artists", artists);
        model.addAttribute("playlistId", id);
        model.addAttribute("trackCount", tracks.size());
      } catch (Exception e) {
        model.addAttribute("error", "Error fetching tracks: " + e.getMessage());
      }
    }
    return "artist_selection";
  }

  @PostMapping("/check-releases")
  public String checkReleases(
    @RequestParam List<String> artistIds,
    @RequestParam String sinceDate,
    @RequestParam String playlistId,
    @AuthenticationPrincipal OAuth2User principal,
    @org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient(
      "spotify"
    ) org.springframework.security.oauth2.client.OAuth2AuthorizedClient authorizedClient,
    Model model
  ) {
    if (principal != null) {
      String accessToken = authorizedClient.getAccessToken().getTokenValue();
      populateReleases(artistIds, sinceDate, playlistId, accessToken, model);
    }
    return "releases";
  }

  @PostMapping("/add-to-playlist")
  public String addToPlaylist(
    @RequestParam String playlistId,
    @RequestParam String albumId,
    @RequestParam String sourcePlaylistId,
    @RequestParam String sinceDate,
    @RequestParam List<String> artistIds,
    @AuthenticationPrincipal OAuth2User principal,
    @org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient(
      "spotify"
    ) org.springframework.security.oauth2.client.OAuth2AuthorizedClient authorizedClient,
    Model model
  ) {
    if (principal != null) {
      try {
        var tracks = spotifyApiService.getAlbumTracks(albumId);
        var uris = tracks
          .stream()
          .map(com.releasefetcher.model.Track::uri)
          .toList();
        spotifyApiService.addTracksToPlaylist(playlistId, uris);
        model.addAttribute(
          "message",
          "Successfully added " + tracks.size() + " tracks to playlist!"
        );
      } catch (Exception e) {
        model.addAttribute("error", "Failed to add tracks: " + e.getMessage());
      }

      String accessToken = authorizedClient.getAccessToken().getTokenValue();
      populateReleases(
        artistIds,
        sinceDate,
        sourcePlaylistId,
        accessToken,
        model
      );
    }
    return "releases";
  }

  private void populateReleases(
    List<String> artistIds,
    String sinceDate,
    String sourcePlaylistId,
    String accessToken,
    Model model
  ) {
    try {
      java.time.LocalDate date = java.time.LocalDate.parse(sinceDate);
      var releases = releaseLogicService.getFilteredReleases(
        artistIds,
        date,
        accessToken
      );
      model.addAttribute("releases", releases);
    } catch (Exception e) {
      model.addAttribute("releases", List.of());
      model.addAttribute("error", "Error fetching releases: " + e.getMessage());
    }

    try {
      var playlists = spotifyApiService.getUserPlaylists();
      model.addAttribute("playlists", playlists);
    } catch (Exception e) {
      // Ignore
    }

    model.addAttribute("sinceDate", sinceDate);
    model.addAttribute("sourcePlaylistId", sourcePlaylistId);
    model.addAttribute("artistIds", artistIds); // Ensure this is available for the view loop
  }
}
