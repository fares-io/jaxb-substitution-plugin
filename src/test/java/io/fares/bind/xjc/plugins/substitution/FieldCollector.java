package io.fares.bind.xjc.plugins.substitution;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

public class FieldCollector {

  private FieldDeclaration field;

  private final List<AnnotationExpr> annotations = new LinkedList<>();

  public boolean hasAnnotations() {
    return !annotations.isEmpty();
  }

  public List<AnnotationExpr> getAnnotations() {
    return Collections.unmodifiableList(annotations);
  }

  public void addAnnotation(AnnotationExpr annotation) {
    this.annotations.add(annotation);
  }

  public boolean hasField() {
    return field != null;
  }

  public FieldDeclaration getField() {
    return field;
  }

  public void setField(FieldDeclaration field) {
    this.field = field;
  }

  public Optional<ClassOrInterfaceType> getFieldImplementationType() {

    return Stream.of(field)
      .filter(Objects::nonNull)
      .map(FieldDeclaration::getElementType)
      .filter(t -> t instanceof ReferenceType)
      .map(ReferenceType.class::cast)
      .map(ReferenceType::getElementType)
      .filter(t -> t instanceof ClassOrInterfaceType)
      .map(ClassOrInterfaceType.class::cast)
      .findAny();

  }

  public Optional<AnnotationExpr> getAnnotation(Class<? extends Annotation> annotation) {

    return annotations.stream()
      .filter(Objects::nonNull)
      .filter(a -> a.getName().getIdentifier().equals(annotation.getSimpleName()))
      .findAny();

  }

}
