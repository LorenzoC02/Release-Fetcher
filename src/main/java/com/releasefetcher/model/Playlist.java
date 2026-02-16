package com.releasefetcher.model;

import java.util.List;

public record Playlist(
  String id,
  String name,
  String description,
  List<Image> images
) {
  public record Image(String url, Integer height, Integer width) {}
}
