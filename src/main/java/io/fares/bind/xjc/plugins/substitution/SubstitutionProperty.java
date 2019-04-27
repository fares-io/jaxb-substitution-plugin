package io.fares.bind.xjc.plugins.substitution;

import com.sun.codemodel.JFieldVar;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.Outline;

public interface SubstitutionProperty<P extends CPropertyInfo, T extends CElement> {

  @NotNull
  P getProperty();

  @NotNull
  T getTypeReference();

  @NotNull
  Class<P> getType();

  String getFieldName();

  void modifyStage1();

  void ignoreStage2(String reason);

  void modifyStage2(Outline outline, JFieldVar field);

}
