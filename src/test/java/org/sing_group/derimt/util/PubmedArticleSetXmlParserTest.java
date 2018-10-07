package org.sing_group.derimt.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

public class PubmedArticleSetXmlParserTest {

  /**
   * The test XML file was obtained using the following query:
   * https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=22368089,30235322&retmode=xml
   */
  private final File testXmlFile = new File("src/test/resources/pubmed/efetch-query.xml");

  @Test
  public void testParseXml() throws FileNotFoundException {
    Map<String, PubmedArticleInfo> result = PubmedArticleSetXmlParser.parse(new FileInputStream(testXmlFile));
    assertEquals(new HashSet<>(asList("22368089", "30235322")), result.keySet());

    assertEquals(
      new PubmedArticleInfo(
        "22368089",
        "The development and activity-dependent expression of aggrecan in the cat visual cortex.",
        asList("Kind P C", "Sengpiel F", "Beaver C J", "Crocker-Buque A", "Kelly G M", "Matthews R T", "Mitchell D E"),
        "The Cat-301 monoclonal antibody identifies aggrecan, a chondroitin sulfate proteoglycan in the cat visual "
        + "cortex and dorsal lateral geniculate nucleus (dLGN). During development, aggrecan expression increases in "
        + "the dLGN with a time course that matches the decline in plasticity. Moreover, examination of tissue from "
        + "selectively visually deprived cats shows that expression is activity dependent, suggesting a role for "
        + "aggrecan in the termination of the sensitive period. Here, we demonstrate for the first time that the "
        + "onset of aggrecan expression in area 17 also correlates with the decline in experience-dependent "
        + "plasticity in visual cortex and that this expression is experience dependent. Dark rearing until 15 "
        + "weeks of age dramatically reduced the density of aggrecan-positive neurons in the extragranular layers, "
        + "but not in layer IV. This effect was reversible as dark-reared animals that were subsequently exposed to "
        + "light showed normal numbers of Cat-301-positive cells. The reduction in aggrecan following certain early "
        + "deprivation regimens is the first biochemical correlate of the functional changes to the γ-aminobutyric "
        + "acidergic system that have been reported following early deprivation in cats."
      ), result.get("22368089")
    );
  }
}
