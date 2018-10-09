package org.sing_group.derimt.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.junit.Test;

public class CellSetHtmlParserTest {

  /**
   * The test HTML file was obtained (using wget) from: https://www.cell.com/immunity/fulltext/S1074-7613(16)30432-0
   */
  private final File testHtmlFile = new File("src/test/resources/cell/article.html");

  @Test
  public void testParseXml() throws FileNotFoundException {
    Optional<String> result = new CellHtmlParser().getPubmedId(testHtmlFile);
    assertEquals("27851914", result.get());
  }
}
