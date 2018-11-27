package org.sing_group.derimt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PubMedArticleIdentifierMappingXmlParser {

  public static Map<String, String> parse(InputStream xmlInputStream) {
    Map<String, String> toret = new HashMap<>();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    dbf.setNamespaceAware(false);
    dbf.setValidating(false);
    try {
      dbf.setFeature("http://xml.org/sax/features/namespaces", false);
      dbf.setFeature("http://xml.org/sax/features/validation", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    } catch (ParserConfigurationException e) {}

    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.parse(xmlInputStream);
      NodeList recordNodes = document.getElementsByTagName("record");
      for (int i = 0; i < recordNodes.getLength(); i++) {
        Element currentRecordNode = (Element) recordNodes.item(i);
        String doi = currentRecordNode.getAttribute("doi");
        String pubmedId = currentRecordNode.getAttribute("pmid");
        if (pubmedId != null && !pubmedId.isEmpty()) {
          toret.put(doi, pubmedId);
        }
      }
    } catch (SAXException | IOException | ParserConfigurationException e) {
      e.printStackTrace();
    }

    return toret;
  }
}
