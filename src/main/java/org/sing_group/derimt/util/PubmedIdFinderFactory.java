package org.sing_group.derimt.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

public class PubmedIdFinderFactory {
  private static PubmedIdFinderFactory instance = null;

  private final GseaGeneSetHtmlParser gseaGeneSetHtmlParser = new GseaGeneSetHtmlParser();
  private final CellHtmlParser cellHtmlParser = new CellHtmlParser();
  private final GenomeBiologyBiomedCentralUrlParser genomeBiologyBiomedCentralUrlParser =
    new GenomeBiologyBiomedCentralUrlParser();
  private final NatureUrlParser natureUrlParser = new NatureUrlParser();
  private final DoiUrlParser doiUrlParser = new DoiUrlParser();
  private final NcbiPubMedUrlParser ncbiPubMedUrlParser = new NcbiPubMedUrlParser();

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
      return of(gseaGeneSetHtmlParser);
    } else if (url.contains("cell.com/immunity")) {
      return of(cellHtmlParser);
    } else if (url.contains("genomebiology.biomedcentral.com/articles/")) {
      return of(genomeBiologyBiomedCentralUrlParser);
    } else if (url.contains("dx.doi.org") || url.contains("doi.org")) {
      return of(doiUrlParser);
    } else if (url.contains("www.nature.com/articles/")) {
      return of(natureUrlParser);
    } else if (url.contains("www.ncbi.nlm.nih.gov/pubmed/")) {
      return of(ncbiPubMedUrlParser);
    } else {
      return empty();
    }
  }
}
