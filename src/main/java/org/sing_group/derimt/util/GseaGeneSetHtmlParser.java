package org.sing_group.derimt.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class parses the HTML contents of GSEA Gene Set description pages (such as
 * http://software.broadinstitute.org/gsea/msigdb/cards/KAECH_NAIVE_VS_DAY8_EFF_CD8_TCELL_DN).
 * 
 * @author hlfernandez
 *
 */
public class GseaGeneSetHtmlParser implements PubmedIdFinder {
  private static final String PUBMED = "Pubmed ";
  private static final String NOT_AVAILABLE = "NOT_AVAILABLE";

  private File gseaCacheFile;
  private Map<String, String> cache = new HashMap<>();

  public GseaGeneSetHtmlParser() {
    this(new File("data/gsea-pubmed-ids-cache.dat"));
  }

  public GseaGeneSetHtmlParser(File gseaCacheFile) {
    this.gseaCacheFile = gseaCacheFile;
    this.restoreCache();
  }

  private void restoreCache() {
    if (this.gseaCacheFile.exists()) {
      try {
        FileInputStream fis = new FileInputStream(gseaCacheFile);
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
  public Optional<String> getPubmedId(String url) throws MalformedURLException {
    url = fixUrl(url);

    if (this.cache.containsKey(url)) {
      String cacheValue = this.cache.get(url);
      return cacheValue.equals(NOT_AVAILABLE) ? Optional.empty() : Optional.of(cacheValue);
    } else {
      return saveInCache(url, getPubmedId(new URL(url), 3));
    }
  }

  private static final String fixUrl(String url) {
    return url.replace(
      "http://www.broadinstitute.org/gsea/msigdb/cards/", "http://software.broadinstitute.org/gsea/msigdb/cards/"
    );
  }

  private Optional<String> getPubmedId(URL url, int attempts) {
    int attemptCount = 0;
    do {
      try {
        Optional<String> result = getPubmedId(url.openStream());
        return result;
      } catch (IOException e1) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    } while (attemptCount++ < attempts);
    throw new RuntimeException("Cannot connect to " + url);
  }

  public Optional<String> getPubmedId(File htmlFile) throws FileNotFoundException {
    return getPubmedId(new FileInputStream(htmlFile));
  }

  private Optional<String> getPubmedId(InputStream htmlInputStream) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(htmlInputStream, "ISO-8859-1"));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains(PUBMED)) {
          return of(line.substring(line.indexOf(PUBMED) + PUBMED.length(), line.indexOf("</a>")).trim());
        }
      }
      return empty();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<String> saveInCache(String url, Optional<String> value) {
    this.cache.put(url, value.orElse(NOT_AVAILABLE));
    this.saveCache();
    return value;
  }

  private void saveCache() {
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(this.gseaCacheFile);
      new ObjectOutputStream(fos).writeObject(this.cache);
      fos.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
