# IBM Knowledge Catalog data quality Databand integration demo

The code sample demonstrates how IKC data quality components can be integrated with Databand to enable
data quality observability. The code reads an data asset's quality information from IKC and creates a
Databand pipeline view of the asset's data quality check results and SLA compliance.

An IKC asset with quality scores is required. A separate, matching data quality SLA per data quality dimension
is required, with all SLA names prefixed with "DBInt", plus a separate one for the "Overall" dimension,
identified by "Overall" in its name.

Last verified with IBM Knowledge Catalog 5.0.3 and IBM Databand 1.0.104.3.

To run the demo, create a `config.properties` file in the root folder with this content:

```
# IKC host URL, e.g. https://cpd-wkc.apps.ikchost.com
IKC_HOST=

# IKC Basic auth token as Base64 string, e.g. YWRtaW46cGFzc3dvcmQ=
IKC_BASIC_AUTH_TOKEN=

# IKC project ID
PROJECT_ID=

# Test asset's data quality ID (not the CAMS ID). Can be obtained via
# /data_quality/v4/search_dq_asset?project_id=<ID1>&wkc_asset_id=<ID2>
# (Swagger: https://api.dataplatform.dev.cloud.ibm.com/data_quality/v3/api/explorer/#/Data%20Quality%20Assets/search_dq_asset)
# by providing the CAMS project ID and asset ID
DQ_ASSET_ID=

# The Databand instance bulk event URL, e.g. https://databand-host.databand.ai/api/v1/tracking/open-lineage/881c3448-2f14-11ef-b811-2e4d00527841/events/bulk
DATABAND_URL=

# The Databand bearer token
DATABAND_TOKEN=
```
and provide the values.

Then run

`
mvn clean compile assembly:single
`

`
java -jar target/ikc-dq-databand-1.0-SNAPSHOT-jar-with-dependencies.jar
`
