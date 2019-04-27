package io.fares.bind.xjc.plugins.substitution;

import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class XmlElementAnnotationsFinder extends VoidVisitorAdapter<FieldCollector> {

  /**
   * Used to collect the XmlElement or XmlElementRef annotations
   *
   * @param n         the node visited
   * @param collector collecting the information on the field
   */
  @Override
  public void visit(NormalAnnotationExpr n, FieldCollector collector) {

    super.visit(n, collector);

    if (n.getName().getName().startsWith("XmlElement")) {
      collector.addAnnotation(n);
    }

  }

  @Override
  public void visit(final SingleMemberAnnotationExpr n, final FieldCollector collector) {

    super.visit(n, collector);

    if (n.getName().getName().startsWith("XmlElement")) {
      collector.addAnnotation(n);
    }

  }

}
