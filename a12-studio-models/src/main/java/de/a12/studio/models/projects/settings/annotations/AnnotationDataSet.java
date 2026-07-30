package de.a12.studio.models.projects.settings.annotations;

public class AnnotationDataSet {
  private String name;
  private AnnotationHeaderSet headerSet = new AnnotationHeaderSet();
  private AnnotationFieldSet fieldSet = new AnnotationFieldSet();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AnnotationHeaderSet getHeaderSet() {
    return headerSet;
  }

  public void setHeaderSet(AnnotationHeaderSet headerSet) {
    this.headerSet = headerSet;
  }

  public AnnotationFieldSet getFieldSet() {
    return fieldSet;
  }

  public void setFieldSet(AnnotationFieldSet fieldSet) {
    this.fieldSet = fieldSet;
  }
}
