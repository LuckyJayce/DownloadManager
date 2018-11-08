package com.shizhefei.downloadmanager.base;

import java.util.List;
import java.util.Map;

public class DownloadParams {
    private String url;
    private String dir;
    private String fileName;
    private boolean overite;
    private Map<String, List<String>> params;
    private Map<String, List<String>> headers;
    private int blockSize;
}
