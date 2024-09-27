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
 */package com.ibm.integration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.ibm.integration.ikc.Config;
import com.ibm.integration.ikc.Connection;
import com.ibm.integration.ikc.DataRetriever;
import com.ibm.integration.model.databand.Component;
import com.ibm.integration.model.databand.EventType;
import com.ibm.integration.model.databand.Run;
import com.ibm.integration.model.databand.Task;
import com.ibm.integration.model.ikc.Issue;
import com.ibm.integration.model.ikc.IssueCollection;
import com.ibm.integration.model.ikc.IssueCollectionItem;
import com.ibm.integration.model.ikc.SLA;
import com.ibm.integration.model.ikc.SLACollection;
import com.ibm.integration.model.ikc.ScoreCollection;
import com.ibm.integration.model.ikc.ScoreOfDimension;

/**
 * Sample integration between IKC data quality components and Databand. See the Readme file or the Config class for the required environment
 * variables.
 */
public class IntegrationDemo {
    public static final String SLA_NAME_PREFIX = "DBInt";
    public static final String DB_PROJECT_NAME = "IKC DQ Demo";
    public static final String DB_PIPELINE_NAME = "IKC data asset: new_employees.csv";
    public static final String DB_RUN_NAME = "Asset Data Quality";
    public static final String OVERALL_SLA_NAME = "Overall";
    public static final String[] DIMENSION_NAMES = { "Completeness", "Consistency", "Validity" };
    public static final String DB_OVERALL_TASK_NAME = "Overall Data Quality SLA";
    public static final String DB_WORKFLOW_TASK_NAME = "Remediation workflow";
    final private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static void main(String[] args) throws Exception {
        String projectId = Config.getInstance().getProjectId();
        String assetId = Config.getInstance().getDqAssetId();
        Connection connection = new Connection();
        DataRetriever retriever = new DataRetriever(connection);

        System.out.println("Retrieving DQ checks...");
        IssueCollection issues = retriever.retrieveIssues(projectId, assetId);
        System.out.println("Found " + issues.getIssues().size() + " checks.");

        System.out.println("Retrieving DQ scores...");
        ScoreCollection scores = retriever.retrieveScores(projectId, assetId);
        System.out.println("Found " + scores.getScores().size() + " scores.");

        System.out.println("Retrieving DQ SLAs...");
        SLACollection slaCollection = retriever.retrieveSlas();
        List<SLA> slas = slaCollection.getSlas().stream().filter(s -> s.getName() != null && s.getName().startsWith(SLA_NAME_PREFIX))
                .toList();
        System.out.println("Found " + slas.size() + " SLAs.");

        System.out.println("Generating run data...");

        // Simplification: Generate timestamps
        String startTime = dateFormat.format(new Date());
        int randomDuration = ThreadLocalRandom.current().nextInt(5, 10 + 1);
        String endTime = dateFormat.format(new Date(System.currentTimeMillis() + randomDuration * 1000L));
        String wfTime = dateFormat.format(new Date(System.currentTimeMillis() + 3600 * 1000));

        // Create the pipeline
        Run run = new Run(DB_PIPELINE_NAME, DB_PROJECT_NAME, DB_RUN_NAME, startTime, endTime, EventType.COMPLETE, "");

        // Create overall SLA task
        SLA mainSla = slas.stream().filter(s -> s.getName().contains(OVERALL_SLA_NAME)).findFirst().orElse(null);
        assert mainSla != null;
        double overallThreshold = mainSla.getConditions().get(0).getThreshold();
        double overallScore = scores.getScores().get(0).getScore();
        // Simplification: Skip retrieving the remediation workflow details, assume it gets started when the overall SLA is violated

        Task workflowTask = null;
        if (overallScore < overallThreshold) {
            workflowTask = new Task(DB_WORKFLOW_TASK_NAME, wfTime, wfTime, EventType.COMPLETE, "Workflow created", run);
            run.addTask(workflowTask);
        }
        Task overallSLATask = createTask(DB_OVERALL_TASK_NAME, startTime, endTime, overallScore, overallThreshold, run, run, workflowTask);

        // Create data quality dimension SLA and check tasks
        List<SLA> subSlas = slas.stream().filter(s -> !s.getName().contains(OVERALL_SLA_NAME)).toList();
        for (String dimensionName : DIMENSION_NAMES) {
            SLA dimensionSLA = subSlas.stream().filter(sla -> sla.getConditions().get(0).getDimension().getName().contains(dimensionName))
                    .findAny().orElse(null);
            assert dimensionSLA != null;
            double dimensionThreshold = dimensionSLA.getConditions().get(0).getThreshold();
            double dimensionScore = getScoreValue(scores, dimensionName);
            System.out.println("===========================");
            System.out.println("Dimension: " + dimensionName);
            System.out.println("Asset score: " + dimensionScore);
            System.out.println("SLA threshold: " + dimensionThreshold);
            Task dimensionSLATask = createTask(dimensionName + " SLA", startTime, endTime, dimensionScore, dimensionThreshold, run, run,
                    overallSLATask);

            // Create data quality checks
            // Simplification: set a meaningful threshold for check scores
            double checkThreshold = (1 - ((1 - dimensionThreshold) / 2));
            System.out.println("Check threshold: " + checkThreshold);

            List<IssueCollectionItem> dimensionIssues = issues.getIssues().stream()
                    .filter(i -> i.getChildren().get(0).getCheck().getDimension().getName().contains(dimensionName)).toList();
            for (IssueCollectionItem issue : dimensionIssues) {
                final Issue firstSubIssue = issue.getChildren().get(0);
                String checkName = firstSubIssue.getCheck().getName();
                double checkScore = 1.0 - issue.getPercentOccurrences();
                System.out.println("---------------------------");
                System.out.println("Check: " + checkName + ": " + checkScore);
                if (firstSubIssue.isIgnored()) {
                    System.out.println("Ignoring deactivated check");
                    continue;
                }
                createTask(checkName, startTime, endTime, checkScore, checkThreshold, run, run, dimensionSLATask);
            }
        }
        System.out.println("===========================");
        DatabandSender.sendRun(run);
    }

    private static Task createTask(String name, String startTime, String endTime, double score, double threshold, Run run, Component parent,
            Task receiver) {
        boolean violated = score < threshold;
        if (violated)
            System.out.println("Score is below threshold");
        EventType eventType = violated ? EventType.FAIL : EventType.COMPLETE;
        String slaLogBody = violated ? "Score: " + score + " < " + threshold : "Score: " + score + ". All good!";
        Task newTask = new Task(name, startTime, endTime, eventType, slaLogBody, parent);
        run.addTask(newTask);
        if (receiver != null)
            receiver.addInput(newTask);
        return newTask;
    }

    private static double getScoreValue(ScoreCollection scores, String dimensionName) {
        ScoreOfDimension score = scores.getScores().get(0).getDimensionScores().stream()
                .filter(ds -> ds.getDimension().getName().contains(dimensionName)).findAny().orElse(null);
        assert score != null;
        return score.getScore();
    }
}