package org.sing_group.derimt.util;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

/**
 * The interface that defines methods to find a PubMedID in the content of an internet address or file.
 * 
 * @author hlfernandez
 *
 */
public interface PubmedIdFinder {
  /**
   * Finds the PubMedID in the specified {@code url}.
   * 
   * @param url an internet address to download the content where the PubMedID must be found
   * @return the PubMedID wrapped as an {@code Optional} that will be empty if it cannot found
   * @throws MalformedURLException if the specified url is malformed
   */
  Optional<String> getPubmedId(String url) throws MalformedURLException;

  /**
   * Returns an appropriate {@code PubmedIdFinder} for the specified url.
   * 
   * @param url the url where the PubmedID must be found.
   * @return an appropriate {@code PubmedIdFinder} for the specified url that will be empty if there is no an
   *         appropriate implementation.
   */
  public static Optional<PubmedIdFinder> getFinderForUrl(String url) {
    return PubmedIdFinderFactory.getInstance().getFinderForUrl(url);
  }
  
  default String replace(String string, Map<String, String> replacements) {
    for (Entry<String, String> entry : replacements.entrySet()) {
      string = string.replace(entry.getKey(), entry.getValue());
    }

    return string;
  }
}
