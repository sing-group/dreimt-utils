package org.sing_group.derimt.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Optional;

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
   * Finds the PubMedID in the specified {@code file}.
   * 
   * @param file the file where the PubMedID must be found
   * @return the PubMedID wrapped as an {@code Optional} that will be empty if it cannot found
   * @throws FileNotFoundException if the file cannot be found
   */
  Optional<String> getPubmedId(File file) throws FileNotFoundException;

  /**
   * Returns an appropriate {@code PubmedIdFinder} for the specified url.
   * 
   * @param url the url where the PubmedID must be found.
   * @return an appropriate {@code PubmedIdFinder} for the specified url that will be empty if there is no an
   *         appropriate implementation.
   */
  public static Optional<PubmedIdFinder> getFinderForUrl(String url) {
    if (url.contains("broadinstitute")) {
      return Optional.of(new GseaGeneSetHtmlParser());
    } else if (url.contains("cell.com/immunity")) {
      return Optional.of(new CellHtmlParser());
    } else {
      return Optional.empty();
    }
  }
}
