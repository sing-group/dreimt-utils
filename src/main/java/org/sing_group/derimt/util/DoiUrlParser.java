package org.sing_group.derimt.util;

import java.util.HashMap;
import java.util.Map;

public class DoiUrlParser extends AbstractDoiToPubMedIdConverter {

  private static final Map<String, String> REPLACEMENTS = new HashMap<>();

  static {
    REPLACEMENTS.put("https://dx.doi.org/", "");
    REPLACEMENTS.put("http://dx.doi.org/", "");
  }

  @Override
  protected String extractDoiFromUrl(String url) {
    return replace(url, REPLACEMENTS);
  }
}
