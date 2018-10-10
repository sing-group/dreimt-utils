package org.sing_group.derimt.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class GeneSetsPubmedIdResolver {
  private static final Logger LOGGER = Logger.getLogger(GeneSetsPubmedIdResolver.class.getName());
  private static final String UNRECOGNISED_LINK = "Unrecognised Link";
  private static final String NA_ID = "NA";

  private int headerLines;
  private int linkColumn;

  public GeneSetsPubmedIdResolver(int headerLines, int linkColumn) {
    this.headerLines = headerLines;
    this.linkColumn = linkColumn;
  }

  public void process(File tsvInput, File tsvOutput, boolean appendOutput) throws IOException {
    List<String> inputLines = Files.readAllLines(tsvInput.toPath());
    List<String> headerLines = inputLines.subList(0, this.headerLines);
    inputLines = inputLines.subList(this.headerLines, inputLines.size());

    Map<TsvLine, String> lineToPmid = new HashMap<>();
    for (String line : inputLines) {
      Optional<TsvLine> optLine = TsvLine.from(line, this.linkColumn);
      if (!optLine.isPresent()) {
        LOGGER.warning("Ignore line with invalid format: " + line);
        continue;
      }

      Optional<PubmedIdFinder> pubmedIdFinder = PubmedIdFinder.getFinderForUrl(optLine.get().getUrl());

      if (!pubmedIdFinder.isPresent()) {
        lineToPmid.put(optLine.get(), UNRECOGNISED_LINK);
      } else {
        Optional<String> pubmedId = pubmedIdFinder.get().getPubmedId(optLine.get().getUrl());
        if (!pubmedId.isPresent()) {
          lineToPmid.put(optLine.get(), NA_ID);
        } else {
          lineToPmid.put(optLine.get(), pubmedId.get());
        }
      }
    }

    BufferedWriter bw = Files.newBufferedWriter(tsvOutput.toPath(), FileUtils.getOpenOptions(appendOutput));
    headerLines.forEach(l -> {
      try {
        bw.write(l + "\tPubMedID\n");
      } catch (IOException e) {}
    });

    lineToPmid.keySet().stream().sorted(new Comparator<TsvLine>() {

      @Override
      public int compare(TsvLine o1, TsvLine o2) {
        return Integer.compare(o1.getInstanceId(), o2.getInstanceId());
      }
    }).forEach(tsvLine -> {
      String pubmedId = lineToPmid.get(tsvLine);
      try {
        bw.write(tsvLine.getLine() + "\t" + pubmedId + "\n");
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

  private static class TsvLine {
    private static int instances = 0;

    private int instanceId;
    private String line;
    private String url;

    public TsvLine(String line, String url) {
      this.instanceId = (++instances);
      this.line = line;
      this.url = url;
    }

    public String getLine() {
      return line;
    }

    public String getUrl() {
      return url;
    }

    public int getInstanceId() {
      return instanceId;
    }

    public static Optional<TsvLine> from(String line, int linkColumn) {
      String[] split = line.split("\t");
      if (split.length >= linkColumn) {
        return of(new TsvLine(line, split[linkColumn]));
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

  public static void main(String[] args) {
    if (args.length != 4) {
      System.err.println("Invalid number of arguments.");
      System.err.println(
        "This operation requires four arguments: </path/to/input-file.tsv> </path/to/output-file.tsv> <link-column> <header-lines-count>"
      );
      System.exit(1);
    }

    File input = new File(args[0]);
    File output = new File(args[1]);
    int linkColumn = Integer.parseInt(args[2]);
    int headerLines = Integer.parseInt(args[3]);

    try {
      new GeneSetsPubmedIdResolver(headerLines, linkColumn).process(input, output, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
