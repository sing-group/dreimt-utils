package org.sing_group.derimt.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collector;

public class GeneSetsLinksResolver {
  private static final Logger LOGGER = Logger.getLogger(GeneSetsLinksResolver.class.getName());

  private int headerLines;
  private int batchSize;

  public GeneSetsLinksResolver(int headerLines, int batchSize) {
    this.headerLines = headerLines;
    this.batchSize = batchSize;
  }

  public void process(File tsvInput, File tsvOutput, boolean appendOutput) throws IOException {
    List<String> inputLines = Files.readAllLines(tsvInput.toPath());
    inputLines = inputLines.subList(this.headerLines, inputLines.size());

    Map<TsvLine, String> lineToPmid = new HashMap<>();
    for (String line : inputLines) {
      Optional<TsvLine> optLine = TsvLine.from(line);
      if (!optLine.isPresent()) {
        LOGGER.warning("Ignore line with invalid format: " + line);
        continue;
      }

      Optional<String> pubmedId = GseaGeneSetHtmlParser.getPubmedId(new URL(optLine.get().getUrl()).openStream());
      if (!pubmedId.isPresent()) {
        LOGGER.warning("Pubmed ID not found for: " + line);
        continue;
      }

      lineToPmid.put(optLine.get(), pubmedId.get());
    }

    Set<String> pubmedIdsSet = new HashSet<>(lineToPmid.values());
    List<List<String>> batchLists = pubmedIdsSet.stream().collect(blockCollector(this.batchSize));
    Map<String, PubmedArticleInfo> pubmedIds = new HashMap<>();
    for (List<String> batchList : batchLists) {
      try {
        Map<String, PubmedArticleInfo> currentIds = PubmedIdsResolver.resolve(batchList);
        if (currentIds.keySet().size() != batchList.size()) {
          LOGGER.warning("Warning, the following Pubmed IDs could not have been converted: ");
          Set<String> missingIds = new HashSet<>(batchList);
          missingIds.removeAll(currentIds.keySet());
          missingIds.forEach(id -> LOGGER.warning("\t" + id));
        } else {
          pubmedIds.putAll(currentIds);
        }
      } catch (Exception e) {
        LOGGER.warning("Error retrieving Pubmed IDs for: " + batchList);
      }
    }

    lineToPmid.entrySet().forEach(e -> {
      System.err
        .println(e.getKey().getName() + "\t" + e.getValue() + "\t" + pubmedIds.get(e.getValue()).getArticleTitle() + "\t" + pubmedIds.get(e.getValue()).getAuthors());
    });

    BufferedWriter bw = Files.newBufferedWriter(tsvOutput.toPath(), getOpenOptions(appendOutput));
    bw.write("Signature\tLink\tPubMedID\tTitle\tAuthors\tAbstract\n");
    for (Entry<TsvLine, String> e : lineToPmid.entrySet()) {
      String pubmedId = e.getValue();
      PubmedArticleInfo articleInfo = pubmedIds.get(pubmedId);
      bw.write(
        (e.getKey().getName() + "\t" + e.getKey().getUrl() + "\t" + pubmedId + "\t" + articleInfo.getArticleTitle()
          + "\t" + articleInfo.getAuthorsString() + "\t" + articleInfo.getArticleAbstract() + "\n")
      );
    }
  }

  private static OpenOption[] getOpenOptions(boolean append) {
    List<OpenOption> openOptions = new LinkedList<>();
    openOptions.add(StandardOpenOption.CREATE);
    if (append) {
      openOptions.add(StandardOpenOption.CREATE);
    }
    return openOptions.toArray(new OpenOption[openOptions.size()]);
  }

  private static class TsvLine {
    private String name;
    private String url;

    public TsvLine(String name, String url) {
      this.name = name;
      this.url = url;
    }

    public String getName() {
      return name;
    }

    public String getUrl() {
      return url;
    }

    public static Optional<TsvLine> from(String line) {
      String[] split = line.split("\t");
      if (split.length == 2) {
        return of(new TsvLine(split[0], split[1]));
      }
      return empty();
    }
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
}
