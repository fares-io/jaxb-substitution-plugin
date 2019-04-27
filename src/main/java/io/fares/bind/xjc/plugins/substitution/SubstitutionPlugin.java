/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package io.fares.bind.xjc.plugins.substitution;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.xml.sax.ErrorHandler;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.String.format;

public class SubstitutionPlugin extends AbstractParameterizablePlugin {

  public static final String NS = "http://jaxb2-commons.dev.java.net/basic/substitution";

  public static final String SUBSTITUTION_HEAD = "head";

  public static final QName SUBSTITUTION_HEAD_NAME = new QName(NS, SUBSTITUTION_HEAD);

  public static final String SUBSTITUTION_HEAD_REF = "head-ref";

  public static final QName SUBSTITUTION_HEAD_REF_NAME = new QName(NS, SUBSTITUTION_HEAD_REF);

  private final Map<CClassInfo, SubstitutionCandidate> candidates = new LinkedHashMap<>();

  private boolean firstModelPostProcessCall = true;

  @Override
  public String getOptionName() {
    return "Xsubstitution";
  }

  @Override
  public String getUsage() {
    return "  -Xsubstitution          : enable substitution group replacement";
  }

  @Override
  public Collection<QName> getCustomizationElementNames() {
    return Arrays.asList(SUBSTITUTION_HEAD_NAME, SUBSTITUTION_HEAD_REF_NAME);
  }

  @Override
  public void postProcessModel(Model model, ErrorHandler errorHandler) {

    if (firstModelPostProcessCall) {
      firstModelPostProcessCall = false;
      logger.info("");
      logger.info("------------  Stage 1 - rewriting substitution type definitions ------------");
    }

    for (final CClassInfo classInfo : model.beans().values()) {

      if (!classInfo.hasProperties()) {
        continue;
      }

      adjustClassProperties(classInfo);

    }

  }

  private void adjustClassProperties(final CClassInfo classInfo) {

    if (logger.isDebugEnabled()) {
      logger.debug("");
      logger.debug(format("inspecting %s", classInfo.fullName()));
    }

    SubstitutionPropertyVisitor visitor = new SubstitutionPropertyVisitor(classInfo);

    for (CPropertyInfo property : classInfo.getProperties()) {
      property.accept(visitor);
    }

    SubstitutionCandidate candidate = new SubstitutionCandidate(classInfo, visitor.getProperties());

    if (visitor.hasSubstitutedProperties()) {
      candidate.modifyStage1();
      candidates.put(classInfo, candidate);
    }

  }

  @Override
  public boolean run(Outline outline, Options opt) {

    logger.info("");
    logger.info("------------  Stage 2 - rewriting annotations ------------");

    // now in part 2 of the transformation we need to traverse the generated field declarations
    // find our plugin customization and swap @XmlElement for @XmlElementRef

    for (SubstitutionCandidate candidate : candidates.values()) {
      candidate.modifyStage2(outline);
    }

    logger.info("");
    logger.info(format("Fixed %d class substitutions", candidates.size()));
    logger.info("");

    return true;

  }

}
