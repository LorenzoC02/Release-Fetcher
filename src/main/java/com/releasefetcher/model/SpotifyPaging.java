package com.releasefetcher.model;

import java.util.List;

public record SpotifyPaging<T>(List<T> items, String next, Integer total) {}
