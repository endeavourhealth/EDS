package org.endeavourhealth.ui.json;

public class JsonRemoteFiler {
    private String name;
    private String description;
    private int subscriberId;
    private JsonTechnicalDetails technicalDetails;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    public JsonTechnicalDetails getTechnicalDetails() {
        return technicalDetails;
    }

    public void setTechnicalDetails(JsonTechnicalDetails technicalDetails) {
        this.technicalDetails = technicalDetails;
    }


    public static class JsonTechnicalDetails {
        private JsonFileDetails subscriberFileLocationDetails;
        private JsonSftpDetails sftpConnectionDetails;

        public JsonFileDetails getSubscriberFileLocationDetails() {
            return subscriberFileLocationDetails;
        }

        public void setSubscriberFileLocationDetails(JsonFileDetails subscriberFileLocationDetails) {
            this.subscriberFileLocationDetails = subscriberFileLocationDetails;
        }

        public JsonSftpDetails getSftpConnectionDetails() {
            return sftpConnectionDetails;
        }

        public void setSftpConnectionDetails(JsonSftpDetails sftpConnectionDetails) {
            this.sftpConnectionDetails = sftpConnectionDetails;
        }
    }

    public static class JsonFileDetails {
        private String dataDir;
        private String stagingDir;
        private String destinationDir;
        private String archiveDir;
        private String pgpCertFile;
        private String resultsSourceDir;
        private String resultsStagingDir;
        private String privateKeyFile;

        public String getDataDir() {
            return dataDir;
        }

        public void setDataDir(String dataDir) {
            this.dataDir = dataDir;
        }

        public String getStagingDir() {
            return stagingDir;
        }

        public void setStagingDir(String stagingDir) {
            this.stagingDir = stagingDir;
        }

        public String getDestinationDir() {
            return destinationDir;
        }

        public void setDestinationDir(String destinationDir) {
            this.destinationDir = destinationDir;
        }

        public String getArchiveDir() {
            return archiveDir;
        }

        public void setArchiveDir(String archiveDir) {
            this.archiveDir = archiveDir;
        }

        public String getPgpCertFile() {
            return pgpCertFile;
        }

        public void setPgpCertFile(String pgpCertFile) {
            this.pgpCertFile = pgpCertFile;
        }

        public String getResultsSourceDir() {
            return resultsSourceDir;
        }

        public void setResultsSourceDir(String resultsSourceDir) {
            this.resultsSourceDir = resultsSourceDir;
        }

        public String getResultsStagingDir() {
            return resultsStagingDir;
        }

        public void setResultsStagingDir(String resultsStagingDir) {
            this.resultsStagingDir = resultsStagingDir;
        }

        public String getPrivateKeyFile() {
            return privateKeyFile;
        }

        public void setPrivateKeyFile(String privateKeyFile) {
            this.privateKeyFile = privateKeyFile;
        }
    }

    public static class JsonSftpDetails {
        private String hostname;
        private String hostPublicKey;
        private String port;
        private String username;
        private String clientPrivateKeyPassword;
        private String clientPrivateKey;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getHostPublicKey() {
            return hostPublicKey;
        }

        public void setHostPublicKey(String hostPublicKey) {
            this.hostPublicKey = hostPublicKey;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getClientPrivateKeyPassword() {
            return clientPrivateKeyPassword;
        }

        public void setClientPrivateKeyPassword(String clientPrivateKeyPassword) {
            this.clientPrivateKeyPassword = clientPrivateKeyPassword;
        }

        public String getClientPrivateKey() {
            return clientPrivateKey;
        }

        public void setClientPrivateKey(String clientPrivateKey) {
            this.clientPrivateKey = clientPrivateKey;
        }
    }

}
