package org.sing_group.derimt.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

public class PubmedIdFinderFactory {
  private static PubmedIdFinderFactory instance = null;
  
  private final GseaGeneSetHtmlParser gseaGeneSetHtmlParser = new GseaGeneSetHtmlParser();
  private final CellHtmlParser cellHtmlParser = new CellHtmlParser();

  protected PubmedIdFinderFactory() {}

  public static PubmedIdFinderFactory getInstance() {
    if (instance == null) {
      instance = new PubmedIdFinderFactory();
    }
    return instance;
  }
  
  /**
   * Returns an appropriate {@code PubmedIdFinder} for the specified url.
   * 
   * @param url the url where the PubmedID must be found.
   * @return an appropriate {@code PubmedIdFinder} for the specified url that will be empty if there is no an
   *         appropriate implementation.
   */
  public Optional<PubmedIdFinder> getFinderForUrl(String url) {
    if (url.contains("broadinstitute")) {
      return of(gseaGeneSetHtmlParser );
    } else if (url.contains("cell.com/immunity")) {
      return of(cellHtmlParser);
    } else {
      return empty();
    }
  }
}
