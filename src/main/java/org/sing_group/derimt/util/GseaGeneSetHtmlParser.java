package org.sing_group.derimt.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * This class parses the HTML contents of GSEA Gene Set description pages (such as
 * http://software.broadinstitute.org/gsea/msigdb/cards/KAECH_NAIVE_VS_DAY8_EFF_CD8_TCELL_DN).
 * 
 * @author hlfernandez
 *
 */
public class GseaGeneSetHtmlParser {

  private static final String PUBMED = "Pubmed ";

  public static Optional<String> getPubmedId(InputStream htmlInputStream) {
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
      e.printStackTrace();
      return empty();
    }
  }
}
