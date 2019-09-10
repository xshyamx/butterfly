package com.paypal.butterfly.utilities.conditions.pom;

import com.paypal.butterfly.extensions.api.TUExecutionResult;
import com.paypal.butterfly.extensions.api.exception.TransformationUtilityException;
import com.paypal.butterfly.utilities.TransformationUtilityTestHelper;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PomParentMatchTest extends TransformationUtilityTestHelper {
  private PomParentMatch parentMatch;
  private TUExecutionResult executionResult;


  @Test
  public void parentExistsFull() {
    parentMatch = new PomParentMatch("com.test", "foo-parent", "1.0").relative("pom.xml");
    executionResult = parentMatch.execution(transformedAppFolder, transformationContext);
    Assert.assertEquals(executionResult.getType(), TUExecutionResult.Type.VALUE);
    Assert.assertTrue((Boolean) executionResult.getValue());
    Assert.assertEquals(parentMatch.getDescription(), "Check if the pom has a parent matching 'com.test:foo-parent:1.0' exists in a POM file");

  }
  @Test
  public void parentExistsNoVersion() {
    parentMatch = new PomParentMatch("com.test", "foo-parent").relative("pom.xml");
    executionResult = parentMatch.execution(transformedAppFolder, transformationContext);
    Assert.assertEquals(executionResult.getType(), TUExecutionResult.Type.VALUE);
    Assert.assertTrue((Boolean) executionResult.getValue());
    Assert.assertEquals(parentMatch.getDescription(), "Check if the pom has a parent matching 'com.test:foo-parent' exists in a POM file");
  }

  @Test
  public void doesNotExistFull() {

    parentMatch = new PomParentMatch()
        .setGroupId("xmlunit")
        .setArtifactId("xmlunit")
        .setVersion("1.6")
        .relative("pom.xml");

    executionResult = parentMatch.execution(transformedAppFolder, transformationContext);
    Assert.assertEquals(executionResult.getType(), TUExecutionResult.Type.VALUE);
    Assert.assertFalse((Boolean) executionResult.getValue());
  }

  @Test void doesNotExistWithoutVersion() {
    parentMatch = new PomParentMatch("org.slf4j", "slf4j-api").relative("pom.xml");
    executionResult = parentMatch.execution(transformedAppFolder, transformationContext);
    Assert.assertEquals(executionResult.getType(), TUExecutionResult.Type.VALUE);
    Assert.assertFalse((Boolean) executionResult.getValue());
  }

  @Test
  public void invalidXmlFileTest() {
    parentMatch = new PomParentMatch("xmlunit", "xmlunit").relative("/src/main/resources/dogs.yaml");
    Assert.assertEquals(parentMatch.getDescription(), "Check if the pom has a parent matching 'xmlunit:xmlunit' exists in a POM file");
    Assert.assertEquals(parentMatch.getGroupId(), "xmlunit");
    Assert.assertEquals(parentMatch.getArtifactId(), "xmlunit");
    Assert.assertNull(parentMatch.getVersion());

    executionResult = parentMatch.execution(transformedAppFolder, transformationContext);
    Assert.assertEquals(executionResult.getType(), TUExecutionResult.Type.ERROR);
    Assert.assertNull(executionResult.getValue());
    Assert.assertNotNull(executionResult.getException());
    Assert.assertEquals(executionResult.getException().getClass(), TransformationUtilityException.class);
    Assert.assertEquals(executionResult.getException().getMessage(), "Exception happened when checking if POM parent xmlunit:xmlunit exists in /src/main/resources/dogs.yaml");
    Assert.assertNotNull(executionResult.getException().getCause());
    Assert.assertEquals(executionResult.getException().getCause().getClass(), XmlPullParserException.class);
    Assert.assertEquals(executionResult.getException().getCause().getMessage(), "only whitespace content allowed before start tag and not T (position: START_DOCUMENT seen T... @1:1) ");
  }
}
