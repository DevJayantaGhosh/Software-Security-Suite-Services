package com.jayanta.projectmanagement.model;

import lombok.Data;

@Data
public class RepoScanResults {
    private SignatureVerificationResult signatureVerification;
    private SecretLeakDetectionResult secretLeakDetection;
    private VulnerabilityScanResult vulnerabilityScan;
}
