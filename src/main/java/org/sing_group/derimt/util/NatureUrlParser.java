package org.sing_group.derimt.util;

import java.util.HashMap;
import java.util.Map;

public class NatureUrlParser extends AbstractDoiToPubMedIdConverter {
  private static final Map<String, String> REPLACEMENTS = new HashMap<>();

  static {
    REPLACEMENTS.put("https://www.nature.com/articles/", "");
    REPLACEMENTS.put("http://www.nature.com/articles/", "");
  }

  @Override
  protected String extractDoiFromUrl(String url) {
    url = replace(url, REPLACEMENTS);
    if (url.contains("#")) {
      url = url.substring(0, url.indexOf("#"));
    }
    return "10.1038/" + url;
  }
  
  public static void main(String[] args) {
    System.err.println(new NatureUrlParser().extractDoiFromUrl("https://www.nature.com/articles/nbt.3367"));
    System.err.println(new NatureUrlParser().getPubmedId("https://www.nature.com/articles/nbt.3367"));
  }
}
