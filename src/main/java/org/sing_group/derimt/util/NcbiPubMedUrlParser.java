package org.sing_group.derimt.util;

import static java.util.Optional.ofNullable;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NcbiPubMedUrlParser implements PubmedIdFinder {

  private static final Map<String, String> REPLACEMENTS = new HashMap<>();

  static {
    REPLACEMENTS.put("https://www.ncbi.nlm.nih.gov/pubmed/", "");
    REPLACEMENTS.put("http://www.ncbi.nlm.nih.gov/pubmed/", "");
  }

  @Override
  public Optional<String> getPubmedId(String url) throws MalformedURLException {
    return ofNullable(replace(url, REPLACEMENTS));
  }
}
