package org.sing_group.derimt.util;

import static java.nio.file.Files.createTempFile;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CellHtmlParser implements PubmedIdFinder {
  private static final String NOT_AVAILABLE = "NOT_AVAILABLE";

  private File cacheFile;
  private Map<String, String> cache = new HashMap<>();

  public CellHtmlParser() {
    this(new File("data/cell-pubmed-ids-cache.dat"));
  }

  public CellHtmlParser(File cacheFile) {
    this.cacheFile = cacheFile;
    this.restoreCache();
  }

  private void restoreCache() {
    if (this.cacheFile.exists()) {
      try {
        FileInputStream fis = new FileInputStream(cacheFile);
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
    if (this.cache.containsKey(url)) {
      String cacheValue = this.cache.get(url);
      return cacheValue.equals(NOT_AVAILABLE) ? Optional.empty() : Optional.of(cacheValue);
    }
    try {
      File tempFile = createTempFile("cell-html-parser-", ".html").toFile();
      new ProcessBuilder(asList("wget", url, "-O", tempFile.getAbsolutePath()))
        .start()
        .waitFor();
      return saveInCache(url, getPubmedId(tempFile));
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public Optional<String> getPubmedId(File htmlFile) {
    Document document;
    try {
      document = Jsoup.parse(htmlFile, "UTF-8");

      Elements metaTags = document.getElementsByAttributeValue("name", "citation_pmid");
      if (metaTags.size() > 0) {
        return Optional.of(metaTags.get(0).attr("content"));
      } else {
        return Optional.empty();
      }
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
      fos = new FileOutputStream(this.cacheFile);
      new ObjectOutputStream(fos).writeObject(this.cache);
      fos.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws MalformedURLException {
    System.err
      .println(new CellHtmlParser().getPubmedId("https://www.cell.com/immunity/fulltext/S1074-7613(16)30432-0"));
  }
}
