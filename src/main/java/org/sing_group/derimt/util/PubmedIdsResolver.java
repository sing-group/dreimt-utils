package org.sing_group.derimt.util;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class PubmedIdsResolver {

  public static Map<String, PubmedArticleInfo> resolve(List<String> pubmedIds)
    throws MalformedURLException, IOException {
    return PubmedArticleSetXmlParser.parse(
      new URL(
        "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="
          + pubmedIds.stream().collect(joining(",")) + "&retmode=xml"
      ).openStream()
    );
  }
}
