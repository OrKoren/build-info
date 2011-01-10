/*
 * Copyright (C) 2010 JFrog Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jfrog.build.client;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.jfrog.build.api.util.Log;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static org.jfrog.build.api.BuildInfoConfigProperties.*;
import static org.jfrog.build.api.BuildInfoFields.*;
import static org.jfrog.build.api.BuildInfoProperties.*;
import static org.jfrog.build.client.ClientConfigurationFields.*;
import static org.jfrog.build.client.ClientProperties.*;
import static org.jfrog.build.api.LicenseControlFields.*;

/**
 * @author freds
 */
public class ArtifactoryClientConfiguration {
    public static final Predicate<String> PUBLISH_MATRIX_PARAMS_PREDICATE = new Predicate<String>() {
        public boolean apply(String input) {
            return input.startsWith(PROP_DEPLOY_PARAM_PROP_PREFIX);
        }
    };

    private final PrefixPropertyHandler root;

    public final ResolverHandler resolver = new ResolverHandler();
    public final PublisherHandler publisher = new PublisherHandler();
    /**
     * @deprecated Should be at root level and removed from here
     */
    @Deprecated
    public final BuildInfoConfigHandler buildInfoConfig = new BuildInfoConfigHandler();
    public final BuildInfoHandler info = new BuildInfoHandler();
    public final ProxyHandler proxy = new ProxyHandler();

    public ArtifactoryClientConfiguration(Log log) {
        this.root = new PrefixPropertyHandler(log, new TreeMap<String, String>());
    }

    public void fillFromProperties(Map<String, String> props) {
        for (Map.Entry<String, String> entry : props.entrySet()) {
            root.setStringValue(entry.getKey(), entry.getValue());
        }
    }

