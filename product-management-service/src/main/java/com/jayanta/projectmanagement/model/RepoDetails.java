package com.jayanta.projectmanagement.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class RepoDetails {
    @Field("repoUrl")
    private String repoUrl;

    @Field("branch")
    private String branch;

    @Field("scans")
    private RepoScanResults scans;
}
