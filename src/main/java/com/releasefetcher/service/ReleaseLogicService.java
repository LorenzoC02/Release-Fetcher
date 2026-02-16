package com.releasefetcher.service;

import com.releasefetcher.model.Album;
import com.releasefetcher.model.Artist;
import com.releasefetcher.model.PlaylistTrack;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReleaseLogicService {

  private final SpotifyApiService spotifyApiService;

  public ReleaseLogicService(SpotifyApiService spotifyApiService) {
    this.spotifyApiService = spotifyApiService;
  }

  public java.util.List<
    com.releasefetcher.model.ArtistStat
  > extractUniqueMainArtists(List<PlaylistTrack> playlistTracks) {
    Map<String, Long> countMap = playlistTracks
      .stream()
      .filter(
        pt ->
          pt.track() != null &&
          pt.track().artists() != null &&
          !pt.track().artists().isEmpty()
      )
      .map(pt -> pt.track().artists().getFirst())
      .collect(Collectors.groupingBy(Artist::id, Collectors.counting()));

    Map<String, Artist> artistMap = playlistTracks
      .stream()
      .filter(
        pt ->
          pt.track() != null &&
          pt.track().artists() != null &&
          !pt.track().artists().isEmpty()
      )
      .map(pt -> pt.track().artists().getFirst())
      .distinct()
      .collect(
        Collectors.toMap(Artist::id, Function.identity(), (a1, a2) -> a1)
      );

    return countMap
      .entrySet()
      .stream()
      .map(entry ->
        new com.releasefetcher.model.ArtistStat(
          artistMap.get(entry.getKey()),
          entry.getValue()
        )
      )
      .sorted((a, b) -> Long.compare(b.trackCount(), a.trackCount())) // Descending
      .collect(Collectors.toList());
  }

  public List<Album> getFilteredReleases(
    List<String> artistIds,
    LocalDate sinceDate,
    String accessToken
  ) {
    return artistIds
      .parallelStream()
      .map(artistId -> spotifyApiService.getArtistAlbums(artistId, accessToken))
      .flatMap(List::stream)
      .filter(distinctByKey(Album::id))
      .filter(album -> isReleasedAfter(album, sinceDate))
      .sorted(Comparator.comparing(Album::release_date))
      .collect(Collectors.toList());
  }

  private boolean isReleasedAfter(Album album, LocalDate sinceDate) {
    if (album.release_date() == null) return false;
    try {
      LocalDate releaseDate;
      if (album.release_date_precision().equals("year")) {
        releaseDate = LocalDate.of(
          Integer.parseInt(album.release_date()),
          1,
          1
        );
      } else if (album.release_date_precision().equals("month")) {
        releaseDate = LocalDate.parse(album.release_date() + "-01");
      } else {
        releaseDate = LocalDate.parse(album.release_date());
      }
      return !releaseDate.isBefore(sinceDate);
    } catch (Exception e) {
      return false;
    }
  }

  // Helper for distinct by property
  private static <T> Predicate<T> distinctByKey(
    Function<? super T, ?> keyExtractor
  ) {
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }
}
