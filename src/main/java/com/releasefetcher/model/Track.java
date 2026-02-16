package com.releasefetcher.model;

import java.util.List;

public record Track(
  String id,
  String name,
  String uri,
  List<Artist> artists,
  Album album
) {
  public record Album(
    String id,
    String name,
    String release_date,
    String release_date_precision,
    List<Image> images
  ) {
    public record Image(String url, Integer height, Integer width) {}
  }
}
