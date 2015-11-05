package no.api.sentinel.enumuration;

/**
 *
 */
public enum Service {
    FRONTIER("http://bearing.dev.abctech-thailand.com/api/frontier", "http://bearing.dev.abctech-thailand.com/frontier/apiadmin/ping"),
    PRIMUS("http://bearing.dev.abctech-thailand.com/api/primusr", "http://bearing.dev.abctech-thailand.com/primus/apiadmin/ping"),
    ELASTICSEARCH("http://bearing:9200", "http://bearing:9200"),
    PIPEK("http://bearing.dev.abctech-thailand.com/api/pipek", "http://bearing.dev.abctech-thailand.com/pipek/apiadmin/ping");

    Service(String serviceAddress, String pingAddress) {
        this.serviceAddress = serviceAddress;
        this.pingAddress = pingAddress;
    }

    private String serviceAddress;
    private String pingAddress;

    public String serviceAddress() {
        return serviceAddress;
    }

    public String pingAddress() {
        return pingAddress;
    }
}
