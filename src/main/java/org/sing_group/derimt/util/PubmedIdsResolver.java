package org.sing_group.derimt.util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PubmedIdsResolver {
  private static final Logger LOGGER = Logger.getLogger(PubmedIdsResolver.class.getName());

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

  private void processPubmedIdsList(File inputFile, File outputFile, int batchSize) throws IOException {
    List<String> pubmedIdsList = Files.readAllLines(inputFile.toPath()).stream().collect(Collectors.toList());

    List<List<String>> batchLists = new HashSet<String>(pubmedIdsList).stream().collect(blockCollector(batchSize));
    Map<String, PubmedArticleInfo> pubmedIds = new HashMap<>();
    for (List<String> batchList : batchLists) {
      try {
        Thread.sleep(1000); /**
                             * Wait 1000ms between queries so that NCBI do not think we are being abusive, even with the
                             * API key ;-)
                             **/

        Map<String, PubmedArticleInfo> currentIds = this.resolve(batchList);

        if (currentIds.keySet().size() != batchList.size()) {
          LOGGER.warning("Warning: the following PubMed IDs could not have been resolved: ");
          Set<String> missingIds = new HashSet<>(batchList);
          missingIds.removeAll(currentIds.keySet());
          missingIds.forEach(id -> LOGGER.warning("\t" + id));
        }
        pubmedIds.putAll(currentIds);
      } catch (Exception e) {
        LOGGER.warning("Error retrieving Pubmed IDs for: " + batchList);
        e.printStackTrace();
      }
    }

    BufferedWriter bw = Files.newBufferedWriter(outputFile.toPath(), FileUtils.getOpenOptions(false));
    bw.write("PubMedID\tTitle\tAuthors\tAbstract\n");

    for (String pmid : pubmedIdsList) {
      PubmedArticleInfo articleInfo =
        pubmedIds.getOrDefault(pmid, new PubmedArticleInfo(pmid, "NA", Arrays.asList("NA"), "NA"));
      bw.write(
        pmid + "\t" + articleInfo.getArticleTitle() + "\t" + articleInfo.getAuthorsString() + "\t"
          + articleInfo.getArticleAbstract() + "\n"
      );
      bw.flush();
    }

    bw.close();
  }

  public static Collector<String, List<List<String>>, List<List<String>>> blockCollector(int blockSize) {
    return Collector.of(
      ArrayList<List<String>>::new,
      (list, value) -> {
        List<String> block = (list.isEmpty() ? null : list.get(list.size() - 1));
        if (block == null || block.size() == blockSize)
          list.add(block = new ArrayList<>(blockSize));
        block.add(value);
      },
      (r1, r2) -> {
        throw new UnsupportedOperationException("Parallel processing not supported");
      }
    );
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Invalid number of arguments.");
      System.err.println(
        "This operation requires two arguments: </path/to/input-file.txt> </path/to/output-file.tsv>"
      );
      System.exit(1);
    }

    File inputFile = new File(args[0]);
    File outputFile = new File(args[1]);
    int batchSize = 150;

    try {
      new PubmedIdsResolver().processPubmedIdsList(inputFile, outputFile, batchSize);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
