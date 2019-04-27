package io.fares.bind.xjc.plugins.substitution;

import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class SubstitutionCandidate {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private final CClassInfo classInfo;

  private final List<SubstitutionProperty> properties = new LinkedList<>();

  public SubstitutionCandidate(CClassInfo classInfo, List<SubstitutionProperty> properties) {
    this.classInfo = classInfo;
    this.properties.addAll(properties);
  }

  public List<SubstitutionProperty> getProperties() {
    return Collections.unmodifiableList(properties);
  }

  public void modifyStage1() {
    for (SubstitutionProperty property : properties) {
      property.modifyStage1();
    }
  }

  public void modifyStage2(Outline outline) {

    // get a reference to the generated class
    ClassOutline classOutline = outline.getClazz(classInfo);

    logger.info("");
    logger.info(format("inspecting %s", classOutline.implClass.fullName()));

    // list of fields to pick the customised ones from
    Map<String, JFieldVar> classFields = classOutline.ref.fields();

    for (SubstitutionProperty property : properties) {
      JFieldVar field = classFields.get(property.getFieldName());
      if (field == null) {
        property.ignoreStage2(" field was not generated");
      } else {
        property.modifyStage2(outline, field);
      }
    }

  }

}
