package org.sing_group.derimt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class parses the XML file that can be obtained using EFetch utility from the NCBI E-utilities tools
 * (https://www.ncbi.nlm.nih.gov/books/NBK25499/#chapter4.EFetch).
 * 
 * This utility can retrieve Pubmed information for one or several articles using queries like:
 * https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=22368089,30235322&retmode=xml
 * 
 * @author hlfernandez
 *
 */
public class PubmedArticleSetXmlParser {
  public static Map<String, PubmedArticleInfo> parse(InputStream xmlInputStream, boolean acceptMissingAbstracts) {
    Map<String, PubmedArticleInfo> toret = new HashMap<>();
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
      NodeList articleNodes = document.getElementsByTagName("PubmedArticle");

      for (int articleIndex = 0; articleIndex < articleNodes.getLength(); articleIndex++) {
        Element articleNode = (Element) articleNodes.item(articleIndex);

        NodeList medlineCitations = articleNode.getElementsByTagName("MedlineCitation");
        if (medlineCitations.getLength() < 1) {
          continue;
        }

        Element medlineCitation = (Element) medlineCitations.item(0);

        NodeList pmidNodeList = medlineCitation.getElementsByTagName("PMID");
        if (pmidNodeList.getLength() < 1) {
          continue;
        }

        String pmid = pmidNodeList.item(0).getTextContent();

        NodeList articleTitleNodeList = medlineCitation.getElementsByTagName("ArticleTitle");
        if (articleTitleNodeList.getLength() < 1) {
          continue;
        }

        String articleTitle = articleTitleNodeList.item(0).getTextContent();

        NodeList abstractNodeList = medlineCitation.getElementsByTagName("AbstractText");
        String articleAbstract = "NA";
        if (abstractNodeList.getLength() < 1) {
          if (!acceptMissingAbstracts) {
            continue;
          }
        } else {
          articleAbstract = abstractNodeList.item(0).getTextContent();
        }

        NodeList authorNodeList = medlineCitation.getElementsByTagName("Author");
        if (authorNodeList.getLength() < 1) {
          continue;
        }

        List<String> authorsList = new LinkedList<>();
        for (int i = 0; i < authorNodeList.getLength(); i++) {
          Element author = (Element) authorNodeList.item(i);

          NodeList lastNameNodeList = author.getElementsByTagName("LastName");
          if (lastNameNodeList.getLength() < 1) {
            continue;
          }

          NodeList nameNodeList = author.getElementsByTagName("ForeName");
          if (nameNodeList.getLength() < 1) {
            continue;
          }

          authorsList.add(lastNameNodeList.item(0).getTextContent() + " " + nameNodeList.item(0).getTextContent());
        }

        toret.put(pmid, new PubmedArticleInfo(pmid, articleTitle, authorsList, articleAbstract));
      }
    } catch (SAXException | IOException | ParserConfigurationException e) {
      e.printStackTrace();
    }

    return toret;
  }
}
