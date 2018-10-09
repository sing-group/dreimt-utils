package org.sing_group.derimt.util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PubmedIdsResolver {
  private File pubmedCacheFile;
  private Map<String, PubmedArticleInfo> cache = new HashMap<>();

  public PubmedIdsResolver() {
    this(new File("data/pubmed-ids-cache.dat"));
  }

  public PubmedIdsResolver(File pubmedCacheFile) {
    this.pubmedCacheFile = pubmedCacheFile;
    this.restoreCache();
  }

  private void restoreCache() {
    if (this.pubmedCacheFile.exists()) {
      try {
        FileInputStream fis = new FileInputStream(pubmedCacheFile);
        Object cache = new ObjectInputStream(fis).readObject();
        fis.close();

        try {
          @SuppressWarnings("unchecked")
          Map<String, PubmedArticleInfo> map = (Map<String, PubmedArticleInfo>) cache;
          this.cache.putAll(map);
        } catch (ClassCastException e) {
          throw new RuntimeException(e);
        }
      } catch (ClassNotFoundException | IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Map<String, PubmedArticleInfo> resolve(List<String> pubmedIds)
    throws MalformedURLException, IOException {

    List<String> unCachedIds = pubmedIds.stream().filter(id -> !cache.containsKey(id)).collect(toList());

    if (!unCachedIds.isEmpty()) {
      Map<String, PubmedArticleInfo> result =
        PubmedArticleSetXmlParser.parse(
          new URL(
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="
              + unCachedIds.stream().collect(joining(","))
              + "&api_key=bc6d130c00d3983b05e6667c2e54df5beb08"
              + "&retmode=xml"
          ).openStream(),
          true
        );
      this.cache.putAll(result);
      this.saveCache();
    }

    return this.cache.entrySet().stream().filter(e -> pubmedIds.contains(e.getKey()))
      .collect(toMap(e -> e.getKey(), e -> e.getValue()));
  }

  private void saveCache() {
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(this.pubmedCacheFile);
      new ObjectOutputStream(fos).writeObject(this.cache);
      fos.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws MalformedURLException, IOException {
    new PubmedIdsResolver()
      .resolve(Arrays.asList("19535937", "22368089", "30235322")).entrySet().forEach(e -> {
        System.err.println(e.getKey() + "\t" + e.getValue().getPubmedId() + "\t" + e.getValue().getArticleTitle());
      });
  }
}
