package com.iseekfree.common.sdk.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("claw.mongo")
public class ClawMongoProperties {

    private boolean enabled = true;
    private String uri = "mongodb://127.0.0.1:27017";
    private String database = "claw";
    private boolean autoIndex = true;
    private final List<String> mapPackages = new ArrayList<>();
    private final Map<String, Datastore> datastores = new LinkedHashMap<>();
    private final Map<String, Cluster> clusters = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isAutoIndex() {
        return autoIndex;
    }

    public void setAutoIndex(boolean autoIndex) {
        this.autoIndex = autoIndex;
    }

    public List<String> getMapPackages() {
        return mapPackages;
    }

    public Map<String, Datastore> getDatastores() {
        return datastores;
    }

    public Map<String, Cluster> getClusters() {
        return clusters;
    }

    public static class Cluster {
        private String uri;
        private Boolean autoIndex;
        private final Map<String, Datastore> datastores = new LinkedHashMap<>();

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Boolean getAutoIndex() {
            return autoIndex;
        }

        public void setAutoIndex(Boolean autoIndex) {
            this.autoIndex = autoIndex;
        }

        public Map<String, Datastore> getDatastores() {
            return datastores;
        }
    }

    public static class Datastore {
        private String cluster;
        private String database;
        private Boolean autoIndex;
        private final List<String> mapPackages = new ArrayList<>();

        public String getCluster() {
            return cluster;
        }

        public void setCluster(String cluster) {
            this.cluster = cluster;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public Boolean getAutoIndex() {
            return autoIndex;
        }

        public void setAutoIndex(Boolean autoIndex) {
            this.autoIndex = autoIndex;
        }

        public List<String> getMapPackages() {
            return mapPackages;
        }
    }
}
