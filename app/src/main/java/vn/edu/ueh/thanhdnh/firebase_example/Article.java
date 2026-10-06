package vn.edu.ueh.thanhdnh.firebase_example;

public class Article {

  private String id;
  private String title;
  private String content;
  private String imageBase64;

  public Article() {
  }

  public Article(
          String id,
          String title,
          String content,
          String imageBase64
  ) {

    this.id = id;
    this.title = title;
    this.content = content;
    this.imageBase64 = imageBase64;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(
          String title
  ) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(
          String content
  ) {
    this.content = content;
  }

  public String getImageBase64() {
    return imageBase64;
  }

  public void setImageBase64(
          String imageBase64
  ) {
    this.imageBase64 = imageBase64;
  }
}