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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collector;

public class GeneSetsLinksResolver {
  private static final Logger LOGGER = Logger.getLogger(GeneSetsLinksResolver.class.getName());
  private static final String NA_ID = "NA";

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

      Optional<String> pubmedId = GseaGeneSetHtmlParser.getPubmedId(new URL(optLine.get().getUrl()), 3);
      if (!pubmedId.isPresent()) {
        lineToPmid.put(optLine.get(), NA_ID);
      } else {
        lineToPmid.put(optLine.get(), pubmedId.get());
      }
    }

    Set<String> pubmedIdsSet = new HashSet<>(lineToPmid.values());
    pubmedIdsSet.remove(NA_ID);
    List<List<String>> batchLists = pubmedIdsSet.stream().collect(blockCollector(this.batchSize));
    Map<String, PubmedArticleInfo> pubmedIds = new HashMap<>();
    for (List<String> batchList : batchLists) {
      try {
        Thread.sleep(1000);
        Map<String, PubmedArticleInfo> currentIds = PubmedIdsResolver.resolve(batchList);
        if (currentIds.keySet().size() != batchList.size()) {
          Set<String> missingIds = new HashSet<>(batchList);
          missingIds.removeAll(currentIds.keySet());
          missingIds.forEach(id -> LOGGER.warning("\t" + id));
        }
        pubmedIds.putAll(currentIds);
      } catch (Exception e) {
        LOGGER.warning("Error retrieving Pubmed IDs for: " + batchList);
      }
    }

    BufferedWriter bw = Files.newBufferedWriter(tsvOutput.toPath(), getOpenOptions(appendOutput));
    bw.write("Signature\tLink\tPubMedID\tTitle\tAuthors\tAbstract\n");

    lineToPmid.keySet().stream().sorted(new Comparator<TsvLine>() {

      @Override
      public int compare(TsvLine o1, TsvLine o2) {
        return Integer.compare(o1.getInstanceId(), o2.getInstanceId());
      }
    }).forEach(tsvLine -> {
      String pubmedId = lineToPmid.get(tsvLine);
      PubmedArticleInfo articleInfo =
        pubmedIds.getOrDefault(pubmedId, new PubmedArticleInfo("NA", "NA", Collections.emptyList(), "NA"));
      try {
        bw.write(
          (tsvLine.getName() + "\t" + tsvLine.getUrl() + "\t" + pubmedId + "\t" + articleInfo.getArticleTitle()
            + "\t" + articleInfo.getAuthorsString() + "\t" + articleInfo.getArticleAbstract() + "\n")
        );
        bw.flush();
      } catch (IOException e) {
        LOGGER.warning("Exception processing ID: " + pubmedId);
        e.printStackTrace();
      }
    });

    try {
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
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
    private static int instances = 0;

    private int instanceId;
    private String name;
    private String url;

    public TsvLine(String name, String url) {
      this.instanceId = (++instances);
      this.name = name;
      this.url = url;
    }

    public String getName() {
      return name;
    }

    public String getUrl() {
      return url;
    }

    public int getInstanceId() {
      return instanceId;
    }

    public static Optional<TsvLine> from(String line) {
      String[] split = line.split("\t");
      if (split.length == 2) {
        return of(new TsvLine(split[0], split[1]));
      }
      return empty();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + instanceId;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TsvLine other = (TsvLine) obj;
      if (instanceId != other.instanceId)
        return false;
      return true;
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
