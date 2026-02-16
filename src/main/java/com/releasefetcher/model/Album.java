package com.releasefetcher.model;

import java.util.List;

public record Album(
  String id,
  String name,
  String release_date,
  String release_date_precision,
  String album_type,
  List<Image> images,
  List<Artist> artists,
  java.util.Map<String, String> external_urls
) {
  public record Image(String url, Integer height, Integer width) {}
  // We reuse Artist model here
}
