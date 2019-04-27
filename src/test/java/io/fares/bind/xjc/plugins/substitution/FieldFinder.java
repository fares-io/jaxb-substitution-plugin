package io.fares.bind.xjc.plugins.substitution;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.sun.istack.NotNull;

public class FieldFinder extends VoidVisitorAdapter<FieldCollector> {

  private final String name;

  public FieldFinder(@NotNull String name) {
    this.name = name;
  }

  @Override
  public void visit(FieldDeclaration n, FieldCollector collector) {

    super.visit(n, collector);

    boolean isExtensionField = n.getVariables().stream()
      .map(VariableDeclarator::getId)
      .map(VariableDeclaratorId::getName)
      .anyMatch(name::equals);

    if (isExtensionField) {
      collector.setField(n);
      // now find us all jaxb annotations for that field
      n.accept(new XmlElementAnnotationsFinder(), collector);
    }

  }

  public static FieldCollector findField(CompilationUnit unit, String fieldName) {
    FieldCollector collector = new FieldCollector();
    new FieldFinder(fieldName).visit(unit, collector);
    return collector;
  }

}
