package org.sing_group.derimt.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

public class PubMedArticleIdentifierMappingXmlParserTest {

  /**
   * The test XML file was obtained using the following query:
   * https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/?ids=10.1093/nar/gks1195
   */
  private final File testXmlFile = new File("src/test/resources/pubmed/article-identifier-mapping-query.xml");

  @Test
  public void testParseXml() throws FileNotFoundException {
    Map<String, String> result = PubMedArticleIdentifierMappingXmlParser.parse(new FileInputStream(testXmlFile));
    assertEquals(new HashSet<>(asList("10.1093/nar/gks1195")), result.keySet());
    assertEquals("23193287", result.get("10.1093/nar/gks1195"));
  }
}
