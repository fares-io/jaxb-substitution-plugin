# JAXB Substitution Group Plugin

XJC does some pretty strange stuff based on the assumption that someone will do weird things within the bounds of the XML schema specification. Unfortunately some of those assumptions result in generation of unusable JAXB binding classes. This plugin attempts to correct one of these subtle annoyances.

## Usage

Configure the XJC compiler

The plugin can be used with any JAXB compiler that is capable of registering XJC plugins. The plugin jar needs to be made available to the XJC compiler classpath. In maven this is not the project classpath but the classpath of the plugin that generates code from one or more XML schema.

Example configuration for the JAXB compiler:

```xml
<plugin>
  <groupId>org.jvnet.jaxb</groupId>
  <artifactId>jaxb-maven-plugin</artifactId>
  <configuration>
    <extension>true</extension>
    <plugins>
      <plugin>
        <groupId>io.fares.bind.xjc.plugins</groupId>
        <artifactId>jaxb-substitution-plugin</artifactId>
        <version>1.0.0</version>
      </plugin>
    </plugins>
    <args>
      <arg>-Xsubstitution</arg>
    </args>
  </configuration>
</plugin>
```

Check out the unit tests and their respective schemata for all possible usage examples.

## Why such a plugin?

Depending on whether the substitution group head is referenced in the same compilation multiple times or not, XJC compiler will create a `JAXBElement<T>` typed field. The following [article](https://community.oracle.com/blogs/kohsuke/2006/03/03/why-does-jaxb-put-xmlrootelement-sometimes-not-always) on the java.net community goes into the details of this rather strange behaviour. In a nutshell, the compiler will make decisions on how to structure binding fields based on what substitution elements are available at design time rather than solving this problem at runtime. This design neglects the fact that one can build bindings in episodes which might introduce additional substitution candidates into the overall binding context.

This plugin will fix this XJC assumption and postpones the decision on what to do with additional substitution candidates to the binding runtime context.

### Problem 1 - Using Element Ref with a Substitution Group Head

Lets say we have a schema where the `Extension` element is defined as a substitution head in at least 1 other XML element, XJC will create a ugly and unusable class with `JaxbElement` wrapped fields.

Schema:

```xml
<xsd:complexType name="Context">
  <xsd:sequence>
    <xsd:element ref="tns:Extension" minOccurs="0" maxOccurs="10"/>
  </xsd:sequence>
</xsd:complexType>

<xsd:element name="Extension" type="tns:Extension" abstract="true"/>

<xsd:complexType name="Extension" abstract="true">
  <xsd:attribute name="type" type="xsd:string" use="required"/>
</xsd:complexType>

<xsd:element name="SomeExtension" type="tns:SomeExtension" substitutionGroup="tns:Extension"/>

<xsd:complexType name="SomeExtension">
  <xsd:complexContent>
    <xsd:extension base="tns:Extension"/>
  </xsd:complexContent>
</xsd:complexType>
```

Generated Class:

```java
public class Context {

    @XmlElementRef(name = "Extension", namespace = "urn:test", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends Extension>> extension;

}
```

### Problem 2 - Using Inline Element based on a Substitution Group Head type

This variant will generate a properly formatted Java bean but when the JAXB marshaller tries to serialise the bean it will output `xsi:type` endcoded XML that is hard to work with in xml processing engines such as xquery.

Schema:

```xml
<xsd:complexType name="Context">
  <xsd:sequence>
    <xsd:element name="Extension" type="tns:Extension" minOccurs="0" maxOccurs="10"/>
  </xsd:sequence>
</xsd:complexType>
```

Generated Class:

```java
public class Context {

  @XmlElement(name = "Extension")
  protected List<Extension> extension;

}
```

JAXB Output:

```xml
<Context xmlns="urn:test" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <Extension xsi:type="SomeExtension"/>
  <Extension xsi:type="OneExtension"/>
</Context>
```

### Desired Behaviour

The ideal modelling state would be to just reference a `xsd:element` which is a substitution head.

1. ensure the `JAXBElement` wrapper is not generated
2. ensure any `@XmlElement` annotations at field or accessor level are replaced with the `@XmlElementRef` directive

Schema:

```xml
<xsd:complexType name="Context">
  <xsd:sequence>
    <xsd:element ref="tns:Extension" minOccurs="0" maxOccurs="10"/>
  </xsd:sequence>
</xsd:complexType>
```

Generated Class:

```java
public class Context {

  @XmlElementRef(name = "Extension", required = false)
  protected List<Extension> extension;

}
```

JAXB Output:

```xml
<Context xmlns="urn:test">
  <SomeExtension/>
  <OneExtension/>
</Context>
```
