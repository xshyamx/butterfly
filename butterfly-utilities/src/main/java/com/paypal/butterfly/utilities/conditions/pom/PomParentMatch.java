package com.paypal.butterfly.utilities.conditions.pom;

import com.paypal.butterfly.extensions.api.SingleCondition;
import com.paypal.butterfly.extensions.api.TUExecutionResult;
import com.paypal.butterfly.extensions.api.TransformationContext;
import com.paypal.butterfly.extensions.api.exception.TransformationUtilityException;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Check if the given pom file has a parent artifact, and it matches the specified
 * groupId, artifactId and version. Optionally, if no version is provided, then it
 * should only check for groupId and artifactId.
 */
public class PomParentMatch extends SingleCondition<PomParentMatch> {

  private static final String DESCRIPTION = "Check if the pom has a parent matching '%s:%s%s' exists in a POM file";

  private String groupId;
  private String artifactId;
  private String version = null;

  public PomParentMatch() {
  }

  public PomParentMatch(String groupId, String artifactId) {
    setGroupId(groupId);
    setArtifactId(artifactId);
  }

  public PomParentMatch(String groupId, String artifactId, String version) {
    this(groupId, artifactId);
    setVersion(version);
  }

  public PomParentMatch setGroupId(String groupId) {
    checkForBlankString("GroupId", groupId);
    this.groupId = groupId;
    return this;
  }

  public PomParentMatch setArtifactId(String artifactId) {
    checkForBlankString("ArtifactId", artifactId);
    this.artifactId = artifactId;
    return this;
  }

  public PomParentMatch setVersion(String version) {
    checkForEmptyString("Version", version);
    this.version = version;
    return this;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public String getDescription() {
    return String.format(DESCRIPTION, groupId, artifactId, (version == null ? "" : ":" + version));
  }

  @Override
  protected TUExecutionResult execution(File transformedAppFolder, TransformationContext transformationContext) {
    MavenXpp3Reader reader = new MavenXpp3Reader();
    FileInputStream fileInputStream = null;
    boolean exists = false;
    TransformationUtilityException ex = null;

    File file = getAbsoluteFile(transformedAppFolder, transformationContext);

    try {
      fileInputStream = new FileInputStream(file);
      Model model = reader.read(fileInputStream);
      Parent parent = model.getParent();
      if ( parent != null ) {
        if (parent.getGroupId().equals(groupId) && parent.getArtifactId().equals(artifactId) && (version == null || version.equals(parent.getVersion()))) {
          exists = true;
        }
      }
    } catch (XmlPullParserException | IOException e) {
      String pomFileRelative = getRelativePath(transformedAppFolder, file);
      String dependency = String.format("%s:%s%s", groupId, artifactId, (version == null ? "" : ":" + version));
      String details = String.format("Exception happened when checking if POM parent %s exists in %s", dependency, pomFileRelative);
      ex = new TransformationUtilityException(details, e);
    } finally {
      if(fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          if (ex == null) {
            String pomFileRelative = getRelativePath(transformedAppFolder, file);
            ex = new TransformationUtilityException("Exception happened when closing pom file " + pomFileRelative, e);
          } else {
            ex.addSuppressed(e);
          }
        }
      }
    }

    if (ex != null) {
      return TUExecutionResult.error(this, ex);
    }

    return TUExecutionResult.value(this, exists);
  }
}
