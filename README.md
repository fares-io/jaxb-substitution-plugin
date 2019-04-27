# JAXB Substitution Group Plugin

XJC does some pretty strange stuff based on the assumption that someone will do weird things within the bounds of the XML schema specification. Unfortunately some of those assumptions result in generation of unusable JAXB binding classes. This plugin attempts to correct one of these subtle annoyances.

## Usage

1. Configure the XJC compiler

The plugin can be used with any JAXB compiler that is capable of registering XJC plugins. The plugin jar needs to be made available to the XJC compiler classpath. In maven this is not the project classpath but the classpath of the plugin that generates code from one or more XML schema.

Example configuration for the JAXB2 commons compiler:

```xml
<plugin>
  <groupId>org.jvnet.jaxb2.maven2</groupId>
  <artifactId>maven-jaxb2-plugin</artifactId>
  <configuration>
    <plugins>
      <plugin>
        <groupId>io.fares.bind.xjc.plugins</groupId>
        <artifactId>jaxb-substitution-plugin</artifactId>
        <version>0.0.5</version>
      </plugin>
    </plugins>
    <extension>true</extension>
    <args>
      <arg>-Xsubstitution</arg>
    </args>
  </configuration>
</plugin>
```

2. Enable the plugin in JAXB configuration and annotate types

Ensure to activate the substitution plugin using the XJC bindings `jaxb:extensionBindingPrefixes` mechanism. Once the plugin is activated the extension annotation `substitution:head` can be attached to a `xsd:element` or `xsd:complexType` definition and the `substitution:head-ref` can be attached to any property type definition.

Example attaching a `substition:head` declaration to a type within the XML schema:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema targetNamespace="urn:test"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:substitution="http://jaxb2-commons.dev.java.net/basic/substitution"
  xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
  jaxb:version="2.1"
  jaxb:extensionBindingPrefixes="substitution">

  <xsd:element name="Extension" type="tns:Extension" abstract="true"/>

  <xsd:complexType name="Extension" abstract="true">
    <xsd:annotation>
      <xsd:appinfo>
        <substitution:head/>
      </xsd:appinfo>
    </xsd:annotation>
  </xsd:complexType>

</xsd:schema>

```

Example attaching a `substition:head` declaration to a type using a JAXB binding file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jaxb:bindings jaxb:version="2.1"
  jaxb:extensionBindingPrefixes="substitution"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
  xmlns:substitution="http://jaxb2-commons.dev.java.net/basic/substitution">

  <jaxb:bindings node="/xsd:schema" schemaLocation="myschema.xsd">
    <jaxb:bindings node="xsd:complexType[@name='Extension']">
      <substitution:head/>
    </jaxb:bindings>
  </jaxb:bindings>

</jaxb:bindings>
```

Check out the unit tests and their respective schemata for all possible usage examples.

NOTE: The rules XJC applies to decide when to use a `xsd:element` and when to use its `xsd:complextType` to build the internal model are anything but obvious. To ensure consistent activation of this plugin, it is recommended to attache the `sbustitution:head-ref` to the target (ref) element rather than using the `substitution:head` on the base element.

Check out these 2 tests to see this mind boggling logic of the XJC behaviour.

1. `schemas/HeadOnlyElementRef`

In this test we the `substitution:head` has to be attached to the `xsd:complexType` definition to access the extension in the XJC compilation context. The XJC compiler will choose to use the type as it can guarantee that no other element might be able to be used for the element reference. We'd argue this is logic is flawed because the element and its type are both annotated as abstract and the introduction of any additional schema elements in a later episode compilation might actually introduce possible substitution alternatives.

2. `schemas/SubstitutionHeadElementList`

In this test the `substitution:head` has to be attached to the `xsd:element` definition to access the extension in the XJC compilation context. The XJC compiler will choose to use the element declaration to build the internal model as there are other possible candidates in the compilation unit which could be substituted. So the compiler cannot be sure that the type is the only one used to satisfy the reference.

## Why such a plugin?

Depending on whether the substitution group head is referenced in the same compilation multiple times or not, XJC compiler will create a `JAXBElement<T>` typed field. The following [article](https://community.oracle.com/blogs/kohsuke/2006/03/03/why-does-jaxb-put-xmlrootelement-sometimes-not-always) on the java.net community  goes into the details of this rather strange behaviour. In a nutshell, the compiler will make decisions on how to structure binding fields based on what substitution elements are available at design time rather than solving this problem at runtime. This design neglects the fact that one can build bindings in episodes which might introduce additional substitution candidates into the overall binding context.

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
