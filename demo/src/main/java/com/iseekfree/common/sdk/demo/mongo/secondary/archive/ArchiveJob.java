package com.iseekfree.common.sdk.demo.mongo.secondary.archive;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

@Entity("archive_jobs")
public class ArchiveJob {

    @Id
    private ObjectId id;

    @Indexed(options = @IndexOptions(name = "uk_archive_job_key", unique = true))
    private String jobKey;

    private String status;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
