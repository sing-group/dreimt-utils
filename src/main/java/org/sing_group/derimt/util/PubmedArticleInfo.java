package org.sing_group.derimt.util;

import static java.util.stream.Collectors.joining;

import java.util.List;

public class PubmedArticleInfo {
  private String pubmedId;
  private String articleTitle;
  private List<String> authors;
  private String articleAbstract;

  PubmedArticleInfo(String pubmedId, String articleTitle, List<String> authors, String articleAbstract) {
    this.pubmedId = pubmedId;
    this.articleTitle = articleTitle;
    this.authors = authors;
    this.articleAbstract = articleAbstract;
  }

  public String getPubmedId() {
    return pubmedId;
  }

  public String getArticleTitle() {
    return articleTitle;
  }

  public List<String> getAuthors() {
    return authors;
  }
  
  public String getAuthorsString() {
    return this.authors.stream().collect(joining(", "));
  }

  public String getArticleAbstract() {
    return articleAbstract;
  }

  @Override
  public String toString() {
    return "PMID = " + this.pubmedId + "[" + this.articleTitle + "]"
      + this.authors.stream().collect(joining(", ", " (", ")"));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((articleAbstract == null) ? 0 : articleAbstract.hashCode());
    result = prime * result + ((articleTitle == null) ? 0 : articleTitle.hashCode());
    result = prime * result + ((authors == null) ? 0 : authors.hashCode());
    result = prime * result + ((pubmedId == null) ? 0 : pubmedId.hashCode());
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
    PubmedArticleInfo other = (PubmedArticleInfo) obj;
    if (articleAbstract == null) {
      if (other.articleAbstract != null)
        return false;
    } else if (!articleAbstract.equals(other.articleAbstract))
      return false;
    if (articleTitle == null) {
      if (other.articleTitle != null)
        return false;
    } else if (!articleTitle.equals(other.articleTitle))
      return false;
    if (authors == null) {
      if (other.authors != null)
        return false;
    } else if (!authors.equals(other.authors))
      return false;
    if (pubmedId == null) {
      if (other.pubmedId != null)
        return false;
    } else if (!pubmedId.equals(other.pubmedId))
      return false;
    return true;
  }
}
