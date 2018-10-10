package org.sing_group.derimt.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.junit.Test;

public class GseaGeneSetHtmlParserTest {

  /**
   * The test HTML file was obtained from:
   * http://software.broadinstitute.org/gsea/msigdb/cards/KAECH_NAIVE_VS_DAY8_EFF_CD8_TCELL_DN
   */
  private final File testHtmlFile = new File("src/test/resources/gsea/geneset.html");

  @Test
  public void testParseXml() throws FileNotFoundException {
    Optional<String> result = new GseaGeneSetHtmlParser().getPubmedId(testHtmlFile);
    assertEquals("12526810", result.get());
  }
}