    public void fillFromProperties(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            root.setStringValue((String) entry.getKey(), (String) entry.getValue());
        }
    }

    public Map<String, String> getAllProperties() {
        return root.props;
    }

    public String getContextUrl() {
        return root.getStringValue(PROP_CONTEXT_URL);
    }

    public void setContextUrl(String contextUrl) {
        root.setStringValue(PROP_CONTEXT_URL, contextUrl);
    }

    public void setTimeout(Integer timeout) {
        root.setIntegerValue(PROP_TIMEOUT, timeout);
    }

    public Integer getTimeout() {
        return root.getIntegerValue(PROP_TIMEOUT);
    }

    public class ResolverHandler extends RepositoryConfiguration {
        private final Predicate<String> matrixPredicate;

        public ResolverHandler() {
            super(PROP_RESOLVE_PREFIX);
            matrixPredicate = new Predicate<String>() {
                public boolean apply(String input) {
                    return input.startsWith(getMatrixParamPrefix());
                }
            };
        }

        public String getDownloadUrl() {
            // Legacy property from Gradle plugin apply from technique
            return root.getStringValue("artifactory.downloadUrl");
        }

        @Override
        public String getMatrixParamPrefix() {
            return getPrefix() + MATRIX;
        }

        @Override
        public Predicate<String> getMatrixParamFilter() {
            return matrixPredicate;
        }
    }

    public class PublisherHandler extends RepositoryConfiguration {
        public PublisherHandler() {
            super(PROP_PUBLISH_PREFIX);
        }

        public void setSnapshotRepoKey(String repoKey) {
            setStringValue(SNAPSHOT_REPO_KEY, repoKey);
        }

        public String getSnapshotRepoKey() {
            return getStringValue(SNAPSHOT_REPO_KEY);
        }

        public void setPublishArtifacts(Boolean enabled) {
            setBooleanValue(PUBLISH_ARTIFACTS, enabled);
        }

        public Boolean isPublishArtifacts() {
            return getBooleanValue(PUBLISH_ARTIFACTS);
        }

        public void setPublishBuildInfo(Boolean enabled) {
            setBooleanValue(PUBLISH_BUILD_INFO, enabled);
        }

        public Boolean isPublishBuildInfo() {
            return getBooleanValue(PUBLISH_BUILD_INFO);
        }

        public void setIncludePatterns(String patterns) {
            setStringValue(INCLUDE_PATTERNS, patterns);
        }

        public String getIncludePatterns() {
            return getStringValue(INCLUDE_PATTERNS);
        }

        public void setExcludePatterns(String patterns) {
            setStringValue(EXCLUDE_PATTERNS, patterns);
        }

        public String getExcludePatterns() {
            return getStringValue(EXCLUDE_PATTERNS);
        }

        @Override
        public String getMatrixParamPrefix() {
            return PROP_DEPLOY_PARAM_PROP_PREFIX;
        }

        @Override
        public Predicate<String> getMatrixParamFilter() {
            return PUBLISH_MATRIX_PARAMS_PREDICATE;
        }

    }

    public class ProxyHandler extends AuthenticationConfiguration {
        public ProxyHandler() {
            super(PROP_PROXY_PREFIX);
        }

        public void setHost(String host) {
            setStringValue(HOST, host);
        }

        public String getHost() {
            return getStringValue(HOST);
        }

        public void setPort(Integer port) {
            setIntegerValue(PORT, port);
        }

        public Integer getPort() {
            return getIntegerValue(PORT);
        }
    }

    public class AuthenticationConfiguration extends PrefixPropertyHandler {
        public AuthenticationConfiguration(String prefix) {
            super(root, prefix);
        }

        public void setEnabled(Boolean enabled) {
            setBooleanValue(ENABLED, enabled);
        }

        public Boolean isEnabled() {
            return getBooleanValue(ENABLED);
        }

        public void setUserName(String userName) {
            setStringValue(USERNAME, userName);
        }

        public String getUserName() {
            return getStringValue(USERNAME);
        }

        public void setPassword(String password) {
            setStringValue(PASSWORD, password);
        }

        public String getPassword() {
            return getStringValue(PASSWORD);
        }
    }

    public abstract class RepositoryConfiguration extends AuthenticationConfiguration {

        protected RepositoryConfiguration(String prefix) {
            super(prefix);
        }

        public void setName(String name) {
            setStringValue(NAME, name);
        }

        public String getName() {
            return getStringValue(NAME);
        }

        public void setUrl(String url) {
            setStringValue(URL, url);
        }

        public String getUrl() {
            return getStringValue(URL);
        }

        public void setRepoKey(String repoKey) {
            setStringValue(REPO_KEY, repoKey);
        }

        public String getRepoKey() {
            return getStringValue(REPO_KEY);
        }

        public void setMaven(boolean enabled) {
            setBooleanValue(MAVEN, enabled);
        }

        public Boolean isMaven() {
            return getBooleanValue(MAVEN);
        }

        public void setIvy(Boolean enabled) {
            setBooleanValue(IVY, enabled);
        }

        public Boolean isIvy() {
            return getBooleanValue(IVY);
        }

        public void setM2Compatible(Boolean enabled) {
            setBooleanValue(IVY_M2_COMPATIBLE, enabled);
        }

        public boolean isM2Compatible() {
            return getBooleanValue(IVY_M2_COMPATIBLE);
        }

        public void setIvyArtifactPattern(String artPattern) {
            setStringValue(IVY_ART_PATTERN, artPattern);
        }

        public String getIvyArtifactPattern() {
            String value = getStringValue(IVY_ART_PATTERN);
            if (StringUtils.isBlank(value)) {
                return LayoutPatterns.M2_PATTERN;
            }
            return value.trim();
        }

        public void setIvyPattern(String ivyPattern) {
            setStringValue(IVY_IVY_PATTERN, ivyPattern);
        }

        public String getIvyPattern() {
            String value = getStringValue(IVY_IVY_PATTERN);
            if (StringUtils.isBlank(value)) {
                return LayoutPatterns.DEFAULT_IVY_PATTERN;
            }
            return value.trim();
        }

        public abstract String getMatrixParamPrefix();

        public abstract Predicate<String> getMatrixParamFilter();

        public void addMatrixParam(String key, String value) {
            setStringValue(getMatrixParamPrefix() + key, value);
        }

        public void addMatrixParams(Map<String, String> vars) {
            props.putAll(Maps.filterKeys(vars, getMatrixParamFilter()));
        }

        public Map<String, String> getMatrixParams() {
            return Maps.filterKeys(props, getMatrixParamFilter());
        }
    }

    public class BuildInfoConfigHandler extends PrefixPropertyHandler {
        public BuildInfoConfigHandler() {
            super(root, BUILD_INFO_CONFIG_PREFIX);
        }

        public void setPropertiesFile(String propertyFile) {
            setStringValue(PROPERTIES_FILE, propertyFile);
        }

        public String getPropertiesFile() {
            return getStringValue(PROPERTIES_FILE);
        }

        public void setExportFile(String exportFile) {
            setStringValue(EXPORT_FILE, exportFile);
        }

        public String getExportFile() {
            return getStringValue(EXPORT_FILE);
        }

        public void setIncludeEnvVars(Boolean enabled) {
            setBooleanValue(INCLUDE_ENV_VARS, enabled);
        }

        public Boolean isIncludeEnvVars() {
            return getBooleanValue(INCLUDE_ENV_VARS);
        }
    }

    public class LicenseControlHandler extends PrefixPropertyHandler {
        public LicenseControlHandler() {
            super(root, BUILD_INFO_LICENSE_CONTROL_PREFIX);
        }

        public void setRunChecks(Boolean enabled) {
            setBooleanValue(RUN_CHECKS, enabled);
        }

        public Boolean isRunChecks() {
            return getBooleanValue(RUN_CHECKS);
        }

        public void setViolationRecipients(String recipients) {
            setStringValue(VIOLATION_RECIPIENTS, recipients);
        }

        public String getViolationRecipients() {
            return getStringValue(VIOLATION_RECIPIENTS);
        }

        public void setIncludePublishedArtifacts(Boolean enabled) {
            setBooleanValue(INCLUDE_PUBLISHED_ARTIFACTS, enabled);
        }

        public Boolean isIncludePublishedArtifacts() {
            return getBooleanValue(INCLUDE_PUBLISHED_ARTIFACTS);
        }

        public void setScopes(String scopes) {
            setStringValue(SCOPES, scopes);
        }

        public String getScopes() {
            return getStringValue(SCOPES);
        }

        public void setAutoDiscover(Boolean enabled) {
            setBooleanValue(AUTO_DISCOVER, enabled);
        }

        public Boolean isAutoDiscover() {
            return getBooleanValue(AUTO_DISCOVER);
        }

    }

    public class BuildInfoHandler extends PrefixPropertyHandler {
        public final LicenseControlHandler licenseControl = new LicenseControlHandler();

        private final Predicate<String> buildVariablesPredicate;

        public BuildInfoHandler() {
            super(root, BUILD_INFO_PREFIX);
            buildVariablesPredicate = new Predicate<String>() {
                public boolean apply(String input) {
                    return input.startsWith(BUILD_INFO_PREFIX + ENVIRONMENT_PREFIX);
                }
            };
        }

        public void setBuildName(String buildName) {
            setStringValue(BUILD_NAME, buildName);
        }

        public String getBuildName() {
            return getStringValue(BUILD_NAME);
        }

        public void setBuildNumber(String buildNumber) {
            setStringValue(BUILD_NUMBER, buildNumber);
        }

        public String getBuildNumber() {
            return getStringValue(BUILD_NUMBER);
        }

        public void setBuildTimestamp(String timestamp) {
            setStringValue(BUILD_TIMESTAMP, timestamp);
        }

        public String getBuildTimestamp() {
            return getStringValue(BUILD_TIMESTAMP);
        }

        public void setBuildStarted(String timestamp) {
            setStringValue(BUILD_STARTED, timestamp);
        }

        public String getBuildStarted() {
            return getStringValue(BUILD_STARTED);
        }

        public void setPrincipal(String principal) {
            setStringValue(PRINCIPAL, principal);
        }

        public String getPrincipal() {
            return getStringValue(PRINCIPAL);
        }

        public void setBuildUrl(String buildUrl) {
            setStringValue(BUILD_URL, buildUrl);
        }

        public String getBuildUrl() {
            return getStringValue(BUILD_URL);
        }

        public void setVcsRevision(String vcsRevision) {
            setStringValue(VCS_REVISION, vcsRevision);
        }

        public String getVcsRevision() {
            return getStringValue(VCS_REVISION);
        }

        public void setBuildAgentName(String buildAgentName) {
            setStringValue(BUILD_AGENT_NAME, buildAgentName);
        }

        public String getBuildAgentName() {
            return getStringValue(BUILD_AGENT_NAME);
        }

        public void setBuildAgentVersion(String buildAgentVersion) {
            setStringValue(BUILD_AGENT_VERSION, buildAgentVersion);
        }

        public String getBuildAgentVersion() {
            return getStringValue(BUILD_AGENT_VERSION);
        }

        public void setParentBuildName(String parentBuildName) {
            setStringValue(BUILD_PARENT_NAME, parentBuildName);
        }

        public String getParentBuildName() {
            return getStringValue(BUILD_PARENT_NAME);
        }

        public void setParentBuildNumber(String parentBuildNumber) {
            setStringValue(BUILD_PARENT_NUMBER, parentBuildNumber);
        }

        public String getParentBuildNumber() {
            return getStringValue(BUILD_PARENT_NUMBER);
        }

        public void setBuildRetentionDays(Integer daysToKeep) {
            setIntegerValue(BUILD_RETENTION_DAYS, daysToKeep);
        }

        public Integer getBuildRetentionDays() {
            return getIntegerValue(BUILD_RETENTION_DAYS);
        }

        public void setBuildRetentionMinimumDate(String date) {
            setStringValue(BUILD_RETENTION_MINIMUM_DATE, date);
        }

        public String getBuildRetentionMinimumDate() {
            return getStringValue(BUILD_RETENTION_MINIMUM_DATE);
        }

        public void addBuildVariables(Map<String, String> buildVariables) {
            for (Map.Entry<String, String> entry : buildVariables.entrySet()) {
                setStringValue(ENVIRONMENT_PREFIX + entry.getKey(), entry.getValue());
            }
        }

        public Map<String, String> getBuildVariables(Map<String, String> buildVariables) {
            return Maps.filterKeys(props, buildVariablesPredicate);
        }
    }
}