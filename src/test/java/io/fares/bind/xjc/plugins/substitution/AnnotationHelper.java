package io.fares.bind.xjc.plugins.substitution;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public interface AnnotationHelper {

  BiPredicate<Expression, Class<?>> isInstanceOf = (expression, type) -> expression != null && type != null && type.isAssignableFrom(expression.getClass());

  default <T> Optional<T> getValue(NormalAnnotationExpr ann, String name, Class<T> type) {

    return ann
      .getPairs()
      .stream()
      .filter(p -> name.equals(p.getName()))
      .map(MemberValuePair::getValue)
      .filter(e -> isInstanceOf.test(e, type))
      .map(type::cast)
      .findAny();

  }

  default void verifyField(String typeName,
                           String fieldName,
                           String expectedXmlElementName,
                           String expectedTypeName,
                           Class<? extends Annotation> expectedXmlAnnotationType,
                           boolean expectedOptional,
                           Supplier<Path> generatedPathProvider) throws ParseException, IOException {

    CompilationUnit unit = JavaParser.parse(generatedPathProvider.get().resolve(typeName.replace(".", File.separator) + ".java").toFile());

    FieldCollector collector = FieldFinder.findField(unit, fieldName);

    assertTrue(collector.hasField(), "field " + fieldName + " not present on compiled class");

    // we expect the extension field to be of type test.Extension
    Optional<ClassOrInterfaceType> implType = collector.getFieldImplementationType();
    assertTrue(implType.isPresent(), "field is not a class reference but primitive");
    assertEquals(expectedTypeName, implType.get().toStringWithoutComments());

    // test expected annotation
    Optional<AnnotationExpr> ann = collector.getAnnotation(expectedXmlAnnotationType);
    assertTrue(ann.isPresent(), "no @" + expectedXmlAnnotationType.getSimpleName() + " annotation present on field");

    if (ann.get() instanceof NormalAnnotationExpr) {

      NormalAnnotationExpr annotationExpr = (NormalAnnotationExpr) ann.get();

      // the annotation name field must match Extension
      Optional<StringLiteralExpr> name = getValue(annotationExpr, "name", StringLiteralExpr.class);
      assertTrue(name.isPresent());
      assertEquals(expectedXmlElementName, name.get().getValue());

      if (expectedOptional) {
        // the required field must not exist
        Optional<BooleanLiteralExpr> required = getValue(annotationExpr, "required", BooleanLiteralExpr.class);
        assertFalse(required.isPresent(), "element must not be required");
      } else {
        // the required field must be true
        Optional<BooleanLiteralExpr> required = getValue(annotationExpr, "required", BooleanLiteralExpr.class);
        assertTrue(required.isPresent(), "element must be required");
        assertTrue(required.get().getValue());
      }

    } else if (ann.get() instanceof SingleMemberAnnotationExpr) {
      SingleMemberAnnotationExpr annotationExpr = (SingleMemberAnnotationExpr) ann.get();
    }

  }

  default void verifyOptionalField(String typeName,
                                   String fieldName,
                                   String expectedXmlElementName,
                                   String expectedTypeName,
                                   Class<? extends Annotation> expectedXmlAnnotationType,
                                   Supplier<Path> generatedPathProvider) throws ParseException, IOException {

    verifyField(typeName, fieldName, expectedXmlElementName, expectedTypeName, expectedXmlAnnotationType, true, generatedPathProvider);

  }

  default void verifyRequiredField(String typeName,
                                   String fieldName,
                                   String expectedXmlElementName,
                                   String expectedTypeName,
                                   Class<? extends Annotation> expectedXmlAnnotationType,
                                   Supplier<Path> generatedPathProvider) throws ParseException, IOException {

    verifyField(typeName, fieldName, expectedXmlElementName, expectedTypeName, expectedXmlAnnotationType, false, generatedPathProvider);

  }

}
