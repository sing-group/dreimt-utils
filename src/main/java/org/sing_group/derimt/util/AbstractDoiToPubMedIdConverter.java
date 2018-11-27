package org.sing_group.derimt.util;

import static java.util.Optional.empty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class AbstractDoiToPubMedIdConverter implements PubmedIdFinder {
  private static final Logger LOGGER = Logger.getLogger(PubmedIdsResolver.class.getName());

  private File doiCacheFile;
  private Map<String, String> cache = new HashMap<>();

  public AbstractDoiToPubMedIdConverter() {
    this(new File("data/doi-to-pubmedid-cache.dat"));
  }

  public AbstractDoiToPubMedIdConverter(File pubmedCacheFile) {
    this.doiCacheFile = pubmedCacheFile;
    this.restoreCache();
  }

  private void restoreCache() {
    if (this.doiCacheFile.exists()) {
      try {
        FileInputStream fis = new FileInputStream(doiCacheFile);
        Object cache = new ObjectInputStream(fis).readObject();
        fis.close();

        try {
          @SuppressWarnings("unchecked")
          Map<String, String> map = (Map<String, String>) cache;
          this.cache.putAll(map);
        } catch (ClassCastException e) {
          throw new RuntimeException(e);
        }
      } catch (ClassNotFoundException | IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public Optional<String> getPubmedId(String url) {
    return getPubMedIdFromDoi(extractDoiFromUrl(url));
  }

  protected abstract String extractDoiFromUrl(String url);

  protected Optional<String> getPubMedIdFromDoi(String doi) {
    if (!this.cache.containsKey(doi)) {
      try {
        Map<String, String> result =
          PubMedArticleIdentifierMappingXmlParser.parse(
            new URL("https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/?ids=" + doi)
              .openStream()
          );
        if (result.containsKey(doi)) {
          String pubmed = result.get(doi);
          this.cache.put(doi, pubmed);
          this.saveCache();
          return Optional.of(pubmed);
        } else {
          throw new IOException();
        }
      } catch (IOException e) {
        LOGGER.warning("(Warning, the following DOI could not have been converted to PubMedID: " + doi);
        e.printStackTrace();
        return empty();
      }
    } else {
      return Optional.of(this.cache.get(doi));
    }
  }

  private void saveCache() {
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(this.doiCacheFile);
      new ObjectOutputStream(fos).writeObject(this.cache);
      fos.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String replace(String string, Map<String, String> replacements) {
    for (Entry<String, String> entry : replacements.entrySet()) {
      string = string.replace(entry.getKey(), entry.getValue());
    }

    return string;
  }
}
