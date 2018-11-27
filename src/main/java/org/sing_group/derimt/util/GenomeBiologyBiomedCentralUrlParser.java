package org.sing_group.derimt.util;

import java.util.HashMap;
import java.util.Map;

public class GenomeBiologyBiomedCentralUrlParser extends AbstractDoiToPubMedIdConverter {

  private static final Map<String, String> REPLACEMENTS = new HashMap<>();

  static {
    REPLACEMENTS.put("https://genomebiology.biomedcentral.com/articles/", "");
    REPLACEMENTS.put("http://genomebiology.biomedcentral.com/articles/", "");
  }

  @Override
  protected String extractDoiFromUrl(String url) {
    return replace(url, REPLACEMENTS);
  }
}
