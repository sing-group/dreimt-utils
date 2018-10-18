package org.sing_group.derimt.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
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
    int lineCount = 0;
    List<String> headerLines = new LinkedList<>();

    try (
      BufferedReader br = new BufferedReader(new FileReader(tsvInput));
      BufferedWriter bw = Files.newBufferedWriter(tsvOutput.toPath(), FileUtils.getOpenOptions(appendOutput))
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (lineCount < this.headerLines) {
          lineCount++;
          headerLines.add(line);

          if (lineCount == this.headerLines) {
            headerLines.forEach(l -> {
              try {
                bw.write(l + "\tPubMedID\n");
              } catch (IOException e) {
                LOGGER.warning("Exception writing header line: " + l);
                e.printStackTrace();
              }
            });
            bw.flush();
          }

          continue;
        }

        Optional<TsvLine> optLine = TsvLine.from(line, this.linkColumn);
        if (!optLine.isPresent()) {
          LOGGER.warning("Ignore line with invalid format: " + line);
          continue;
        }

        Optional<PubmedIdFinder> pubmedIdFinder = PubmedIdFinder.getFinderForUrl(optLine.get().getUrl());

        String pubMedId = "";
        if (!pubmedIdFinder.isPresent()) {
          pubMedId = UNRECOGNISED_LINK;
        } else {
          Optional<String> pubmedId = pubmedIdFinder.get().getPubmedId(optLine.get().getUrl());
          if (!pubmedId.isPresent()) {
            pubMedId = NA_ID;
          } else {
            pubMedId = pubmedId.get();
          }
        }

        try {
          bw.write(optLine.get().getLine() + "\t" + pubMedId + "\n");
          bw.flush();
        } catch (IOException e) {
          LOGGER.warning("Exception processing ID: " + pubMedId);
          e.printStackTrace();
        }
      }
    }
  }

  private static class TsvLine {
    private String line;
    private String url;

    public TsvLine(String line, String url) {
      this.line = line;
      this.url = url;
    }

    public String getLine() {
      return line;
    }

    public String getUrl() {
      return url;
    }

    public static Optional<TsvLine> from(String line, int linkColumn) {
      String[] split = line.split("\t");
      if (split.length >= linkColumn) {
        return of(new TsvLine(line, split[linkColumn]));
      }
      return empty();
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
