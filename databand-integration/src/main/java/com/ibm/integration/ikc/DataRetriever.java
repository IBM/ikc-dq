/*
 * Copyright IBM Corp. 2024
 *
 * The following sample of source code ("Sample") is owned by International
 * Business Machines Corporation or one of its subsidiaries ("IBM") and is
 * copyrighted and licensed, not sold. You may use, copy, modify, and
 * distribute the Sample in any form without payment to IBM, for the purpose of
 * assisting you in the development of your applications.
 *
 * The Sample code is provided to you on an "AS IS" basis, without warranty of
 * any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do
 * not allow for the exclusion or limitation of implied warranties, so the above
 * limitations or exclusions may not apply to you. IBM shall not be liable for
 * any damages you suffer as a result of using, copying, modifying or
 * distributing the Sample, even if IBM has been advised of the possibility of
 * such damages.
 */
package com.ibm.integration.ikc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.ibm.integration.model.ikc.IssueCollection;
import com.ibm.integration.model.ikc.SLACollection;
import com.ibm.integration.model.ikc.ScoreCollection;
import com.ibm.integration.util.JsonUtils;

public class DataRetriever {
    private final String ENDPOINT_ISSUES = ("/data_quality/v4/issues");
    private final String ENDPOINT_SCORES = ("/data_quality/v4/scores");
    private final String ENDPOINT_SLAS = ("/data_quality/v4/slas");
    private final Connection connection;

    public DataRetriever(Connection connection) {
        this.connection = connection;
    }

    public IssueCollection retrieveIssues(String projectId, String dqAssetId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(
                        Config.getInstance().getIkcHost() + ENDPOINT_ISSUES + "?project_id=" + projectId + "&reported_for.id=" + dqAssetId))
                .header("Authorization", "Bearer " + connection.getBearerToken()).method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = sendRequest(request);
        return JsonUtils.fromJson(response.body(), IssueCollection.class);
    }

    public ScoreCollection retrieveScores(String projectId, String dqAssetId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(
                        Config.getInstance().getIkcHost() + ENDPOINT_SCORES + "?project_id=" + projectId + "&asset_id=" + dqAssetId))
                .header("Authorization", "Bearer " + connection.getBearerToken()).method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = sendRequest(request);
        return JsonUtils.fromJson(response.body(), ScoreCollection.class);
    }

    public SLACollection retrieveSlas() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(Config.getInstance().getIkcHost() + ENDPOINT_SLAS))
                .header("Authorization", "Bearer " + connection.getBearerToken()).method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = sendRequest(request);
        return JsonUtils.fromJson(response.body(), SLACollection.class);
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = connection.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Connection.validateResponse(response);
        return response;
    }
}
