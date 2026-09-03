# data_services dataservices documentation src

Data Services
Table of Contents
Introduction . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Breaking Change Management . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Versioning . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
How Do We Treat Breaking Changes?. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
What is Public and Internal API? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
What is Breaking Change? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
What is Non-Breaking Change? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
How We Mark Deprecation? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
"Only For Usage" and Customizable Interfaces. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Environment Setup . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
PostgreSQL Setup . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Spring Cache . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
Java BIO / NIO / NIO2 / APR . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
Manual Database Setup . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
Spring Actuator Support. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
Initialization Phase . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 21
Initialization Sequence . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Custom Logic . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 25
Execution of Custom JSON-RPC Requests. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 25
Cluster-safe Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Configuration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
Configuration Options. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
Custom Caches . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Query Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Document Related Configuration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
CDD Related Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
RPC Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Model & Document Initialization Import. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Other Initialization Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Jobs Configurations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
Attachment Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
Java Client Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
Authorization Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
Actuator Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
Logger Anonymizer . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Other Properties. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
1

-- 1 of 334 --

Configuration Profiles. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
JSON-RPC Endpoint . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
JSON-RPC Endpoint and Operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
Idempotency . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
Request-Id Cleanup Job. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
Header of the Request. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
Body of the Request . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
Response of the JSON-RPC Request . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
Common JSON Types. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
Operation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 61
JSON-RPC 2.0 Core Operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
Exception Handling in Data Services. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 92
Overview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 92
Exception Handling Paths . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 93
Usage Guidelines . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 93
Privacy Rules for Anonymized Messages. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 94
Key Classes. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 94
Query API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 94
Glossary . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 94
High-level Overview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 95
Indexing . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 148
Query API Authorization . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 150
Extensions Points. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 155
Performance . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 157
Data Migration Support . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 168
Predefined Migrations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 168
Custom Migration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 169
Migration Step . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 169
Migration Task . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 170
Migration Execution . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 171
Error Handling . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 171
Example Usage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 171
Transaction Management and Concurrency . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 172
JSON-RPC Batch Transaction Handling. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 172
Transaction Isolation Level . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 173
Index Consistency Guarantees . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 173
Concurrent Document Updates . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 173
Error Handling and Rollback . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 174
Read Replica Routing . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 175
Best Practices . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 177
Documents . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 177
2

-- 2 of 334 --

Unique Constraints . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 178
Attachments. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 184
Attachment Definition and Usage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 184
Attachment Upload . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 187
Attachment Download . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 188
Attachment Deletion . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 189
Attachment Assignment . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 189
Attachment Extension Points . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 189
Attachment Mime Type Probing Improvement . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 190
Relationships . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 191
Abbreviations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 191
Overview and Definitions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 191
Document Model CRUD . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 192
Document CRUD . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 192
Model Graph . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 192
Relationship Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 195
Relationship Migration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 198
Compose Documents . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 199
Setup Server for CDD . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 199
CDM Handling . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 199
CDD Handling . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 200
Java API. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 201
Calling Data Services Functionality. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 202
Extending the Server . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 212
Data Services Security. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 233
Data Services Authentication . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 233
Data Services Authorization . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 233
Support Only HTTP/1 Protocol . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 237
Log Injection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 237
Data Services Artifacts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 238
Runnable Artifacts. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 238
Data Services BOM. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 239
ModelGraphGenerator . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 240
Data Services Helm Charts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 240
HTTP API Documentation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 240
Attachments . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 240
Documents. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 241
Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 241
Monitoring. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 241
Query API. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 241
Relationships. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 241
3

-- 3 of 334 --

Workspace (SME API): For testing and demo purposes only. Not intended for production use. . 241
Common Request Headers. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 242
Data Services Clients . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 251
Java Client . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 251
Data Services Command Line Interface (CLI) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 253
Data Services - Content Store. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 254
Introduction . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 254
Content Store Sub Modules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 256
Content Store Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 256
How to Start the Content Store Module . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 260
Content Store HTTP API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 262
Content Store Client Module . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 265
Sequence Diagram. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 265
Content Store Events . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 267
Content Store Artifacts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 268
Content Store Probing Content Mime Type . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 268
Examples . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 270
Extending Server . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 270
Content Store Server Example . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 271
Troubleshooting Common Problems. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 271
Javadoc is not properly rendered in IDE . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 271
Error org.quartz.SchedulerException: Job instantiation failed . . . . . . . . . . . . . . . . . . . . . . . . . . . 271
Dynamic Gradle Versions. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 272
Hazelcast Warnings In Logs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 272
Spring Boot Issues When Upgrading From 3.3.x to 3.4.x . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 273
Configuration Change Does Not Apply In Spring Boot Integration Tests For Data Services . . . . . 273
Server Fails to Start with Actuator Misconfiguration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 274
OOMKilled Errors for Kubernetes Deployments . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 275
Using com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService with
Asynchronous Tasks . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 275
Missing Locale en-US.UTF8 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 276
Embedded PostgreSQL Fails to Initialize on Windows (Missing Visual C++ Redistributable). . . . 276
Shared Memory Limit Reached on macOS (shmget failed) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 278
Liquibase Checksum Invalid Errors in Version 4.33.0 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 279
References . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 279
JavaDoc. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 279
TypeDoc . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 280
Infrastructure Dependencies. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 280
Migration Instructions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 281
2026.06 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 281
4

-- 4 of 334 --

Introduction
Data Services specialize on performant, extendable, configurable and scalable data access
(documents + links = data). Data Services provide access to all A12 models, including relationship
ones.
Data can be accessed in multiple ways:
1. directly via HTTP requests
2. via Typescript client: com.mgmtp.a12.dataservices:dataservices-access
3. via Java client: com.mgmtp.a12.dataservices:dataservices-client
4. via command line client: com.mgmtp.a12.dataservices:dataservices-client-cli
 Data Services use Kernel library for data validation, refer to their documentation
for more information.
Breaking Change Management
Versioning
Semantic versioning is used.
How Do We Treat Breaking Changes?
We ensure a thorough evaluation of every modification, scrutinizing its potential impact on system
integrity. Our initial strategy involves diligently striving to prevent any disruptive alterations by
identifying alternative approaches that can be implemented seamlessly without causing
disruptions.
In cases where avoiding a breaking change proves unfeasible or where compelling reasons
necessitate a departure from non-breaking practices, we meticulously plan such changes for
incorporation into a major release, in accordance with our A12 Versioning Schema.
It’s crucial to note that our minor releases and patch releases adhere to a policy of backward
compatibility, meaning they are designed to introduce enhancements without introducing breaking
changes. This commitment provides our users with a stable and reliable framework for their
ongoing projects.
Employing the A12 deprecation guidelines is a deliberate approach aimed at affording our
customers greater flexibility when planning their migration strategies. By following these
guidelines, we empower our users to manage transitions smoothly and efficiently.
To ensure transparency and ease of reference, all breaking changes and deprecations are
meticulously documented in our migration notes. This serves as a comprehensive resource for
users navigating these modifications, fostering a clear understanding of the adjustments made
during each release.
5

-- 5 of 334 --

What is Public and Internal API?
The Internal API consistently resides within the internal package, with the sole exception being
autoconfiguration (found in the autoconfigure module) classes. While these autoconfiguration
classes are deemed internal, they are not housed in the internal package.
Anything outside the internal scope is regarded as public and is assured not to undergo changes
throughout a single major release version.
What is Breaking Change?
A change is deemed breaking if:
• Project compilation encounters failure.
• There is a change in application behavior.
• Code adaptation becomes necessary.
• Manual model adaptation is required.
Only happen in Major releases. Migration instructions will always be provided to facilitate easier
integration.
What is Non-Breaking Change?
A change is deemed non-breaking if:
• Migration is provided without necessitating any manual steps from customer projects.
• Modifications occur in the internal API. Please note that migration steps are public, they are
considered valid only for the corresponding release line. Therefore, any changes in the next
release line are not considered breaking.
• Deprecation events take place.
These occurrences are confined to minor and patch releases.
Breaking Non-breaking
6

-- 6 of 334 --

Public API • Eliminate public signature
• Introduce an incompatible
alteration to the public
signature
• Modify code behavior
• Add a public signature to
the interface without
providing default
implementation
• For JSON-RPC: Include or
modify a mandatory field in
the request or
remove/change a field in
the response
• Providing new public API
Internal • Changes of application
behaviour which may affect
client projects
• All changes preserving the
behaviour are considered
non-breaking
Configuration • Introduce mandatory
configuration: Implement a
feature that cannot be
disabled via configuration.
• Remove configuration
• Rename configuration
• Change the default value for
a configuration key
• Change the behavior of the
configuration
• Introduce a new mandatory
configuration key with a
default value that maintains
the existing behavior.
• Integrate a feature that is
initially disabled by default
• Expand the configuration
key by adding a new
enumeration value
Server initialization sequence • Changes in the order of the
initialization sequence. This
applies also to the sequence
in which events are
published
• Changes in results of the
initialization sequence. No
extension points can be
removed or changed
• Changes in transaction
handling
• Changes in error handling
• Removal of information
• Changes in logging
7

-- 7 of 334 --

Dependencies • Upgrade to a new major
version of A12
dependencies
• Upgrade to a new major
version of 3rd party
dependencies
• Upgrade to a new minor or
patch versions of A12
dependencies
• Upgrading to a new version
of 3rd party libraries
involves the execution of
integration and regression
tests. If the behavior
remains satisfactory and
the change does not induce
modifications to the Public
API, we retain the right to
upgrade, even to a major
version. Such upgrades are
typically prompted by
security considerations
Other • Modifications necessitating
re-indexing
• Significant performance
degradation
• All changes to the DB or
search index.

The Data Services team understands that client projects are currently dependent
on the DB structure, and therefore we will try to avoid making any changes to
the DB in minor or patch versions, but we reserve a right to do so if it would be
necessary. We also ask client projects to not rely on the DB structure and try to find
different solutions because of the future plans.
How We Mark Deprecation?
• Identified with @Deprecated (Java) or @deprecated (TypeScript) annotations.
• Accompanied by thorough comments specifying the recommended new version usage.
• Documentation outlining Deprecated APIs will be included in the release notes.
• Scheduled for removal in major releases
"Only For Usage" and Customizable Interfaces
Within the application, we make extensive use of interfaces. These interfaces serve two primary
purposes: some are meant to enhance customization, while others are exclusively designated for
usage. To streamline distinction, we have established a simple guideline - if an interface is
annotated with @OnlyForUsage, it is designed for usage only; otherwise, it is meant for
implementation.
8

-- 8 of 334 --

Environment Setup
PostgreSQL Setup
This section contains recommendations to configure a PostgreSQL instance.
Locale Settings
By default, PostgreSQL will use the so-called "locale provider" provided by the operating system
(especially if running PostgreSQL on Linux). This can potentially lead to problems if the
corresponding library of the operating system is updated, resulting in differences when comparing
text values in the database. Such a change can potentially break (unique) indexes in the database
leading to wrong results or even corrupted indexes. Details on this problem can be found in the
PostgreSQL wiki.
To avoid problems with the locale provider it is recommended to initialize the PostgreSQL instance
(aka "cluster") using the builtin locale provider which will not be affected by updates to the
operating system.
This possibility was introduced in PostgreSQL 17 and there are two levels where a built-in provider
and locale can be selected.
• When running initdb to set up the PostgreSQL instance ("cluster")
• When creating a new database using CREATE DATABASE
The collation and collation provider of a database, can not be changed, once a database is created.
Parameters for initdb
When initializing a new PostgreSQL cluster using initdb the default locale and locale provider can
be set as a default for all databases. It is recommended to use the UTF-8 encoding.
The following command will initialize a PostgreSQL cluster using the builtin "C.UTF-8" locale which
is independent of the locale provider of the operating system.
initdb --locale-provider=builtin --builtin-locale="C.UTF-8" -E UTF8 ....
It is highly recommended to initialize the cluster with these settings to avoid and problems with
databases created in the future.
Creating a New Database
When creating a new database, the locale and locale provider can be specified:
Creating a new database with builtin locale provider
CREATE DATABASE new_database
LOCALE_PROVIDER = builtin
9

-- 9 of 334 --

BUILTIN_LOCALE = 'C.UTF-8';
If the cluster was initialized with e.g. the libc provider this will result in an error ("new locale
provider (builtin) does not match locale provider of the template database (libc)"). In that case, use
the template0 database:
Creating a new database with builtin locale provider using template0
CREATE DATABASE new_database
LOCALE_PROVIDER = builtin
BUILTIN_LOCALE = 'C.UTF-8'
TEMPlATE template0;
Monitoring
Starting with PostgreSQL 15, the collation version during creation of database objects is tracked.
Postgres log File
If there is a mismatch between the collation version used to create a database object and the
current version in the operating system, PostgreSQL will log an error message. It is recommended
to monitor the PostgreSQL log file for messages that contain:
WARNING: collation "..." has version mismatch
The "…" will contain the name of the affected collation.
Manual Checking
The version in use and the current version of the operating system’s library can be monitored
proactively with the following query from the PostgreSQL manual:
The query should be run every for each database, every time updates to the operating system were
applied.
Check for collation version mismatches
SELECT pg_describe_object(refclassid, refobjid, refobjsubid) AS "Collation",
pg_describe_object(classid, objid, objsubid) AS "Object"
FROM pg_depend d
JOIN pg_collation c
ON refclassid = 'pg_collation'::regclass
AND refobjid = c.oid
WHERE c.collversion <> pg_collation_actual_version(c.oid)
ORDER BY 1, 2;
 If the above query returns any results, all indexes using such a collation (i.e. those
10

-- 10 of 334 --

on text/varchar columns) must be rebuilt immediately before continuing to use the
system. Otherwise, data loss or corrupt data might be possible.
The safest option is to run REINDEX DATABASE; which rebuilds all indexes in the database, not only
those that would require a rebuild. However in an A12 database, nearly all indexes would be
affected, so only rebuilding affected indexes might not make a big difference.
To reduce the impact of reindexing, the following query from the PostgreSQL wiki can be used to
identify only those indexes that do require a rebuild:
Find indexes that require a rebuild
SELECT DISTINCT
indrelid::regclass::text as table_name,
indexrelid::regclass::text as index_name,
collname,
pg_get_indexdef(indexrelid) as index_definition
FROM (
SELECT indexrelid, indrelid, indcollation[i] coll
FROM pg_index
CROSS JOIN generate_subscripts(indcollation, 1) g(i)
) s
JOIN pg_collation c ON coll=c.oid
WHERE c.collprovider IN ('d', 'c')
AND c.collname NOT IN ('C', 'POSIX')
ORDER BY 1,2
Once all affected indexes are rebuilt, the recorded collation version can be updated using ALTER
DATABASE … REFRESH COLLATION VERSION.
This needs to be done for every affected database.
Spring Cache
Data Services uses Infinispan as its embedded Spring Cache provider. All caching is managed
through the standard Spring Cache abstraction (@Cacheable, @CacheEvict).
Hibernate Second Level Cache is not used.
Cache Provider
Data Services uses infinispan-spring-boot4-starter-embedded to provide an embedded Infinispan
instance. The cache manager is exposed as a SpringEmbeddedCacheManager Spring bean.
The following Spring Boot properties activate the provider:
spring.cache.type=infinispan
infinispan.embedded.configXml=infinispan.xml
11

-- 11 of 334 --

The infinispan.xml file on the application classpath defines all cache names, time-to-live (TTL)
values, and entry limits.
INVALIDATION Mode
All caches operate in INVALIDATION mode. In this mode each pod holds an independent in-
memory copy of cached data. When an entry is evicted on one pod, JGroups transmits a lightweight
invalidation message (~100 bytes) to all other cluster members, causing them to remove that entry
from their local caches. No cached data is transferred between pods.
Cache Definitions
The table below lists all named caches with their configured TTL and maximum entry count.
Cache Name TTL (ms) Max Entries Notes
default 1 200 000 200 General-purpose fallback for
any unnamed cache
validationCache 3 600 000 500 Document model validation
results
securedModelReadCache 36 000 000 200 Security-filtered model
reads; evicted cluster-wide
on security model update
com.mgmtp.a12.dataservices.
model.GenericModel
36 000 000 100 Generic model read results
com.mgmtp.a12.kernel.md.mod
el.api.IDocumentModel
36 000 000 100 Document model read
results
com.mgmtp.a12.dataservices.
relationship.model.Relation
shipModel
36 000 000 100 Relationship model read
results
com.mgmtp.a12.dataservices.
cdd.jms.internal.ComposeDoc
umentModel
36 000 000 200 Compose document model
read results
documentModelIndexedFieldsC
ache
None (-1) 200 Derived indexed-field
metadata; never expires
documentModelIsIndexedField
Cache
None (-1) 500 Per-field index flag lookup;
never expires
modelGraphCache 86 400 000 100 Model type hierarchy graph
modelSubTypesMapCache 86 400 000 100 Model hierarchy subtype
maps
documentModelSearchServiceC
ache
None (-1) 200 Document model search
service results; never
expires
userCache None (-1) 200 Authenticated user details;
invalidated on logout
12

-- 12 of 334 --

Cache Name TTL (ms) Max Entries Notes
tokenCache 86 400 000 50 Authentication tokens
roleMapping 86 400 000 100 User role assignments
Calibrating Entry Limits
The memory max-count values above are conservative defaults. Calibrate these values in a staging
environment before production rollout by inspecting live cache statistics through Spring Actuator
(see Cache Inspection). Adjust infinispan.xml and redeploy if caches exceed configured limits
frequently.
Cross-Pod Cache Eviction
In a multi-pod Kubernetes deployment, cache eviction must be propagated to all running pods so
that no pod serves stale data after a model or security update.
JGroups Transport
Data Services uses JGroups TCP with DNS_PING for cluster peer discovery. All pods in the same
cluster join a named JGroups cluster (dataservices-cluster). When cache.invalidate() is called on
one pod, JGroups sends an INVALIDATE message to every peer. Each peer removes the named entry
from its local cache.
The JGroups transport binds to port 7800 on the pod’s site-local address.
Kubernetes Headless Service
DNS_PING resolves peer addresses by querying a Kubernetes headless Service. A headless Service
returns the IP addresses of all matching pods directly.
Create the following headless Service in the Data Services namespace:
apiVersion: v1
kind: Service
metadata:
name: dataservices-jgroups
spec:
clusterIP: None
selector:
app: dataservices
ports:
- name: jgroups
port: 7800
protocol: TCP
The dns_query in infinispan.xml must match the headless Service name and namespace:
<org.jgroups.protocols.dns.DNS_PING
13

-- 13 of 334 --

dns_query="${jgroups.dns.query:dataservices-jgroups.default.svc.cluster.local}"
dns_record_type="A"/>
Replace default with the actual Kubernetes namespace. The system property jgroups.dns.query
allows overriding the value at runtime without rebuilding the image:
-Djgroups.dns.query=dataservices-jgroups.my-namespace.svc.cluster.local
Deployment Port Requirement
Each Data Services pod must expose port 7800 (TCP) for JGroups communication. Add the following
to the Deployment container specification:
ports:
- name: jgroups
containerPort: 7800
protocol: TCP
Single-Pod and Local Development Behavior
In single-pod deployments or local development environments, DNS_PING returns no peers. The
Infinispan instance forms a cluster of size 1 without errors. Cache entries are evicted from the
single pod’s local cache normally. No additional configuration is required for local development.
No-Cache Profile
When the Spring profile no_cache is active, the Infinispan beans are not created and
spring.cache.type is set to none. All @Cacheable methods pass through to the underlying service or
repository on every call. This profile is intended for development and debugging scenarios where
caching would interfere with data visibility.
Cache Inspection
Spring Actuator
Expose the caches actuator endpoint to inspect all registered caches at runtime:
management.endpoints.web.exposure.include=caches,health,info
Query available caches:
GET /actuator/caches
The response lists all named caches with their current size and hit/miss statistics. Use this
information to calibrate memory max-count values before production rollout.
14

-- 14 of 334 --

Debug Logging
Enable Infinispan debug logging to trace cache operations:
logging.level.org.infinispan=DEBUG
This setting produces verbose output and is not suitable for production. Use it only for targeted
troubleshooting.
Java BIO / NIO / NIO2 / APR
BIO (Blocking I/O):
• Stream-oriented, assigns a thread per request, blocks during read/write.
• Simple and easy to use.
• Blocks file access during operations, no support for virtual files or symbolic links.
NIO (Non-blocking I/O):
• Buffer-oriented, uses channels and selectors for concurrent access.
• Supports asynchronous operations and better performance for large/many files.
• Less efficient for small files due to cache orientation.
NIO2:
• Extends NIO with advanced file and directory management (Path, FileSystem, FileStore).
• Recommended for complex directory/file operations.
APR (Apache Portable Runtime):
• Native library for enhanced performance and scalability, often used with Tomcat.
• Relevant for high-performance scenarios.
Usage
• Tomcat uses NIO protocol by default.
• To enable NIO2: set protocol to org.apache.coyote.http11.Http11Nio2Protocol.
Performance
• For small files (< 1 MB): BIO (100%) > NIO2 (109%) > NIO (190%).
• NIO is cache-oriented, so slower for small files; NIO2 improves file management.
• For large files: NIO outperforms BIO and NIO2 due to better concurrency.
15

-- 15 of 334 --

Recommendation
• Use BIO for simple, blocking file access (DS recommends this setup).
• Use NIO for scalable, concurrent file operations.
• Use NIO2 for advanced file management.
• Consider APR for native performance needs.
Manual Database Setup
Normally, this step is done automatically by the server, but in case you want to execute it manually
e.g. as a different user, you can execute steps to install and update the database manually.

This will not generate scripts that can be run against any database without
modification. This only provides queries from the Liquibase migration scripts.
These scripts contain preconditions which are evaluated during runtime, and
based on the result of these preconditions the query is either executed or not.
These preconditions cannot be expressed in the generated scripts without
Liquibase.
Prerequisites
1. You have to have Liquibase command line tool version 4.33.0 available. Be careful about the
version. Different versions of Liquibase are not interchangeable. You can download it here:
https://github.com/liquibase/liquibase/releases/tag/v4.33.0. Download liquibase-4.33.0.zip and
unzip it to a directory of your choice, let’s say /tmp/a12dataservices_db/.
2. You have to have Data Services artifact: dataservices-server-app-39.0.2-fatjar.jar.
Unpack dataservices-core jar, Quartz jar, and DB driver from artifact.
jar xf dataservices-server-app-39.0.2-fatjar.jar BOOT-INF/lib/
mv BOOT-INF/lib/dataservices-core-39.0.2.jar BOOT-INF/lib/quartz-2.5.2.jar BOOT-
INF/lib/postgresql-42.7.10.jar .
Now you should have in your directory (/tmp/a12dataservices_db/)
• the liquibase distribution (liquibase, liquibase.jar),
• the dataservices-core jar (dataservices-core-39.0.2.jar),
• the Quartz jar(quartz-2.5.2), and
• the DB driver (postgresql-42.7.10.jar).
You can delete BOOT-INF now.
Running DB Upgrade
• To install or upgrade DB structure for Data Services, run
16

-- 16 of 334 --

./liquibase --classpath dataservices-core-39.0.2.jar:quartz-2.5.2.jar:postgresql-
42.7.10.jar --changeLogFile=database/project_model.xml --url
jdbc:postgresql://localhost:5432/DB_NAME --username DB_USER --password DB_PASSWORD
update
• If you just want to get update SQL run
./liquibase --classpath dataservices-core-39.0.2.jar:quartz-2.5.2.jar:postgresql-
42.7.10.jar --changeLogFile=database/project_model.xml --url
jdbc:postgresql://SERVER_HOST:5432/DB_NAME --username DB_USER --password DB_PASSWORD
updateSQL
The --changeLogFile=database/project_model.xml is always the same as it points to path inside
dataservices-core-39.0.2.jar.
For detailed help you can run ./liquibase without parameters to see available commands, options
and examples. Additionally, refer to the Liquibase documentation for the detailed information.
Possible Problems
Database Lock
Liquibase would run its migration only once. It handles this by creating a lock in the table
DATABASECHANGELOGLOCK.
If the application is killed during a Liquibase migration, it may happen that the lock is not released,
so that the application can show "Waiting for changelog lock…." for an indefinite time in the logs.
To solve this, you can run the following SQL command against the database:
UPDATE DATABASECHANGELOGLOCK SET LOCKED=0, LOCKGRANTED=null, LOCKEDBY=null where ID=1;
 Depending on the DBMS, you may need to replace LOCKED=0 with
LOCKED=FALSE.
See more: https://stackoverflow.com/questions/15528795/liquibase-lock-reasons
Spring Actuator Support
Spring Actuator provides endpoints that expose runtime application details such as health, metrics
and configuration.
All standard Spring Actuator endpoints are supported in this application. Endpoints are secured
using the ACCESS_ACTUATOR access right. Only users whose roles include this access right are
authorized to access actuator endpoints.
17

-- 17 of 334 --

By default, all actuator endpoints (/actuator/*) require both authentication and the ACCESS_ACTUATOR
role. The only exception is the health endpoint (/actuator/health/**), which remains publicly
accessible to support Kubernetes liveness and readiness probes.
Endpoints can also be remapped to a custom root path or exposed on a different port.
Security
Actuator endpoint security is enforced at two layers:
1. Authentication — All actuator endpoints except /actuator/health/** require a valid
authentication token (JWT). Unauthenticated requests receive HTTP 401.
2. Authorization — Authenticated users must hold the ACCESS_ACTUATOR access right. Users without
this right receive HTTP 403.
To grant a user access to actuator endpoints, assign a role that includes the ACCESS_ACTUATOR access
right (e.g., the ActuatorAccess role).
Using Actuators in a Cluster
When actuator endpoints require authentication, cluster-internal consumers (Prometheus scrapers,
monitoring agents) need a mechanism to authenticate. The recommended approach is:
Separate Management Port (Preferred for Kubernetes)
Expose actuator on a dedicated management port (e.g., 8081) that is not reachable from outside the
cluster. Use Kubernetes NetworkPolicy to restrict access to the monitoring namespace only.
# application.yml
management:
server:
port: 8081 # separate from app port 8080
endpoints:
web:
exposure:
include: health,prometheus,info
The management port is only accessible within the cluster — network isolation serves as the auth
layer. The Helm chart supports this via containerPorts.management.
Other Authentication Patterns
For scenarios where network isolation is not sufficient, the UAA library supports additional
authentication mechanisms (OAuth2 service accounts, mTLS, API keys). Refer to the UAA
documentation for details on configuring these patterns for actuator endpoint access.
18

-- 18 of 334 --

Initialization Finished Health Indicator
The server has a custom health indicator, which can be checked at /actuator/health or at
actuator/health/dataservicesInitializationFinished. This indicator gives information about
whether the server initialization is finished or not.
Example response from 'GET /actuator/health'
{
"status": "UP",
"components": {
"db": {
"status": "UP",
"details": {
"database": "PostgreSQL",
"validationQuery": "isValid()"
}
},
"diskSpace": {
"status": "UP",
"details": {
"total": 511091388416,
"free": 294934315008,
"threshold": 10485760,
"exists": true
}
},
"ping": {
"status": "UP"
},
"dataservicesInitializationFinished": {
"status": "UP",
"details": {
"dataServicesInitialization": "Finished"
}
}
}
}
In this case, in the JSON, components.dataservicesInitializationFinished.status equals UP means
initialization is finished. Otherwise, the initialization is not finished yet.
Example response from 'GET /actuator/health/dataservicesInitializationFinished'
{
"status": "UP",
"details": {
"dataServicesInitialization": "Finished"
}
}
19

-- 19 of 334 --

In this case, the JSON returned is only about the desired endpoint
dataservicesInitializationFinished, where the status property already tells you whether the
initialization is finished (UP) or not.
Check Configuration options for instructions on how to configure an actuator.
Configuration of Actuator Endpoint
The configuration actuator provides information about the currently applied configuration on the
running DS server. The actuator is accessible via GET request to the /actuator/configuration
resource.
This actuator gives information on configuration changes and on warnings concerning the
configuration of the Data Services server.
To enable this endpoint, provide the properties below:
Enable configuration actuator endpoint
management.endpoints.web.exposure.include="configuration"
management.endpoint.configuration.enabled=true
Example response from 'GET /actuator/configuration'
{
"changes": {
"mgmtp.a12.dataservices.documents.validation.skipForModels": {
"current": "[SkippedValidationModel]",
"default": "null"
},
"mgmtp.a12.dataservices.jsonRpc.spel.enabled": {
"current": "true",
"default": "false"
},
"mgmtp.a12.dataservices.initialization.import.models.overwrite.models": {
"current": "{DomainProduct=false, DomainBrand=false}",
"default": "null"
}
},
"warnings": []
}
The /actuator/configuration/changes endpoint will print a list of all configuration changes, so you
can compare this list with the output of the previous version.
Example response from 'GET /actuator/configuration/changes'
{
"changes": {
"mgmtp.a12.dataservices.documents.validation.skipForModels": {
20

-- 20 of 334 --

"current": "[SkippedValidationModel]",
"default": "null"
},
"mgmtp.a12.dataservices.jsonRpc.spel.enabled": {
"current": "true",
"default": "false"
},
"mgmtp.a12.dataservices.initialization.import.models.overwrite.models": {
"current": "{DomainProduct=false, DomainBrand=false}",
"default": "null"
}
}
}
With the help of the /actuator/configuration/warnings endpoint possible problems of your current
configuration are reported.
Extending the Configuration Endpoint With Custom Properties
 Refer to the DS examples section for an example of adding a custom configuration
to the actuator endpoint.
It is possible to make the actuator endpoint scan your custom configuration (together with its
default values) so any changes to it will be reported in /actuator/configuration/changes. To enable
this feature, you need to map your configuration keys and its default values to the Java class using
the ConfigurationProperties annotation. Then, simply annotate your class by
@ExposePropertiesToActuator annotation provided by Data Services to expose it to the actuator.
Example:
Expose custom configuration properties to actuator
@ExposePropertiesToActuator
@ConfigurationProperties("customProperties")
public class CustomConfigurationProperties {
// custom configuration keys
}
Initialization Phase
 Refer to DS examples section for an example of custom initialization.
Data Services uses multiple persistence stores that need to be synchronized. Synchronization
between search index, database and other parts can be achieved in 2 ways:
1. Using initialization phase of dataservices-server-app
◦ This option is mainly used for development and demonstration purposes.
21

-- 21 of 334 --

2. Using dataservices-server-init-app
◦ For production purposes it is not desired to run initialization phase every time the server
is started. For those purposes, Data Services provide initialization/migration application.
This application can be used from migration/development scripts.
Initialization Sequence
Even though initialization sequence is enabled by default, we recommend to disable it for the
production environments and move all migration and initialization logic to dataservices-server-
init-app instead. Initialization sequence is the same in both for dataservices-server-server-app and
dataservices-server-init-app.
Table 1. Initialization sequence
Step Fail on error Intended use Trigger
Database migration yes Should be run just in
case of database
changes. It can be
executed multiple
times because it
remembers previous
runs.
Triggered by
unexecuted changesets
from Liquibase
Model import yes Should be run just in
case of models change
or for the first time.
Triggered by
configuration key
mgmtp.a12.dataservices
.initialization.import
.models.path by
pointing to the path
where are the models
located.
Data migration yes Migration by
MigrationSteps
extension points. It can
run multiple times
because it remembers
previous runs.
Existence of the
MigrationStep
implementation that
was not executed yet or
marked as always to be
executed
22

-- 22 of 334 --

Step Fail on error Intended use Trigger
Index synchronization yes, but documents
which cannot be
deserialized will be
ignored. Only other
errors will fail the
process
Should be executed just
in case the index and
the DB are not in sync.
This scenario is very
unlikely since index is a
set of Database tables
as well. It might
happen when manual
changes in the
documents have been
done while index
update was not
executed.
Triggered by
configuration key
mgmtp.a12.dataservices
.query.reindexing.mode
Custom initialization
logic
yes Depends on custom
code.
DataServicesCustomInit
ializationEvent
Execution of custom
JSON-RPC requests
yes Depends on custom
code. May be used e.g.
for document import.
All the steps mentioned above ignore security because security can only be applied once all models
and documents are accessible from the database and from the search engine.
Every step from the table above is executed in its own transaction and is committed right after the
step passes.
Keep in mind that Database migration and security is initialized on ContextRefreshedEvent. This
event is not fired automatically but must be thrown manually. We do it during initialization:
Example of starting Spring application with initialization
final SpringApplication application = new SpringApplication(ServerApplication.class);
application.run(args).start();
In case that you initialize your application manually you have to call start() method on application
context explicitly.
Database Migration
Database migration is done using Liquibase. You can see all defined changesets in the dataservices-
core/src/main/resources/database/project_model.xml
Model Import
This phase allows importing models to the server before any other code will be executed.
If the models are already uploaded in a proper version, please point beneath-mentioned
configuration properties to empty directories so no unnecessary model overwriting will be done.
23

-- 23 of 334 --

Models will be preloaded by enabling
mgmtp.a12.dataservices.initialization.import.models.enabled and setting
mgmtp.a12.dataservices.initialization.import.models.path. This property contains comma
separated list of paths where Models will be searched. Base path could be in one of the forms:
classpath:/SOMEPATH
Models are searched in the classpath of the server from the base of SOMEPATH.
file:/SOMEPATH
Models are searched in the filesystem from the base of SOMEPATH.
file:/SOMEPATH/SOMEFILE.zip
Models are searched in the zip file SOMEFILE.zip.
Example:
mgmtp.a12.dataservices.initialization.import.models.path=classpath:/businessmodels,file:/usr/lo
cal/share/businessmodels,file:/usr/local/share/businessmodels.zip
Models must be provided as runtime models with includes already resolved and __meta metadata
already injected. Use the WCF/RMC build-time pipeline to process raw models before importing
them into Data Services.
It is possible to set rules for model overwriting. See mgmtp.a12.dataservices.import.models.*
properties in model overwriting section.
 Changing of the model type is not permitted during the model import,
IntegrityException with message Changing of model [MODEL_NAME] type is not
permitted will be thrown.
Full Model Import
Full model import provides an option to delete all models stored in a database. It can be used in
case of changing specific model type as changing of model types is not allowed by Data Services. We
allow to delete models of specific type by providing list of model types that should be deleted from
a database.
Example configuration for full model import
//For deleting all models:
--mgmtp.a12.dataservices.initialization.import.models.typesToClear=*
//For deleting specific models:
--mgmtp.a12.dataservices.initialization.import.models.typesToClear=modelType1,modelTyp
e2
 Document models for importing must be encoded with UTF-8 charset, otherwise
unexpected characters will be displayed instead.
24

-- 24 of 334 --

 Please be aware of adding JVM argument -Dfile.encoding=utf-8 when running
Data Services application by using JAR artifact, this argument is mandatory for the
application to handle file encoding properly.
Data Migration
One of the very first initialization steps is the migration. More information can be found on
migration page.
Index Synchronization
DS is using custom database tables to store the documents redundantly for search purposes. These
tables are automatically updated by the Data Services core when documents are created, updated
or deleted. However, in some cases it is necessary to synchronize the search index with the
database. There are configuration options available to rebuild the search index from the database.
For more info please see query configuration options documentation for further information.
Custom Logic
DataServicesCustomInitializationEvent
A custom logic can be triggered during the server initialization by
DataServicesCustomInitializationEvent. It is fired after the server initialization is done but before
JSON-RPC initializer is executed.
ContextRefreshedEvent
If you need to have a listener for the Spring’s ContextRefreshedEvent, be aware that our
DataServicesInitializationListener has @Order with value 100. So, put the @Order value on your
listener to be less than 100 if you want it to be handled before the
DataServicesCoreInitializationListener.
Execution of Custom JSON-RPC Requests
During server initialization phase any JSON-RPC request can be executed.
This feature needs to be activated using the property
mgmtp.a12.dataservices.initialization.scripts.jsonRpc.enabled=true.
JSON-RPC requests may be located in any path specified via
mgmtp.a12.dataservices.initialization.scripts.jsonRpc.paths. Files are executed in ascending
order by file name.
Since custom JSON-RPC requests are executed after model import during initialization phase, they
can be used e.g. for document import using the ADD_DOCUMENT RPC method.
25

-- 25 of 334 --

Cache Preloading
All document models stored in the persistent store will be asynchronously loaded to the cache after
the server initialization phase is finished. This is done to improve the performance of the first
requests to the server. Cache preloading is triggered by DataServicesInitializationFinishedEvent.
The following caches are preloaded during startup:
• Model pre-compilation cache: Compiles document models for runtime use
• Validation code cache: Generates validation code for document models
• Indexed field cache: Caches the set of indexed field paths for each Document Model, improving
the performance of first query operations
• Document model search service cache: Initializes the search service for each Document
Model
Cache preloading is performed in parallel for all document models, using the common
ForkJoinPool.
It is possible to disable cache preloading for certain (or all models) by configuration key
mgmtp.a12.dataservices.initialization.preCompile.enabledForModels
Cluster-safe Configuration
The initialization sequence of the server is by default enabled. All initialization steps are therefore
performed during every server initialization, which might cause issues during restarts of
production servers (restart of servers should not result in importing models or migration of
database, etc).
In special cases you may want to split the initialization sequence of the application and common
application runtime (e.g. with clusters, where the initialization steps should be performed just once
and runtime workers should just run and handle requests).
In such cases you can run init application (you can find it in the dataservices-server-init-app) to
perform the initialization steps and then configure server workers to bypass the initialization
steps. For example:
Example configuration to disable initialization sequence
# Disable initialization of database schema import
spring.liquibase.enabled=false
# Disable initialization data import
mgmtp.a12.dataservices.initialization.import.models.enabled=false
mgmtp.a12.dataservices.initialization.import.documents.enabled=false
mgmtp.a12.dataservices.initialization.migration.enabled=false
For more convenient configuration please consider using cluster configuration profile.
26

-- 26 of 334 --

Configuration
 Refer to the DS examples section for an example of DS configuration.
Data Services are using Spring for all configuration needs. All Data Services configuration keys start
with the mgmtp.a12.dataservices prefix.
DS is a multi-module project, so we defined the following rule: Configuration from higher
modules should be able to override configuration from lower modules.
Our default configuration is defined in property files prefixed with dataservices and is by default
applied via @PropertySource annotations which makes it overridable by application.properties or
other alternatives. For more information, see the Spring documentation.
Exceptions are the following modules:
• dataservices-modelgraph-fs-impl
• dataservices-client-cli
For these modules, using application.properties in a root of resources folder to configure them is
not possible, it will result in a bad property ordering. Please, place your application.properties
under config directory which will give your properties required precedence.
For more convenient configuration please see Configuration Profiles.

Spring Boot allows flexible naming when binding environment or properties
values: you don’t have to match names exactly the property name (e.g. firstName)
— alternative forms like first-name, first_name, uppercase variants, etc. are all
accepted. For more information, see the Spring documentation.
Configuration Options
Permanent Configuration
This configuration keys are required for Data Services to run normally and thus should not be
changed.
liquibase.changeLog = classpath:/database/project_model.xml
Liquibase change log configuration
spring.data.jpa.repositories.enabled = true
Spring JPA repositories switch.
spring.datasources.dataservices.jpa.hibernate.ddlAuto = none
Database schema generation
27

-- 27 of 334 --

Changeable Configuration
This block of configuration options focuses on database connections:
spring.datasources.dataservices.url = jdbc:postgresql://localhost:5432/a12database
Database connection string
spring.datasources.dataservices.name = postgresql
Database name
spring.datasources.dataservices.username = username
Username for database connection
spring.datasources.dataservices.password
Password for database connection
You can define your own Liquibase migration datasource by providing a bean with
LiquibaseDatasource annotation like:
Example of custom Liquibase migration datasource
@LiquibaseDataSource @Bean public DataSource dsMigrationDataSource(Object... params) {
// your implementation
}
If custom liquibase migration datasource is not provided, this block of configuration options
focuses on liquibase datasource for database migration:
spring.datasources.dataservices.liquibase.url
The database connection string to apply liquibase migration, if this property is provided, the
following user and password below are required, if not this connection will be ignored and Data
Services datasource will be applied instead.
spring.datasources.dataservices.liquibase.user
The username which liquibase will use on behalf, while performing migration, if this property is
provided but above liquibase url is missing then Data Services url will be used instead.
spring.datasources.dataservices.liquibase.password
The password of liquibase user
spring.datasources.dataservices.liquibase.driver-class-name
Database driver
There are additional tuning properties depending on the connection pool implementation. By
default, Spring (and thus Data Services) uses com.zaxxer.hikari.HikariDataSource.
spring.datasources.dataservices.hikari.connectionTimeout = 30000 (30 seconds)
See the Hikari FAQ
28

-- 28 of 334 --

Read Replica Configuration (Optional)
Data Services supports optional routing of read-only JSON-RPC transactions to a PostgreSQL read
replica. When the property spring.datasources.dataservices-read-replica.url is present, the
routing is activated automatically. No other changes are required. See Read Replica Routing for
routing behavior and consistency trade-offs.
The recommended way to enable read replica support is to activate the dataservices-
postgres_read_replica Spring profile and supply the connection details:
Minimum configuration using the built-in profile
spring.profiles.include=dataservices-postgres_read_replica
spring.datasources.dataservices-read-replica.url=jdbc:postgresql://replica-
host:5432/dataservices
spring.datasources.dataservices-read-replica.username=ds_user
spring.datasources.dataservices-read-replica.password=secret
The profile pre-configures spring.datasources.dataservices-read-replica.hikari.read-only=true,
which enforces read-only mode at the JDBC driver level and prevents accidental writes through the
replica connection pool.
Available configuration properties:
spring.datasources.dataservices-read-replica.url
JDBC URL of the read replica datasource. When absent (the default), all database traffic goes to
the primary datasource and the routing infrastructure is not activated.
spring.datasources.dataservices-read-replica.username
Username for the replica datasource connection.
spring.datasources.dataservices-read-replica.password
Password for the replica datasource connection.
spring.datasources.dataservices-read-replica.hikari.read-only = true (set by profile)
Instructs HikariCP to enforce read-only mode at the JDBC driver level for every connection in
the replica pool. Set by the dataservices-postgres_read_replica profile. Override only if you
manage pool settings manually.
spring.datasources.dataservices-read-replica.hikari.pool-name
Name of the HikariCP connection pool for the replica datasource. Useful for distinguishing
replica pool metrics and thread names from the primary pool.
spring.datasources.dataservices-read-replica.hikari.maximum-pool-size
Maximum number of connections in the replica HikariCP pool. Defaults to the HikariCP default
(10). Tune this value according to the replica’s capacity and the expected read concurrency.
 To disable replica routing, remove the spring.datasources.dataservices-read-
29

-- 29 of 334 --

replica.url property and restart the server.
Multiple Read Replicas
Data Services accepts a single replica endpoint URL. Multiple physical replicas are not managed at
the application level. To distribute read traffic across several replicas, configure infrastructure-level
load balancing and point spring.datasources.dataservices-read-replica.url at the balancer
endpoint:
• PgBouncer — connection pooler with round-robin or least-connections routing across replica
hosts
• HAProxy — TCP load balancer; expose a single virtual IP in front of N replica nodes
• AWS RDS / Aurora — use the cluster reader endpoint, which automatically routes to available
read replicas
• pgpool-II — PostgreSQL-native proxy with built-in load balancing and health checking
From the Data Services perspective, the balancer is just another JDBC host. No application changes
are required.
 If another connection pool implementation should be used (i.e., if client project
overrides spring.datasource.type property), configuration of DataSources (for
DataServices and ContentStore) need to be provided also by client project.
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass = Spring managed
Spring target should be auto-detected, but this is not working for Postgres, so it is more secure to
configure DB type for Quartz manually. It should be class of type
org.quartz.impl.jdbcjobstore.StdJDBCDelegate (one of
org.quartz.impl.jdbcjobstore.StdJDBCDelegate,
org.quartz.impl.jdbcjobstore.PostgreSQLDelegate,).
For more convenient configuration please consider using Postgres configuration profiles.
If the connection to the database server is lost, after connectionTimeout, Data Services server starts
to respond with server error until the connection is restored. Then it continues to respond to HTTP
requests regularly.
Custom Caches
mgmtp.a12.dataservices.cache.modelGraph.enabled = true: boolean
Switch on for better production performance for LIST_* operations.
The Data services server uses Database and Search index to store documents.
Query Configuration
Query is configured with the following configuration properties:
30

-- 30 of 334 --

mgmtp.a12.dataservices.query.aggregation.defaultPrecision = 2: int
Default precision for aggregation functions.
mgmtp.a12.dataservices.query.aggregation.listSize = 10: int
Fixed page size for aggregation endpoint. In aggregation endpoint it is not possible to have
pagination, so this value is used to limit the number of results returned.
mgmtp.a12.dataservices.query.disabledOperators = empty list: java.util.List<java.lang.String>
List of disabled operators. If a query contains one of the disabled operators, an
InvalidInputException is thrown. Possible values are:
• and
• or
• not
• exact_match
• simple_search
• has
• undefined match
• date_range
• datefragment_range
• double_range
mgmtp.a12.dataservices.query.exactMatch.maxInputValueLength = 100: int
The maximum allowed length (in characters) for an input value provided in a exact_match
operation. This limit helps to prevent excessively long inputs that could negatively impact
PostgreSQL regular expression search. Any input value exceeding this configured length will
result in an error response from the API.
mgmtp.a12.dataservices.query.exactMatch.maxValuesCount = 100: int
The maximum number of values allowed in the values list of an exact_match operation. This limit
helps to prevent excessively large value lists that could negatively impact query performance.
Any values list exceeding this configured count will result in an error response from the API.
mgmtp.a12.dataservices.query.maxAndOperands = 1000: int
Hard limit for the number of operands of an or operator. If this limit is exceeded, an
InvalidInputException is thrown.
mgmtp.a12.dataservices.query.maxAndOperators = 1000: int
Hard limit for the number of and operators per query. If this limit is exceeded, an
InvalidInputException is thrown.
mgmtp.a12.dataservices.query.maxLinksSize = 10_000: int
Hard limit for the result of each links section. If this limit is exceeded, an InvalidInputException
is thrown.
31

-- 31 of 334 --

mgmtp.a12.dataservices.query.maxOrOperands = 1000: int
Hard limit for the number of operands of an or operator. If this limit is exceeded, an
InvalidInputException is thrown.
mgmtp.a12.dataservices.query.maxOrOperators = 1000: int
Hard limit for the number of or operators per query. If this limit is exceeded, an
InvalidInputException is thrown.
mgmtp.a12.dataservices.query.maxQueryDepth = 10: int
Hard limit for query depth. If this limit is exceeded, an InvalidInputException is thrown.
mgmtp.a12.dataservices.query.pageRequest.pageNumberLimit = 100: int
Hard limit for query page number. If this limit is exceeded, an InvalidInputException is thrown.
mgmtp.a12.dataservices.query.pageRequest.pageSizeLimit = 100: int
Hard limit for query page size. If this limit is exceeded, an InvalidInputException is thrown.
mgmtp.a12.dataservices.query.reindexing.applyToModels = * meaning that all models are
considered: java.util.List<java.lang.String>
A list of model names to which the reindexing operation will be applied. Setting the value to "*"
permits all existing models. Note: If "*" isn’t the only string in the list, no special meaning will be
applied.
mgmtp.a12.dataservices.query.reindexing.batchSize = 2_000: int
Number of documents to reindex in a single batch. Batches are processed in parallel in
#numberOfThreads threads.
mgmtp.a12.dataservices.query.reindexing.ignoreErrors = false: boolean
A switch to allow ignoring errors during re-indexing. If an error occurs, it will be logged, but the
server initialization will continue without interruption.
 DS by default skips documents that can not be deserialized during reindexing.
This property does not change this behavior.
mgmtp.a12.dataservices.query.reindexing.mode = DISABLED: enum
A switch that allows index manipulation operations to be performed during the DS initialization.
Possible values:
• REBUILD_INDEX: Deletes the complete content of the index and reconstructs it based on the
current documents in the system.
• INDEX_NEW_ONLY: Indexes only the documents that have not been indexed yet.
• DISABLED: No index operations are performed on initialization.
Note that it is possible to control indexing on per-model basis using
mgmtp.a12.dataservices.query.reindexing.applyToModels property.
32

-- 32 of 334 --

mgmtp.a12.dataservices.query.reindexing.modelFields.enabled = true: boolean
Controls whether the model index fields should be re-indexed.
mgmtp.a12.dataservices.query.reindexing.numberOfThreads = 5: int
Number of threads to use for reindexing.
mgmtp.a12.dataservices.query.reindexing.vacuum.enabled = true: boolean
Controls whether the VACUUM ANALYZE should be executed after index rebuild.
mgmtp.a12.dataservices.query.relationshipOrder.maxCount = 5: int
Maximum number of relationship-based sort orders allowed in a single query. This limits the
total number of relationship sorts to prevent complex queries with too many JOINs. Direct field
sorts are not counted against this limit.
mgmtp.a12.dataservices.query.relationshipOrder.maxNestingDepth = 5: int
Maximum nesting depth for relationship-based sorting. This limits how many relationship hops
can be traversed in a single sort specification. For example, sorting by Contract \u2192
BusinessPartner \u2192 Address.city has depth 2. Prevents excessive JOINs and protects against
circular reference issues.
mgmtp.a12.dataservices.query.searchIndexing.enabled = true: boolean
Controls whether documents are indexed into document_fields and document_search tables. When
disabled, the beans responsible for populating these tables are not created, and document CRUD
operations will not populate the search index tables. Note: Disabling this will make document
querying via the Query API unavailable.
 When disabled, this property overwrites functionality of
mgmtp.a12.dataservices.query.reindexing.mode. No index update will be
performed during init or during document CRUD operations.
mgmtp.a12.dataservices.query.simpleSearch.excludingMetadata.enabled = false: boolean
Whether to exclude metadata from the search
mgmtp.a12.dataservices.query.simpleSearch.maxInputValueLength = 100: int
The maximum allowed length (in characters) for an input value provided in a simple_search
operation. This limit helps to prevent excessively long inputs that could negatively impact
PostgreSQL regular expression search. Any input value exceeding this configured length will
result in an error response from the API.
mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize = 3: int
The minimum size of a token that can be included in the search. Tokens smaller than this size
will be ignored in the search process. A value less than 3 is not recommended because it can
degrade performance by increasing the number of irrelevant matches.
mgmtp.a12.dataservices.query.validation.enabled = true: boolean
This is a switch to enable/disable query validation. Please read documentation about validation
phase of Query API. This property should not be used in productional environments there are
performance and security concerns. In version 39.0.0 we will change a default value of this
33

-- 33 of 334 --

property to false.
mgmtp.a12.dataservices.client.configuration.query.scanPackages = Data Services package:
com.mgmtp.a12.dataservices: java.util.List<java.lang.String>
Packages for scanning to custom json mapping.
Document Related Configuration
mgmtp.a12.dataservices.documents.computation.cleanupErrorAndNotComputedValue.enabled = false:
boolean
If true, we apply kernel API for cleaning up error and not computed field after computation.
mgmtp.a12.dataservices.documents.computation.enabledForModels = null:
java.util.List<java.lang.String>
Enable computation for provided document models on save.
mgmtp.a12.dataservices.documents.delete.cascadeLinks.disabledForModels = null:
java.util.List<java.lang.String>
Contains a list of document model names for which links must not be deleted. To disable
deletion for all models, use "*". If a model name is specified in this list, deletion of any links
belonging to that model will not be performed.
mgmtp.a12.dataservices.documents.multiDelete.limit = 50.: int
Hard limit for maximum amount of documents to be deleted in
com.mgmtp.a12.dataservices.document.operation.internal.MultiDeleteDocumentsOperation.
mgmtp.a12.dataservices.documents.persistTransientFields.enabled = false: boolean
Switch for enabling/disabling persistence (consequently indexing) of transient fields
mgmtp.a12.dataservices.documents.validation.enabled = true: boolean
If true, documents are fully validated by default on save. Only documents of models listed in
#partialForModels and #skipForModels are handled differently.
mgmtp.a12.dataservices.documents.validation.partialForModels = null:
java.util.List<java.lang.String>
For documents of these models validate just fields which are set.
mgmtp.a12.dataservices.documents.validation.skipForModels = null:
java.util.List<java.lang.String>
Skip validation of these models on save.
mgmtp.a12.dataservices.initialization.import.documents.computation.cleanupErrorAndNotComputedVa
lue.enabled = false: boolean
If true, we apply kernel API for cleaning up error and not computed field after computation.
mgmtp.a12.dataservices.initialization.import.documents.computation.enabledForModels = null:
java.util.List<java.lang.String>
Enable computation for provided document models on save.
34

-- 34 of 334 --

mgmtp.a12.dataservices.initialization.import.documents.delete.cascadeLinks.disabledForModels =
null: java.util.List<java.lang.String>
Contains a list of document model names for which links must not be deleted. To disable
deletion for all models, use "*". If a model name is specified in this list, deletion of any links
belonging to that model will not be performed.
mgmtp.a12.dataservices.initialization.import.documents.multiDelete.limit = 50.: int
Hard limit for maximum amount of documents to be deleted in
com.mgmtp.a12.dataservices.document.operation.internal.MultiDeleteDocumentsOperation.
mgmtp.a12.dataservices.initialization.import.documents.persistTransientFields.enabled = false:
boolean
Switch for enabling/disabling persistence (consequently indexing) of transient fields
mgmtp.a12.dataservices.initialization.import.documents.validation.enabled = true: boolean
If true, documents are fully validated by default on save. Only documents of models listed in
#partialForModels and #skipForModels are handled differently.
mgmtp.a12.dataservices.initialization.import.documents.validation.partialForModels = null:
java.util.List<java.lang.String>
For documents of these models validate just fields which are set.
mgmtp.a12.dataservices.initialization.import.documents.validation.skipForModels = null:
java.util.List<java.lang.String>
Skip validation of these models on save.
CDD Related Configuration
mgmtp.a12.dataservices.cdd.export.charset = UTF-8: java.lang.String
Specifies the canonical name of the character set that is used to encode the content saved to the
storage. Allowed values depend on the JDK in use. Most common encodings (canonical names)
are
• ISO-8859-1: ISO-8859-1, Latin Alphabet No. 1
• UTF-8: Eight-bit Unicode (or UCS) Transformation Format
• US-ASCII: American Standard Code for Information Interchange
mgmtp.a12.dataservices.cdd.export.csv.delimiter = ;: java.lang.Character
The delimiter used in exported csv file
mgmtp.a12.dataservices.cdd.export.maxRowSize = 65536: int
Hard limit for max export row size for list cdd.
mgmtp.a12.dataservices.cdd.model.modificationAfterInitialization.enabled = false: boolean
Enables/disables CDM readonly after initialization.
35

-- 35 of 334 --

RPC Properties
mgmtp.a12.dataservices.jsonRpc.allowedOperations = A12_INTERNAL_OPERATIONS:
java.util.Set<java.lang.String>
Allows using specified RPC operations or group of operation. You can set the value to "*" to allow
all existing RPC operation. Note that in case "*" wouldn’t be the only string in the list, no special
meaning would be applied. Other pre-defined operation groups are:
• DOCUMENT_OPERATIONS: All document operations (excluding CDD handling, relationships and
attachments).
• CDD_OPERATIONS : All CDD operations (without potentially needed document and link
operations).
• LINK_OPERATIONS : All relationship (aka link) operations (without potentially needed document
operations).
• ATTACHMENT_OPERATIONS : All attachment and thumbnail operations (without potentially
needed document operations).
By default, the A12_INTERNAL_OPERATIONS group is enabled because these operations are
mandatory for projects that have a12-client as frontend application. Please make sure to enable
this group if you have your own configuration.
mgmtp.a12.dataservices.jsonRpc.enabled = false: boolean
Enables/disables JSON-RPC endpoint.
mgmtp.a12.dataservices.jsonRpc.maxMethodCallsPerRequest = 100: int
Limit for maximum number of method calls per single RPC request
mgmtp.a12.dataservices.jsonRpc.spel.enabled = false: boolean
Enables use of placeholder resolution in JSON-RPC requests.
Model & Document Initialization Import
During the Data Services initialization phase, it is possible to configure the server to import models.
The following properties point to the location on the filesystem from where the models should be
loaded as JSON during initialization.
The resources (rpc-requests or models) location resolution on the Data Services initialization uses
expression language which can use an absolute or relative path, as well as wildcards,…
mgmtp.a12.dataservices.initialization.import.models.enabled = true: boolean
Enables import of business models on system initialization.
mgmtp.a12.dataservices.initialization.import.models.path = : java.lang.String[]
Specifies the path where imported models are located.
Examples:
36

-- 36 of 334 --

• file:/path/to/folder/
• classpath:/jsonRpc/
Comments:
• Prefix file: or classpath: is mandatory here.
• For classpath prefix, leading and trailing slashes are optional.
• On Windows /path/to/folder represents the directory C:\path\to\folder.
• Wildcards are not supported here.
mgmtp.a12.dataservices.initialization.import.models.typesToClear = null:
java.util.List<java.lang.String>
Enables full import of models during initialization.
This property specifies which model types should be deleted. To delete all models, use "*". Only
model definitions are removed; the underlying data remains. Links and documents without a
model become inaccessible via the API. Models must be re-added to access data via API.
Otherwise, the data will be accessible only in the database. In case no model type is provided, no
deleting is done.
Related configurations: mgmtp.a12.dataservices.models.relationship.validation.enabled,
mgmtp.a12.dataservices.models.relationship.safe-delete.enabled
Other Initialization Properties
mgmtp.a12.dataservices.initialization.cleanUpRequestId.enabled = false: boolean
Enables clean up of table REQUEST_ID on system initialization.
mgmtp.a12.dataservices.initialization.migration.enabled = true: boolean
Enables migration of custom tasks (e.g. Document or Model migration) on system initialization.
mgmtp.a12.dataservices.initialization.preCompile.enabledForModels = *:
java.util.List<java.lang.String>
Allows whitelisting specific models for cache preloading while disabling all others. This controls
pre-compilation, validation code generation, and indexed field cache preloading. Setting the
value to "*" permits all existing models. Note: If "*" isn’t the only string in the list, no special
meaning will be applied.
mgmtp.a12.dataservices.initialization.scripts.jsonRpc.enabled = false: boolean
Enables the execution of JSON-RPC requests on server initialization.
mgmtp.a12.dataservices.initialization.scripts.jsonRpc.paths = empty list.:
java.util.List<java.lang.String>
Pattern indicating the resources as JSON-RPC requests to be executed on initialization. Supports
providing multiple paths.
Path examples:
37

-- 37 of 334 --

• file:/path/to/folder/*
• file:/path/to/folder/*.json
• file:/path/to/folder/singleRequest.json
• classpath:/jsonRpc/*
• classpath:/jsonRpc/*.json
• classpath:/jsonRpc/singleRequest.json
Comments:
• Prefix file: or classpath: is mandatory here.
• For classpath prefix, leading slashes are optional.
• It will be executed ordered by file name ASC.
• On Windows /path/to/folder represents the directory C:\path\to\folder.
• Double asterisks (**) are not supported here.
• The property is ignored if the
mgmtp.a12.dataservices.initialization.scripts.jsonRpc.enabled property is false.
mgmtp.a12.dataservices.models.list.hardLimit = 50: int
Hard limit for result size of the
com.mgmtp.a12.dataservices.model.operation.internal.ListModelsOperation. How many models
can a single user fetch.
mgmtp.a12.dataservices.models.metadata.document.path = /com/mgmtp/a12/rmc/metadata/document-
meta-data.json: java.lang.String
Path to the document metadata JSON file within the classpath resources folder. By default, the
file is loaded from the rmc conversion artifact. Use absolute paths starting with / for files in the
root of the resources folder.
mgmtp.a12.dataservices.models.relationship.safeDelete.enabled = true: boolean
If true, relationship models are checked for links when deleting. If links exist, the deletion is
aborted and an error is returned.
mgmtp.a12.dataservices.models.relationship.validation.enabled = true: boolean
If true, relationship models are fully validated when saving
Jobs Configurations
See the job scheduling section.
mgmtp.a12.dataservices.jobs.attachments.cleanUpDirtyAttachments.schedule = "0 0 1 * * ?":
java.lang.String
Cron expression to plan attachment cleanup job. See the Quartz Trigger tutorial.
38

-- 38 of 334 --

mgmtp.a12.dataservices.jobs.attachments.cleanUpStaleAttachments.schedule = "0 15 1 * * ?":
java.lang.String
Cron expression to plan attachment cleanup job. See the Quartz Trigger tutorial.
mgmtp.a12.dataservices.jobs.attachments.temporary.contextExpireHours = null:
java.util.Map<java.lang.String,java.lang.Integer>
A map that holds the time in hours after which a temporary attachments in specified context
will be deleted.
mgmtp.a12.dataservices.jobs.attachments.temporary.expireHours = 48: int
Time in hours after which a temporary attachment will be deleted
mgmtp.a12.dataservices.jobs.enabled = true: boolean
Enable All jobs. See Quartz documentation.
mgmtp.a12.dataservices.jobs.relationships.rankRecalculation.enabled = false: boolean
Enables/disables the rank reorder scheduler job.
mgmtp.a12.dataservices.jobs.relationships.rankRecalculation.rmsToReorder = null:
java.util.List<java.lang.String>
List of relationship model names whose document’s ranks should be reordered by the job.
mgmtp.a12.dataservices.jobs.relationships.rankRecalculation.schedule = "0 45 1 ? * SUN":
java.lang.String
Cron schedule to trigger recalculation of all assigned link ranks.
mgmtp.a12.dataservices.jobs.requests.cleanupRequestId.expireHours = 720 (i.e. one month): int
The amount of time in hours after which an idempotence id (entry in table request_id) will be
deleted.
mgmtp.a12.dataservices.jobs.requests.cleanupRequestId.schedule = "0 30 1 * * ?":
java.lang.String
Cron expression to plan request_id cleanup job. See the Quartz Trigger tutorial.
Model Overwriting Configuration
Via this configuration it is possible to choose if models should be overwritten on initialization:
mgmtp.a12.dataservices.initialization.import.models.overwrite.enabled.
mgmtp.a12.dataservices.initialization.import.models.overwrite.documentModels.enabled = true:
boolean
Enables overwriting of document models on application initialization. Applies on importing
business models by mgmtp.a12.dataservices.initialization.import.models.path.
mgmtp.a12.dataservices.initialization.import.models.overwrite.enabled = true: boolean
Configures default for model overwriting on application initialization. Applies on importing
business models by mgmtp.a12.dataservices.initialization.import.models.path.
39

-- 39 of 334 --

mgmtp.a12.dataservices.initialization.import.models.overwrite.models = null:
java.util.Map<java.lang.String,java.lang.Boolean>
Enables model overwriting by particular type of model on application initialization. When not
provided, value mgmtp.a12.dataservices.initialization.import.models.overwrite.enabled will be
used. For example: mgmtp.a12.dataservices.initialization.import.models.overwrite.models.my-
custom-model-type=false will deny overwriting models of type "my-custom-model-type" on
application initialization. Applies on importing user models by
mgmtp.a12.dataservices.initialization.import.models.path.
Attachment Properties
mgmtp.a12.dataservices.attachments.cleanup.retry.delay = "5 min": java.lang.String
Delay before retry after recoverable error. See
com.mgmtp.a12.dataservices.common.quantity.internal.QuantityParsers#parseTimeQuantity(Str
ing) for possible values.
mgmtp.a12.dataservices.attachments.cleanup.retry.max = 5: int
Maximum count of retries for recoverable errors.
mgmtp.a12.dataservices.attachments.enabled = true: boolean
Switch for enabling/disabling attachment handling.
mgmtp.a12.dataservices.attachments.ext.contentstore.embedded.enabled = true: boolean
Switch for using content store embedded mode
mgmtp.a12.dataservices.attachments.ext.contentstore.ticketTimeout = 300: long
Ticket expiration time in seconds.
mgmtp.a12.dataservices.attachments.ext.fs.location = ${user.home}/a12/dataservices/attachments:
java.io.File
Attachment location on file system (from version V1.5 on). Prefix file: is always mandatory for
value.
Example: file:/var/lib/a12/dataservices/attachments
mgmtp.a12.dataservices.attachments.mimeType.inMemoryTemp.enabled = false: boolean
If enabled, enforces probing mime type to use in-memory JimFs as temporary storage during
detection.
mgmtp.a12.dataservices.attachments.mimeType.probeMimeType.enabled = false: boolean
Enable/disable Data Services probes mime type by itself or delegate to Content Store.
mgmtp.a12.dataservices.attachments.restEndpoint.enabled = false: boolean
Switch for enabling/disabling attachment REST endpoint.
mgmtp.a12.dataservices.attachments.thumbnail.generation.imageDiskCache.enabled = false: boolean
Sets a flag indicating whether ImageIO should use disk-based cache when creating
ImageInputStreams and ImageOutputStreams. Setting this property to false disallows the use of
40

-- 40 of 334 --

disk for future streams, which may be advantageous when working with small images, as the
overhead of creating and destroying files is removed. By default, this property is false.
mgmtp.a12.dataservices.attachments.thumbnail.generation.thumbnailator.conserveMemoryWorkaround.
enabled = false: boolean
This property is disabled by default, if enabled, the workaround solution provided by
Thumbnailator will be applied by setting system argument
-Dthumbnailator.conserveMemoryWorkaround=true. Both height and width of image have
dimensions larger than 1800 pixels Thumbnailator will invoke code to load a smaller image to
memory from the source image when creating a thumbnail. This property is only applied if
mgmtp.a12.dataservices.attachments.thumbnail.optimization.performance.enabled=false
mgmtp.a12.dataservices.attachments.thumbnail.optimization.baseUrl = : java.lang.String
Base url for thumbnail for optimization
mgmtp.a12.dataservices.attachments.thumbnail.optimization.performance.enabled = false: boolean
Try to use java.awt.Graphics2D for generating thumbnail to increase performance. If enabled
Graphics2D will be used to generate thumbnail. By default, it’s disabled, Thumbnailator will be
used.
mgmtp.a12.dataservices.attachments.thumbnail.optimization.url.enabled = false: boolean
Thumbnail url is auto-computed on Data Services side. If enabling this config, we must config
base thumbnail url: mgmtp.a12.dataservices.attachments.thumbnail.optimization.baseUrl.
mgmtp.a12.dataservices.attachments.thumbnail.preview.enabled = false: boolean
Switch for load thumbnail functionality
mgmtp.a12.dataservices.attachments.thumbnail.sizeBig = 64: int
Size in pixels for big thumbnail.
mgmtp.a12.dataservices.attachments.thumbnail.sizeSmall = 32: int
Size in pixels for small thumbnail.
mgmtp.a12.dataservices.attachments.type.publicType.models = empty list:
java.util.List<java.lang.String>
List of Document Models which attachments will be public.
Data services uses Content store for storage of attachments. Please also read Content Store
Configuration for more information.
Java Client Properties
mgmtp.a12.dataservices.client.configuration.baseUrl = null: String
Base URL of the server.
41

-- 41 of 334 --

Authorization Properties
mgmtp.a12.dataservices.authorization.backendJob.principal.username = superUser: java.lang.String
Configuration for defining backend job username. This user is used in the following places:
• initialization of the application.
• link rank defragmentation.
• kernel cache preloader.
This implies that the user must have at least permissions to modify documents and models.
Additionally, it must have permission to all actions executed in the events handlers provided as
customization and also to all actions executed from RPC initializer if provided.
So, the recommended set of permissions is at least:
• Model Read
• Model Create
• Model Update
• Query
• Document Create
• Document Update
• Document Delete
mgmtp.a12.dataservices.authorization.roleBased.enabled = true: boolean
Configuration for role based authorization. If value is false, DS will disable all model based
authorization.
Actuator Properties
 All actuator endpoints except /actuator/health/** require the ACCESS_ACTUATOR
access right. See Security for details.
management.endpoints.access.default = none
By default, access to all endpoints (except for shutdown and heapdump) is unrestricted, so you can
configure the permitted access to an endpoint with the management.endpoint.<id>.access
property (example: management.endpoint.shutdown.access=unrestricted).
When none, all endpoints are restricted, and you may use the individual access properties to opt
back in (example: management.endpoint.env.access=read-only). See spring documentation for
the details.
Note that the only exposed endpoints by default are /health and /info although others are
enabled.
management.endpoints.web.exposure.include = health,info
Sets the list of endpoints to be exposed via web. If you want to expose all, set the value to *. See
42

-- 42 of 334 --

spring documentation for the details. Note that 'exposed endpoint' does not mean 'enabled
endpoint', so make sure to expose and enable the ones you want.
management.health.defaults.enabled = false
Disables Spring’s default health indicators. If you need to check their health during initialization,
set it to true (globally via this property, or individually). See spring documentation for the
details.
management.server.port
Custom port for actuator if different from the app.
This is the recommended approach for cluster deployments where monitoring agents need
actuator access — expose actuator on a separate internal port that is not reachable from outside
the cluster.
management.endpoints.web.basePath
Custom actuator name if it needs to be changed.
Change context name like http://localhost/actuator → http://localhost/newName.
management.endpoint.health.showDetails
Show details of the health endpoint.
Use the value always or when-authorized to check status for the server initialization finished, for
example.
Logger Anonymizer
mgmtp.a12.dataservices.logging.anonymization.enabled = true: boolean
Control whether to render anonymous sensitive data for logging.
When logging, we protect sensitive data by replacing it by asterisks by default. You can disable this
behavior by configuration property mgmtp.a12.dataservices.logging.anonymization.enabled=false,
or you can toggle it at runtime by JMX managed bean
com.mgmtp.a12.dataservices.utils.RuntimeSwitchingAnonymizer. To enable JMX, start server with the
following properties:
Example JVM options to enable JMX
com.sun.management.jmxremote
com.sun.management.jmxremote.port=9010
com.sun.management.jmxremote.rmi.port=9010
com.sun.management.jmxremote.local.only=false
com.sun.management.jmxremote.authenticate=false
com.sun.management.jmxremote.ssl=false
and then connect to it by jconsole. Under tab "MBeans" you can view all available managed beans
and if allowed, you can modify its properties.
43

-- 43 of 334 --

Other Properties
mgmtp.a12.dataservices.documents.validation.language = en: java.lang.String
Default validation locale when there is no validation locale provided in request.
mgmtp.a12.dataservices.initialization.import.documents.validation.language = en:
java.lang.String
Default validation locale when there is no validation locale provided in request.
mgmtp.a12.dataservices.server.contextPath = /api: java.lang.String
Mappings in Data Services have the following structure:
SPRING_CONTEXT_PATH/DATA_SERVICES_CONTEXT_PATH/… This property should be used to set
DATA_SERVICES_CONTEXT_PATH (if you want to set SPRING_CONTEXT_PATH use
server.servlet.contextPath instead). Its purpose is to give an ability to differentiate with
DATA_SERVICES_CONTEXT_PATH by introducing your own context path variable. NOTES: 1.
Don’t put leading '/' if SPRING_CONTEXT_PATH has trailing '/'. It will result in '//' prefix in the
mappings. 2. There is a configuration called mgmtp.a12.uaa.authentication.contextPath. It should
be equal to this property for the application to function properly.
mgmtp.a12.dataservices.server.exceptionMapping.shouldAddExceptionToHeader = false: boolean
Defines whether the exception should be added to the header of responses in the exception
mappers.
Configuration Profiles
We maintain a collection of pre-configured profiles that bundle commonly used properties with the
intention to simplify the setup of DS by streamlining the configuration process.
There are three primary mechanisms for configuring profiles in the Spring Framework:
spring.profiles.active, spring.profiles.include, and spring.config.import.
The recommended and straightforward method is leveraging spring.profiles.active. This property
takes precedence over any underlying activated profiles, ensuring that only the explicitly defined
list remains active. By setting spring.profiles.active, you precisely dictate which profiles should be
in effect.
Contrastingly, spring.profiles.include appends additional profiles to the set of active ones rather
than replacing them. This allows for a more cumulative approach, combining configurations from
multiple profiles.
The spring.config.import property serves the purpose of importing externalized configurations
through a specified location pattern. However, it’s essential to note that files imported this way are
not treated as profiles. Consequently, they cannot be employed for profile-specific features such as
the @Profile annotation, nor influenced by the spring.profiles.active setting. In essence,
spring.config.import operates independently of the profile-based configuration mechanisms in
Spring, focusing solely on incorporating externalized configuration from designated locations.
For comprehensive guidance on their application and utilization, please consult the spring profile
44

-- 44 of 334 --

documentation.
• Actuators • H2 database • Embedded Postgres Database • Postgres database • Activate HTTP/1
Support Only for Server Application • Disable cache • Disable Liquibase • UAA • Actuators •
Cluster • Embedded Content Store • Embedded Postgres Database • External Postgres Database •
Activate HTTP/1 Support Only for Server Application • Enable Initialization Scripts • Disable
Attachments • Disable Caching • Disable Jobs • Disable Liquibase • PostgreSQL Read Replica •
Enable RPC • Active Content Store Standalone Mode Integration • Set Up UAA
Actuators
Enable Spring Boot Actuators for all endpoints.
See actuator properties for additional information.

This profile will apply to both data services and content stores. So use just in case
of the standalone mode.
If you don’t use LDAP, but you enable all actuators by wildcard, you can get this
error:
{
"ldap": {
"status": "DOWN",
"details": {
"error": "org.springframework.ldap.CommunicationException:
localhost:389; nested exception is javax.naming.CommunicationException:
localhost:389 [Root exception is java.net.ConnectException: Connection
refused: connect]"
}
}
}
You can avoid this error by adding management.health.ldap.enabled=false property.
Profile Name
contentstore-actuators
Profile Content
management.endpoints.access.default=read-only
management.endpoints.web.exposure.include=*
management.endpoint.health.showDetails=always
management.health.defaults.enabled=true
management.health.ldap.enabled=false
45

-- 45 of 334 --

H2 database

This profile is deprecated and should not be used in production. DS supports only
PostgreSQL. The content store uses a separate data source and has no
dependencies on any PostgreSQL-specific features, but DS has no tests for any
other database.
Set up the application to use embedded H2 in-memory database.
To specify different datasource use these additional properties:
spring.datasources.contentstore.url=jdbc:h2:LOCATION:DB;
spring.datasources.contentstore.username=USERNAME
spring.datasources.contentstore.password=PASSWORD
Additionally, you can change timeout by:
spring.datasources.contentstore.hikari.connectionTimeout=
Profile Name
contentstore-embedded_h2
Profile Content
spring.datasources.contentstore.url=jdbc:h2:mem:contentstore;
spring.datasources.contentstore.driver-class-name=org.h2.Driver
spring.datasources.contentstore.name=contentstore
spring.datasources.contentstore.username=sa
spring.datasources.contentstore.password=
spring.datasources.contentstore.jpa.database=H2
spring.h2.console.enabled=true
spring.h2.console.path=/console/
Embedded Postgres Database
Set up the application to use embedded file-based Postgres database. Default superuser is postgres
and db name is postgres. If spring.datasources.dataservices.embedded-postgres.path is not
provided, temp folder will be used to persist data. Temp folder will depend on OS:
• Linux and MacOS: /tmp/embedded-pg
• Windows: ${user.home}\AppData\Local\Temp
Set spring.datasources.contentstore.embedded-postgres.override-working-directory to a directory
to cache the extracted PostgreSQL binaries (e.g. initdb, postgres) across application runs instead of
the default temporary location.
46

-- 46 of 334 --

 This embedded postgres profile should be used only for development or testing
purpose only. Persistent data might be lost after restarting server.
Additional Possible Configuration:
spring.datasources.contentstore.embedded-postgres.path=file:./cs-embedded-postgres
spring.datasources.contentstore.embedded-postgres.port=5439
spring.datasources.contentstore.embedded-postgres.connect-config.autosave=always
spring.datasources.contentstore.embedded-postgres.locale-c-type=en_US.UTF-8
spring.datasources.contentstore.embedded-postgres.override-working-
directory=file:./postgres-bin
For more connect-config configuration, please refer to org.postgresql.PGProperty or the Postgres
official documentation.
For more postgres-config configuration, please refer to the Postgres documentation on pg_ctl.
To ensure a graceful shutdown of the embedded Postgres database, please be aware of the
following considerations.
When you use the IDE’s (e.g., IntelliJ, Visual Studio Code) Stop button (Red Square), the system
attempts a graceful shutdown. This process allows EmbeddedPostgres to rely on a shutdown
hook for proper cleanup, resource release, and database stopping. However, in some
environments, particularly on Windows and macOS, the shutdown hook may not execute as
expected, preventing the embedded Postgres database from shutting down properly. Even if
you delete the Postgres data folder, the Postgres process might still be running in the
background, which can lead to residual old data. Consequently, restarting the application may
lead to errors indicating the Postgres instance is already running, or you may face port binding
issues.
If this issue occurs, you must manually stop the Postgres process and delete the temporary
postgres folder. Methods include:
• Using Task Manager (Windows) or Activity Monitor (macOS).
• Using the command line to kill the process.
To ensure a reliable graceful shutdown, consider the following alternative:
Use the following properties to enable the Actuator shutdown endpoint:
management.endpoint.shutdown.access=unrestricted
management.endpoints.web.exposure.include=shutdown
Then, send a POST request to the shutdown endpoint to properly trigger the application
exit procedure:
47

-- 47 of 334 --

curl --location --request POST '{YOUR_HOST_URL}/actuator/shutdown'
Your application will then shut down properly. Note that this method should only be used
in your local environment. In production or other environments where the application is
run as a standard Java process, this issue typically does not occur, and the application
will shut down gracefully. Remember, embedded Postgres is only for development
purposes.
Note: If you cannot gracefully shut down the embedded Postgres database, you must
manually kill the Postgres process to avoid data corruption, as the graceful shutdown
method fails in this scenario.
Profile Name
contentstore-embedded_postgres
Profile Content
spring.datasources.contentstore.embedded-postgres.enabled=true
spring.datasources.contentstore.embedded-postgres.port=5435
spring.datasources.contentstore.jpa.database=postgresql
Postgres database
Set up the application to use Postgres database.
List of most used optional configurations you may need:
To specify datasource use these additional properties:
spring.datasources.contentstore.url=jdbc:postgresql://HOST:PORT/DB
spring.datasources.contentstore.username=USERNAME
spring.datasources.contentstore.password=PASSWORD
Additionally, you can change timeout by:
spring.datasources.contentstore.hikari.connectionTimeout=
Profile Name
contentstore-external_postgres
48

-- 48 of 334 --

Profile Content
spring.datasources.contentstore.jpa.database=postgresql
spring.datasources.contentstore.driver-class-name=org.postgresql.Driver
Activate HTTP/1 Support Only for Server Application
With this profile enabled, server application will support HTTP/1 protocol only.
Profile Name
contentstore-http1_only
Profile Content
server.http2.enabled=false
Disable cache
Disable caching completely.
 This profile is discouraged in production environment, because it could cause big
performance drop. Use only if you know what you do.
This profile will apply to both data services and content stores. So use just in case of the standalone
mode.
See cache properties for additional information.
Profile Name
contentstore-no_cache
Profile Content
spring.cache.type=none
Disable Liquibase
Disables initial database creation/migration. This is useful especially for cluster-safe setup of multi
instance servers.
Profile Name
contentstore-no_liquibase
49

-- 49 of 334 --

Profile Content
spring.datasources.contentstore.liquibase.enabled=false
UAA
Setup default UAA configuration.
Profile Name
contentstore-uaa
Profile Content
mgmtp.a12.uaa.authentication.cors.enable=true
mgmtp.a12.uaa.authentication.context-path=/cs/api
Actuators
Enable Spring Boot Actuators for all endpoints.
See the Actuator Properties for additional information.

If you don’t use LDAP, but you enable all actuators by wildcard, you can get this
error:
{
"ldap": {
"status": "DOWN",
"details": {
"error": "org.springframework.ldap.CommunicationException:
localhost:389; nested exception is javax.naming.CommunicationException:
localhost:389 [Root exception is java.net.ConnectException: Connection
refused: connect]"
}
}
}
You can avoid this error by adding management.health.ldap.enabled=false property.
Profile Name
dataservices-actuators
Profile Content
management.endpoints.access.default=read-only
management.endpoints.web.exposure.include=*
50

-- 50 of 334 --

management.endpoint.health.showDetails=always
management.health.defaults.enabled=true
management.health.ldap.enabled=false
Cluster
Configure server to bypass initialization steps. Includes disabling of initialization of database
schema, data import and index manipulation. It is intended to be used in clustered setup with
multiple replicas where you don’t want initializations steps to be executed by each replica. See
cluster-safe configuration for additional information.
Profile Name
dataservices-cluster
Profile Content
spring.datasources.dataservices.liquibase.enabled=false
mgmtp.a12.dataservices.initialization.import.models.enabled=false
mgmtp.a12.dataservices.initialization.scripts.jsonRpc.enabled=false
mgmtp.a12.dataservices.initialization.migration.enabled=false
Embedded Content Store
Enable/disable embedded Content Store server.
Profile Name
dataservices-embedded_contentstore
Profile Content
mgmtp.a12.dataservices.attachments.ext.contentstore.embedded.enabled=true
mgmtp.a12.dataservices.contentstore.server.api.enabled=false
mgmtp.a12.dataservices.contentstore.server.contextPath=/cs
mgmtp.a12.dataservices.contentstore.base-url=http://localhost:${server.port:8080}
mgmtp.a12.dataservices.attachments.thumbnail.optimization.baseUrl=${mgmtp.a12.dataserv
ices.contentstore.base-url}${mgmtp.a12.dataservices.contentstore.server.contextPath}
Embedded Postgres Database
Set up the application to use an embedded file-based Postgres database. Default superuser is
postgres and db name is postgres. If spring.datasources.dataservices.embedded-postgres.path is not
provided, a temp folder will be used to persist data. Temp folder will depend on OS:
• Linux and MacOS: /tmp/embedded-pg
• Windows: ${user.home}\AppData\Local\Temp
51

-- 51 of 334 --

Set spring.datasources.dataservices.embedded-postgres.override-working-directory to a directory
to cache the extracted PostgreSQL binaries (e.g. initdb, postgres) across application runs instead of
the default temporary location.
 This embedded Postgres profile should be used only for development or testing
purposes. Persistent data might be lost after restarting the server.
Additional Possible Configuration:
spring.datasources.dataservices.embedded-postgres.path=file:./ds-embedded-postgres
spring.datasources.dataservices.embedded-postgres.port=5434
spring.datasources.dataservices.embedded-postgres.connect-config.autosave=always
spring.datasources.dataservices.embedded-postgres.locale-c-type=en_US.UTF-8
spring.datasources.dataservices.embedded-postgres.override-working-
directory=file:./postgres-bin
For more connect-config configuration, please refer to org.postgresql.PGProperty or the Postgres
official documentation.
For more postgres-config configuration, please refer to the Postgres documentation on pg_ctl.
To ensure a graceful shutdown of the embedded Postgres database, please be aware of the
following considerations.
When you use the IDE’s (e.g., IntelliJ, Visual Studio Code) Stop button (Red Square), the system
attempts a graceful shutdown. This process allows EmbeddedPostgres to rely on a shutdown
hook for proper cleanup, resource release, and database stopping. However, in some
environments, particularly on Windows and macOS, the shutdown hook may not execute as
expected, preventing the embedded Postgres database from shutting down properly. Even if
you delete the Postgres data folder, the Postgres process might still be running in the
background, which can lead to residual old data. Consequently, restarting the application may
lead to errors indicating the Postgres instance is already running, or you may face port binding
issues.
If this issue occurs, you must manually stop the Postgres process and delete the temporary
postgres folder. Methods include:
• Using Task Manager (Windows) or Activity Monitor (macOS).
• Using the command line to kill the process.
To ensure a reliable graceful shutdown, consider the following alternative:
Use the following properties to enable the Actuator shutdown endpoint:
management.endpoint.shutdown.access=unrestricted
management.endpoints.web.exposure.include=shutdown
52

-- 52 of 334 --

Then, send a POST request to the shutdown endpoint to properly trigger the application
exit procedure:
curl --location --request POST '{YOUR_HOST_URL}/actuator/shutdown'
Your application will then shut down properly. Note that this method should only be used
in your local environment. In production or other environments where the application is
run as a standard Java process, this issue typically does not occur, and the application
will shut down gracefully. Remember, embedded Postgres is only for development
purposes.
Note: If you cannot gracefully shut down the embedded Postgres database, you must
manually kill the Postgres process to avoid data corruption, as the graceful shutdown
method fails in this scenario.
Profile Name
dataservices-embedded_postgres
Profile Content
spring.datasources.dataservices.embedded-postgres.enabled=true
spring.datasources.dataservices.jpa.database=postgresql
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcj
obstore.PostgreSQLDelegate
External Postgres Database
Set up the application to use Postgres database.
List of most used optional configurations you may need:
To specify datasource use these additional properties:
spring.datasources.dataservices.url=jdbc:postgresql://HOST:PORT/DB
spring.datasources.dataservices.username=USERNAME
spring.datasources.dataservices.password=PASSWORD
Additionally, you can change timeout by:
spring.datasources.dataservices.hikari.connectionTimeout=
53

-- 53 of 334 --

Profile Name
dataservices-external_postgres
Profile Content
spring.datasources.dataservices.jpa.database=postgresql
spring.datasources.dataservices.driver-class-name=org.postgresql.Driver
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcj
obstore.PostgreSQLDelegate
Activate HTTP/1 Support Only for Server Application
With this profile enabled, server application will support HTTP1 protocol only.
Profile Name
dataservices-http1_only
Profile Content
server.http2.enabled=false
Enable Initialization Scripts
Enable execution of server initialization scripts, like RPC requests and import of models.
See import initialization properties and other initialization properties for additional information.
Profile Name
dataservices-initscripts
Profile Content
mgmtp.a12.dataservices.initialization.scripts.jsonRpc.enabled=true
Disable Attachments
Disables Data Services attachments including Content Store.
See attachments properties for advanced configuration.
Profile Name
dataservices-no_attachments
54

-- 54 of 334 --

Profile Content
mgmtp.a12.dataservices.attachments.enabled=false
Disable Caching
Disable caching completely.
 This profile is discouraged in production environment, because it could cause big
performance drop. Use only if you know what you do.
In case of embedded mode, this profile will apply to both data services and content stores.
See cache properties for additional information.
Profile Name
dataservices-no_cache
Profile Content
spring.cache.type=none
spring.datasources.dataservices.jpa.properties.hibernate.cache.use_second_level_cache=
false
spring.datasources.dataservices.jpa.properties.hibernate.cache.use_query_cache=false
Disable Jobs
Disables Data Services jobs but not the scheduler itself. It means that you can still add your
custom jobs.
See jobs properties for advanced configuration.
Profile Name
dataservices-no_jobs
Profile Content
mgmtp.a12.dataservices.jobs.enabled=false
Disable Liquibase
Disables initial database creation/migration. This is useful especially for cluster-safe setup of multi
instance servers.
55

-- 55 of 334 --

Profile Name
dataservices-no_liquibase
Profile Content
spring.datasources.dataservices.liquibase.enabled=false
PostgreSQL Read Replica
Route read-only database transactions to a PostgreSQL read replica datasource.
Routing is activated automatically when spring.datasources.dataservices-read-replica.url is
present. No other changes are required. When the property is absent, all traffic goes to the primary
datasource and the routing infrastructure is not started.
This profile enforces read-only mode at the JDBC driver level for every connection in the replica
pool (spring.datasources.dataservices-read-replica.hikari.read-only=true), which prevents
accidental writes through the replica connection pool.
Provide the connection details for your read replica in addition to this profile:
spring.datasources.dataservices-read-replica.url=jdbc:postgresql://replica-
host:5432/dataservices
spring.datasources.dataservices-read-replica.username=ds_user
spring.datasources.dataservices-read-replica.password=secret
See Read Replica Configuration (Optional) for all available properties and Read Replica Routing for
routing behavior and consistency considerations.
Profile Name
dataservices-postgres_read_replica
Profile Content
spring.datasources.dataservices-read-replica.hikari.read-only=true
Enable RPC
Enable all available RPC operations and usage of SpEL expressions in them. For information about
available operations see this link.
You may want to disable some of RPC operations or enable custom ones. For this case override
following property with list of desired operations:
56

-- 56 of 334 --

mgmtp.a12.dataservices.json-rpc.allowed-operations=
In case you would want just prohibit SpEL expressions from RPC operations use following property:
mgmtp.a12.dataservices.json-rpc.spel.enabled=false
See RPC Properties for additional information.
Profile Name
dataservices-rpc
Profile Content
mgmtp.a12.dataservices.jsonRpc.enabled=true
mgmtp.a12.dataservices.json-rpc.spel.enabled=true
mgmtp.a12.dataservices.json-rpc.allowed-operations=*
Active Content Store Standalone Mode Integration
With this profile enabled, Dataservices attachment repository will choose standalone-mode
implementation, including all properties for supporting communication between Dataservices and
standalone Content Store service.
Please note that, this profile also includes required UAA properties to initialize Content Store
Client
Profile Name
dataservices-standalone_contentstore
Profile Content
mgmtp.a12.dataservices.attachments.ext.contentstore.embedded.enabled=false
mgmtp.a12.dataservices.contentstore.server.api.enabled=true
mgmtp.a12.dataservices.contentstore.client.configuration.remote-
url=http://${config.contentstore.host:localhost}:${config.contentstore.port:9090}/cs
mgmtp.a12.dataservices.attachments.thumbnail.optimization.baseUrl=${mgmtp.a12.dataserv
ices.contentstore.client.configuration.remote-url}
Set Up UAA
Set up default UAA configuration.
57

-- 57 of 334 --

Profile Name
dataservices-uaa
Profile Content
mgmtp.a12.uaa.authorization.authorizationDefinition=classpath:/uaa/authorizationDefini
tion.json
mgmtp.a12.uaa.authentication.backend.enabled=true
mgmtp.a12.uaa.authentication.backend.grant-super-user-privileges.enabled=true
mgmtp.a12.uaa.authentication.secured-contexts=/actuator/**
mgmtp.a12.uaa.authentication.unsecured.urls=/api/attachment/thumbnail/**,/cs/download/
**,/api/monitored-properties,/actuator/health/**
JSON-RPC Endpoint
• Operation Overview
• JSON-RPC 2.0 Methods
JSON-RPC Endpoint and Operations
With the JSON-RPC endpoint you can send multiple operations to the server that will be executed
sequentially within a single database transaction. This provides all-or-nothing semantics: if any
operation fails, all changes from the entire request are rolled back. For detailed information on
transaction isolation, concurrency handling, and index consistency, see Transaction Management
and Concurrency.
Idempotency
JSON-RPC operations are executed in non-idempotent mode by default because otherwise it may
have impact on performance. By providing the HTTP header Request-Id (see Header of the Request)
the endpoint is instructed to execute every request with the same Request-Id only once (which is
sort of idempotency) but the return code is different for the executing request (200 if success) and
the rejected ones (409 Conflict). The cleanup of Request-Id entries that remained due to server
shut-down can be enforced by the server initialization configuration (see Other Initialization
Properties).
Request-Id Cleanup Job
Processed Request-Id values are persisted in the REQUEST_ID table to prevent duplicate execution.
Over time, old entries must be removed to prevent unbounded table growth. Data Services provides
a Quartz scheduler job that deletes rows from the REQUEST_ID table once they have exceeded their
configured retention period.
The cleanup job is controlled by the following configuration properties:
58

-- 58 of 334 --

Property Default Description
mgmtp.a12.dataservices.jobs.en
abled
true Master switch for all Data Services scheduler
jobs. Set to false to disable all cleanup jobs.
mgmtp.a12.dataservices.jobs.re
quests.cleanupRequestId.schedu
le
0 0 1 * * ? Cron expression for the cleanup job. The default
runs once daily at 01:00 AM. See the Quartz
Trigger tutorial for syntax reference.
mgmtp.a12.dataservices.jobs.re
quests.cleanupRequestId.expire
Hours
720 (30 days) Number of hours after which a Request-Id entry
is eligible for deletion.
To restore the previous 5-minute cleanup frequency, set the schedule explicitly:
mgmtp.a12.dataservices.jobs.requests.cleanupRequestId.schedule=0 */5 * * * ?
To disable the cleanup job entirely (for example, when idempotency is not used), disable all jobs:
mgmtp.a12.dataservices.jobs.enabled=false
Header of the Request
The JSON-RPC endpoint reads non-mandatory Request-Id HTTP request header which should
contain unique request id. It is recommended to use UUID but DS does not enforce it. If the Request-
Id is present the request will be executed in (quasi) idempotent mode.
Body of the Request
The body of the request for the JSON-RPC endpoint follows the specification of the JSON-RPC 2.0.
Please check the JSON-RPC specification.
• request: Array with elements of type:
◦ jsonrpc: The version of the JSON-RPC implementation. If present, must be set to "2.0"
◦ id (required): A string that is a unique identification in the operation.
◦ method (required): Operation type identification.
◦ params (required): Type, dependent on operation.
The substructure of the params attribute is completely defined by its operation.
The structure of parameter is defined by the specific operation type.
Response of the JSON-RPC Request
The JSON-RPC endpoint returns responses according to the JSON-RPC 2.0 specification. For full
details, refer to the JSON-RPC specification.
59

-- 59 of 334 --

Operations may return a result, but for commands that do not produce data, the result will be null.
Common JSON Types
Common JSON Types are JSON structures that can be found in several core operations. These
structures provide data which is commonly needed in the operations.
Simple Types
ModelRef and DocumentReference are strings.
LinkDescriptor type describes the link but not in the unique way.
• relationshipModel : ModelRef
• entities : Array (of size 2) of structure
◦ role : String
◦ docRef : DocumentReference
• linkDocumentDocRef: The document reference of the link document, if any.
• predecessorLinkRef: A string that represents the link that should be the predecessor of this link.
• position: The link position (TOP or BOTTOM) defines that the link will be added at the top or at the
bottom of the list of links. If predecessorLinkRef is passed, position is ignored. If neither
predecessorLinkRef nor position are passed, the position will be considered to be TOP by default.
Example of LinkDescriptor
{
"relationshipModel": "ProductCampaign",
"entities": [
{
"role": "Product",
"docRef": "Product/10"
},
{
"role": "Campaign",
"docRef": "Campaign/7"
}
],
"linkDocumentDocRef": "BusinessPartner/23",
"predecessorLinkRef": "linkId",
"position": "BOTTOM"
}
RelationshipLinkSpec type is used to reference a link.
• linkDescriptor: LinkDescriptor type.
• id: A string which is a unique identification of the link.
60

-- 60 of 334 --

• sourceRank: A string containing the server-assigned rank of the created link from the perspective
of the source-role entity (e.g. "a", "b", "aa"). Absent or null when the relationship is not ordered
or the rank has not been set.
• targetRank: A string containing the server-assigned rank of the created link from the perspective
of the target-role entity (e.g. "a", "b", "aa"). Absent or null when the relationship is not ordered
or the rank has not been set.
Properties sourceRank and targetRank are only in responses because they can be used in sme/import.
They are not accepted in ADD_LINK or other rpc requests.
Operation
An Operation is any JAVA class annotated with @RemoteOperation that has a public method named
rpc.
The @RemoteOperation annotation defines the mandatory attribute name that is a unique string, e.g.
ADD_LINK, referencing the Operation to be called.
All operations in a JSON-RPC request are executed in the same database transaction. Data Services
does not support alternative transaction handling for JSON-RPC requests. For detailed information
on transaction isolation, concurrency handling, and rollback behavior, see Transaction
Management and Concurrency.
Versioning
You can annotate a method in an Operation by @JsonRpcMethod where the value attribute is a
numeric value of the version of the method. Then you can call this version of the Operation by
appending :VERSION_NUMBER. See following example:
Example of default and versioned method
@RemoteOperation(name = "EXAMPLE") public class ExampleOperation {
public String rpc() {
}
@JsonRpcMethod("2") public String rpcV2() {
}
}
 The @RemoteOperation annotation does not contain the @Component annotation. So,
you should either add @Component to your implementation or initialize it as a Java
@Bean.
Example of calling default and versioned method
[
{
"jsonrpc": "2.0",
61

-- 61 of 334 --

"id": "defaultVersion",
"method": "EXAMPLE"
},
{
"jsonrpc": "2.0",
"id": "version2",
"method": "EXAMPLE:2"
}
]
In our example, calling the Operation with method name EXAMPLE will imply default behavior which
is calling method named rpc with matching parameters.
On the other side, calling method named EXAMPLE:2 will lead to lookup for method annotated by
@JsonRpcMethod("2") where 2 is the version.
Operation Execution
Operation execution is driven by the JsonRpcOperationDispatcher class which is responsible for the
complete execution process.
Accepting an Execution Request
The request for executing one or more Operations is represented by a JsonRpc2Message object. This
is an entry point object which holds the complete request.
The Operations are executed in the order they were received. If one Operation fails, the whole
transaction will fail. The request will also fail if one of the Operations called is not present in the list
of allowed Operations or if it is not defined.
Example of multiple Operations that should follow the ordering of Operations
[
{
"jsonrpc": "2.0",
"id": "addAirJordanToBreakASweatCampaign",
"method": "ADD_LINK",
"params": {...}
},
{
"jsonrpc": "2.0",
"id": "listLinks",
"method": "QUERY",
"params": {...}
},
{
"jsonrpc": "2.0",
"id": "addTonerToPrintCampaign",
"method": "ADD_LINK",
"params": {...}
62

-- 62 of 334 --

}
]
Execution
The input parameters of an Operation are defined in the section params. These parameters are
provided to the bean that is looked up based on the identifier in the method parameter.
Input parameters are deserialized into input types, and the Operation is executed afterwards.
When all Operations have been executed, a map of Operation ids and results is returned. There is
no need to serialize result objects because they will be serialized on higher level into a respective
format.
 An input parameter need to be de-serializable from JSON, whereas results need to
be serializable into JSON. Otherwise, it would not be possible to read input
parameters from JSON and create response as JSON in a generic way.
Error Handling
When an exception is thrown during the execution of an Operation, an error object is created with
all necessary information, the subsequent Operations are skipped, and the whole transaction is
rollbacked.
There are 2 different error types:
Known Error
Represented by RpcException.
This exception contains the data structure OperationError which contains the Operation id and uses
it in the error object. It is recommended to use RpcException for all custom Operations because the
client already knows the error structure.
You can use utility class RpcExceptionSupport which simplifies the exception creation.
Unknown Error
Represented by a checked or unchecked exception, which is unknown to the system.
Since this does not provide any detail about the problem, it is converted to a generic error object.
Transaction Handling
Before processing the JSON-RPC request, a main database transaction is started. All operations in
the batch are executed within this single transaction.
If an operation doesn’t define any transactional behavior, it will simply join the main transaction.
If you need to start a nested transaction, you can annotate the operation’s execute method with
@Transactional annotation and the respective propagation flag.
63

-- 63 of 334 --


Nesting of transactions is not recommended because Data Services uses pre-
commit checks and rollback processors to validate deferred constraints and
maintain data integrity. Using nested transactions must be done carefully to avoid
corrupting these mechanisms.
For more details on transaction isolation levels, concurrent access handling, and rollback behavior,
see Transaction Management and Concurrency.
Read Replica Routing
When a PostgreSQL read replica is configured, Data Services automatically routes JSON-RPC
requests to the replica if every operation in the batch is non-mutating.
An operation is treated as non-mutating when its @RemoteOperation annotation declares isMutation
= false. The default value is true, so operations that do not set this attribute always use the primary
datasource.
Example of a read-only custom operation
@RemoteOperation(name = "MY_QUERY_OPERATION", isMutation = false)
@Component
public class MyQueryOperation {
public MyResult rpc(@JsonRpcParam("id") String id) {
// read-only logic here
}
}
If a batch mixes mutating and non-mutating operations, the entire request is routed to the primary
datasource. When no replica is configured, all requests use the primary datasource regardless of
isMutation.
See PostgreSQL Read Replica Configuration for setup instructions.
Data Integrity Constraints
Data Services enforces two categories of data integrity constraints during JSON-RPC request
processing:
• Unique constraints on document fields — see the Documents section for full details.
• Relationship Model cardinality constraints on links — described below.
Link mutations might result in an inconsistent database state regarding the Relationship Model
(RM):
Relationship models define lower and upper limits on the number of documents of a certain model,
that can participate in a particular relationship. These constraints cannot be checked during the
execution of a single Operation because it is only required that the whole JSON-RPC request should
bring a DB from one valid state to another.
64

-- 64 of 334 --

This means: The validations of separated Operations do not matter, only the final state of the
transaction must be valid.
Example:
In one JSON-RPC request there might be a couple of ADD_LINKS Operations and a couple of
DELETE_LINKS Operations. Some of the ADD_LINK Operations might violate an upper limit defined in
the RM, but those limit violations could be fixed with subsequent DELETE_LINK Operations.
Lower and upper limits are implemented as deferred constraints. The JSON-RPC processing will
collect the information about which links have been added and which have been deleted, and the
final state will be validated after all Operations have been executed and before the transaction will
be committed.
The transaction will be committed if there are no violations, otherwise a rollback ot the transaction
will be issued.

Links that are added and deleted by ADD_LINK or DELETE_LINK Operations will be
collected in a LinkRefs collection. The LinkRefs are validated after all Operations
have been executed and are available only in the scope of an JSON-RPC request.
Therefore, a direct call of the ADD_LINK or DELETE_LINK Operation is not
recommended because in this case no deferred constraints validation will be
performed, and the collections might get corrupted. Validation would either have
to be called later, or the collections need to be cleared after such a call.
 Due to multiple problems with the data integrity checks, the first iteration of
relationship will not support lower limit feature.
Implementing a Custom Operation
JSON-RPC methods are extendable, and client projects can implement their own Operations.
Artifact examples-extending-server contains 2 custom Operation examples (ECHO &
GERMAN_BUSINESS_PARTNERS) which will become available via JSON-RPC Endpoint if the artifact will be
available on the classpath. Those Operations serve as an example of how to implement custom
Operations.
The example code is available in examples-extending-server:
• com.mgmtp.a12.dataservices.examples.operation.ExampleEchoOperation
• com.mgmtp.a12.examples.query.GermanBusinessPartnersOperation
 It is necessary to add the custom Operation to the list of allowed Operations in
mgmtp.a12.dataservices.jsonRpc.allowedOperations.
Placeholder Resolution
Data Services JSON-RPC enables referencing results from previous Operations to build complex
requests. This uses SpEL expressions, restricted for security, allowing only access to properties
65

-- 65 of 334 --

stored in the request context by earlier Operations using their ids.
Example of referencing the result of a previous Operation
[
{
"jsonrpc": "2.0",
"method": "ADD_DOCUMENT",
"id": "AddBrand",
"params": {
"document": {
"Brand": {
"name": "Adidas",
"taxId": "123456789"
}
},
"documentModelName": "DomainBrand",
"locale": "en"
}
},
{
"jsonrpc": "2.0",
"id": "AddAddidasToProduct",
"method": "ADD_LINK",
"params": {
"linkDescriptor": {
"relationshipModel": "ProductBrand",
"entities": [
{
"role": "ProductRole",
"docRef": "Product/123"
},
{
"role": "BrandRole",
"docRef": "#{#AddBrand.metadata.docRef}"
}
]
}
}
}
]
The result of the AddBrand Operation is docRef which is used inside the AddAddidasToProduct
Operation by the statement #{#AddBrand.metadata.docRef}, where AddBrand is a reference to the
Operation id (must be executed in the same request) and docRef is the identification of the created
document. Each Operation has a different result set, therefore the creator of the request must make
sure that the content for a field and the resolved SpEL expression match the requirement.
By default, SpEL functionality is disabled. To enable it you need to use the configuration property
mgmtp.a12.dataservices.jsonRpc.spel.enabled.
66

-- 66 of 334 --

Exception Handling
If there is a syntax error, or the placeholder can’t be resolved, we will throw the error of SpEL to
the client. The following code snippet shows the error case when placeholder can’t be resolved.
Example of SpEL error
{
"title": {
"key": "rpc.operation.error",
"default": "JSON-RPC request failed and rollback was performed"
},
"description": {
"key": "error.convert.json",
"default": "SpEL evaluation error occurred!"
}
}
SpEL Example
Let’s assume we have two Operations which are called ADD_OBJECT and ADD_OBJECT_REFERENCE.
The responses of these Operations are just the created ids, which means that the response object of
the Operation outcome would be of type String. But the result of ADD_OBJECT, which will be stored in
the context, will be an object that holds the object id in the field docRef of type String and the
creation date in the field createdAt of type Date.
This means that the Operation ADD_OBJECT_REFERENCE has access to the attributes docRef and
createdAt of the object created by the Operation ADD_OBJECT.
Example of SpEL usage
[
{
"jsonrpc": "2.0",
"id": "createFirstObject",
"method": "ADD_OBJECT",
"params": {
"object": {
"value": "some Text"
}
}
},
{
"jsonrpc": "2.0",
"id": "createSecondObject",
"method": "ADD_OBJECT_REFERENCE",
"params": {
"object": {
"value": "regularValue",
"referencedObjectId": "#{#createFirstObject.metadata.docRef}",
67

-- 67 of 334 --

"referencedObjectCreation": "#{#createFirstObject.metadata.createdAt}"
}
}
}
]
Implementation
The Java class AddDocumentOperation has to put an object of type DataServicesDocument in the context,
and this is how it should be done:
OperationContextHolder.put(dataservicesDocument);
With the help of SpEL we then have access to all the fields of the result object and can get the id
(docRef) of the object.
Dispatching Requests From The Client-Side
Data Services provides a convenience wrapper for dispatching requests in a type-safe way. Both
REST and JSON-RPC requests are supported.
 Using this API always assumes a configured ServerConnector (provided by the
@com.mgmtp.a12.utils/utils-connector package).
REST
When dispatching any kind of rest request, the method Dispatcher.rest(<request>, <typeguard>)
can be used, e.g. like so:
import { Dispatcher } from "@com.mgmtp.a12.dataservices/dataservices-
access/lib/dispatch/index.js";
import type { RestRequestPayload } from "@com.mgmtp.a12.utils/utils-
connector/lib/main/index.js";
const myRequest: RestRequestPayload = { ... }
type MyResponseType = { ... }
const isMyResponse = (value: unknown): value is MyResponseType => ...
const myResponse = await Dispatcher.rest(myRequest, isMyResponse);
The wrapper will fetch the request via the server connector and assert that the given typeguard
matches the response before returning it.
 Because any kind of request can be made here (as long as it matches the
RestRequestPayload type), you are responsible for providing the correct typeguard
for the response, e.g. the wrapper does not stop you from passing a request of type
68

-- 68 of 334 --

A with a typeguard for response B (which probably always fails at runtime).
JSON-RPC
When dispatching standard rpc requests provided by Data Services, the method
Dispatcher.rpc(<language>, [<request>,…]) can be used. Consider the following example:
import { Dispatcher } from "@com.mgmtp.a12.dataservices/dataservices-
access/lib/dispatch/index.js";
import type { DocumentJsonRpc2Request } from
"@com.mgmtp.a12.dataservices/dataservices-access/lib/Document/index.js";
const currentLanguage = "<language of current user>"
const addDocumentRequest: DocumentJsonRpc2Request.AddJsonRpc2Request = { ... }
const [addDocumentResponse] = await Dispatcher.rpc(currentLanguage,
[addDocumentRequest]);
Like above, the wrapper will fetch all given requests and assert the type of each response according
to its corresponding request. Since we’re dispatching a single request of type ADD_DOCUMENT,
typescript is able to infer the response typing for it:
• the return value of the wrapper call is an array with length 1 (a single response)
• the single element of the array has a specific type (because the request also had a specific type)
This also works when you want to dispatch multiple requests and/or the typing of the request can
be one of multiple. Consider this example:
import { Dispatcher } from "@com.mgmtp.a12.dataservices/dataservices-
access/lib/dispatch/index.js";
import type { DocumentJsonRpc2Request } from
"@com.mgmtp.a12.dataservices/dataservices-access/lib/Document/index.js";
// some logic here that decides which type of request to do
const addOrModifyRequest: DocumentJsonRpc2Request.AddJsonRpc2Request |
DocumentJsonRpc2Request.ModifyJsonRpc2Request = { ... }
const deleteRequest: DocumentJsonRpc2Request.DeleteJsonRpc2Request = { ... }
const [addOrModifyResponse, deleteResponse] = await Dispatcher.rpc(currentLanguage,
[addOrModifyRequest, deleteRequest]);
Again, both responses are typed correctly: The first one is either a ADD_DOCUMENT or MODIFY_DOCUMENT
response (as the request might have been either one), whereas the second response is definitely a
DELETE_DOCUMENT response (because here the request was specific).
 To be able to infer the correct response typing for a request, typescript needs to
know about every typing. For this reason, the rpc dispatcher only supports passing
69

-- 69 of 334 --

the request typings provided by Data Services. Trying to use custom requests here
will produce compile and runtime errors.
Modifying / Replacing Rpc Requests From The Client-Side
The RequestFilter API of the ServerConnector from the @com.mgmtp.a12.utils/utils-connector
package provides low-level access to request modifications (from modifying single parameters of
an operation to completely replacing them). Consider the following example:
import { JsonRpc2Request } from "@com.mgmtp.a12.dataservices/dataservices-
access/lib/json-rpc/index.js";
import type { QueryJsonRpc2Request } from "@com.mgmtp.a12.dataservices/dataservices-
access/lib/query/Request.js";
import type { RequestFilter } from "@com.mgmtp.a12.utils/utils-
connector/lib/main/index.js";
export const MyCustomRequestFilter: RequestFilter = {
canHandleRequest({ request }) {
if (typeof request.body !== "string") {
return false;
}
const json = JSON.parse(request.body);
// we only want to change requests where the body contains rpc requests
return Array.isArray(json) && json.every(JsonRpc2Request.isInstance);
},
doRequestFilter({ request }) {
// this assertion is "allowed" because we check the type above
const rpcArray = JSON.parse(request.body as string) as JsonRpc2Request[];
const customizedRpcs = rpcArray.map(rpc => (isQueryRequest(rpc) ?
customizeQuery(rpc) : rpc));
return {
request: {
...request,
body: JSON.stringify(customizedRpcs)
},
continue: true
};
}
};
// the typeguard that defines which kind of requests we want to look at
function isQueryRequest(value: JsonRpc2Request): value is QueryJsonRpc2Request {
return JsonRpc2Request.isInstance(value) && value.method === "QUERY";
}
70

-- 70 of 334 --

// the actual customization
function customizeQuery(rpc: QueryJsonRpc2Request) {
return {
...rpc,
params: {
...rpc.params,
query: {
...rpc.params.query,
customized: "yes"
}
}
};
}
In this code snippet, a RequestFilter is defined that will customize any rpc operation of type
"QUERY" it encounters.
Here, the actual customization is just adding a property "customized" for illustration purposes. In
reality, you could transform the request in any way you like (for example, remove certain
parameters, or add new ones).
Instead of modifying the request, you can also replace it with a different one (for example, to
replace all rpc operations of type "METHOD" with your own operation type
"MY_CUSTOM_METHOD"). In the same way, you could also transform the header of the request.

When heavily modifying or replacing requests, make sure to still return the
correct response! Since the caller does not know about your modification, it might
break when you violate its assumptions about how the response will look like.
For example, client-side code that dispatches some kind of "LIST_THINGS" request
will probably expect an array of "things" to be returned in the response. If your
custom filter modifies this request in such a way that the response now includes
only a single "thing", it would break when the calling client-side code accesses the
response.
With your filter defined, make sure to pass it into your ServerConnector during initialization.
For example, when defining your own connector, it could look like this:
import {
ConnectorLocator,
RestServerConnector
} from "@com.mgmtp.a12.utils/utils-connector/lib/main/index.js";
// passing the filter from the example above
const serverConnector = new RestServerConnector("./myApi", [MyCustomRequestFilter, /*
other request filters you might have */]);
ConnectorLocator.createInstance(serverConnector);
71

-- 71 of 334 --

When using a configuration mechanism that sets up the ServerConnector for you (for example, using
UAA), look for a configuration setting that allows providing request filters (e.g. a property called
additionalRequestFilters) and pass your custom filter there.

While this approach is powerful in the sense that it allows modification of all
requests (that are dispatched through the ServerConnector), its low-level nature
also means that request modification based on certain conditions may not be
possible (for example, when adding a certain request parameter depends on who
dispatched the request on the client-side).
JSON-RPC 2.0 Core Operations
 Parameters marked with  are mandatory and should not be omitted nor null.
Other parameters (marked with ) can be bypassed or set to null.
• RELINK_DOCUMENT • ADD_LINK • DELETE_LINK • MODIFY_LINK •
LOAD_ATTACHMENT_HEADER • LOAD_THUMBNAIL_URL • LOAD_ATTACHMENT_URL •
MODIFY_DOCUMENT • PARTIAL_MODIFY_DOCUMENT • MULTI_DELETE_DOCUMENTS •
GET_DOCUMENT • ADD_DOCUMENT • DELETE_DOCUMENT • VALIDATE_DOCUMENT •
COPY_DOCUMENT • CHECK_UNIQUENESS • QUERY
RELINK_DOCUMENT
Change the link assignment for a specific document. It deletes the link by linkRef reference and
adds a new link which is defined by linkDescriptor.
• The operation reports an error if the linkRef references a link that does not exist, because the
link document must be reused for newly created link.
• The operation reports an error if the linkDocument is required in the new relationship, but it is
not present in the old link.
The operation fires the following events: RelationshipLinkAfterCreateEvent,
RelationshipLinkAfterDeleteEvent.
For relationshipModel parameter, the error response will different for null and empty value.
Parameters
 linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor
Descriptor of the new link (must contain the desired document reference of linkRef).
 linkRef: java.lang.String
The reference to the link that needs to be moved.
Result
com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec: The RelationshipLinkSpec of a
new link.
72

-- 72 of 334 --

Call sequence
«Operation»	
RELINK_DOCUMENT
«Operation»	
RELINK_DOCUMENT
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
RelationshipLinkAfterCreateEvent
«Event»	
RelationshipLinkAfterCreateEvent
«Event»	
RelationshipLinkAfterDeleteEvent
«Event»	
RelationshipLinkAfterDeleteEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 1. Sequence of RELINK_DOCUMENT calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
73

-- 73 of 334 --

configuration could lead to different results.
ADD_LINK
Link two documents together.
To add a link you need to have a Relationship Model (link.linkDescriptor.relationshipModel) which
defines the document models and their subtypes that can be linked together.
Additionally, you should specify exactly two roles, each of which consist of the role name
(link.linkDescriptor.entities.role) and the DocumentReference of the linked document
(link.linkDescriptor.entities.docRef).
You can also add a link document which contains the link metadata.
If the unbounded field in the RM is false, the RM can also define an upper limit of links for a
document of a role. Then the number of links for a document must not be exceeded for this role.
This maximum number is defined in the RM under upperLimit.
This operation does not ensure the deferred data integrity constraints (upper limit). Instead, the
ADD LINK operation fires events that fill a ThreadLocal collection, which is used by
RelationshipLinkOperationValidator only after all the operations are finished. Calling this operation
directly therefore might corrupt link integrity and cause issues with subsequent RPC operations
due to the usage of ThreadLocal collections for validation.
Parameters
 linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor
The LinkDescriptor.
 linkDocument: tools.jackson.databind.JsonNode
An object of type JsonNode that represents the document.
Result
com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec: The result is the
RelationshipLinkSpec of the newly created link.
Call sequence
74

-- 74 of 334 --

«Operation»
ADD_LINK
«Operation»
ADD_LINK
«Event»
ModelAfterRepositor yLoadEvent
«Event»
ModelAfterRepositor yLoadEvent
«Security»	
Pre checks
Model Read
«Security»	
Pre checks
Model Read
«Event»
ModelAfterLoadEvent
«Event»
ModelAfterLoadEvent
«Event»
DocumentAfterRepositor yLoadEvent
«Event»
DocumentAfterRepositor yLoadEvent
«Event»
RelationshipLinkAfterCreateEvent
«Event»
RelationshipLinkAfterCreateEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 2. Sequence of ADD_LINK calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
DELETE_LINK
Delete one link of the relationship.
If the link doesn’t exist, the operation silently finishes without error to achieve idempotent
behavior.
If there is no error the result will always return the status code 200 regardless if the link has been
found or not.
75

-- 75 of 334 --

Deletes a relationship link as specified by the given RelationshipLinkSpec. If a non-existing link id is
provided in relationshipLinkSpec.getId(), authorization will not be applied because there is no
authorization for a non-existing link.
relationshipLinkSpec.getLinkDescriptor().getRelationshipModel() will not be evaluated for
authorization until a valid link id is provided in relationshipLinkSpec.
Parameters
 linkRef: com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec
the specification containing all link information
Result
void:
Call sequence
«Operation»
DELETE_LINK
«Operation»
DELETE_LINK
«Event»
ModelAfterRepositor yLoadEvent
«Event»
ModelAfterRepositor yLoadEvent
«Security»
Pre checks
Model Read
«Security»
Pre checks
Model Read
«Event»
ModelAfterLoadEvent
«Event»
ModelAfterLoadEvent
«Event»
RelationshipLinkAfterDeleteEvent
«Event»
RelationshipLinkAfterDeleteEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 3. Sequence of DELETE_LINK calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
MODIFY_LINK
Modify the linkDocument of a link.
76

-- 76 of 334 --

The operation fires the following events: ModelAfterLoadEvent,RelationshipLinkAfterUpdateEvent.
The linkDocument must be null when the link document model is null, but it must be provided if the
model is specified. Otherwise RelationshipLinkDocumentNotAllowedException or
RelationshipLinkDocumentMissingException will be thrown.
For relationshipModel parameter, the error response will different for null and empty value.
Parameters
 linkRef: com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec
The RelationshipLinkSpec that contains all link information.
 linkDocument: tools.jackson.databind.JsonNode
An object of type JsonNode that represents the link document.
Result
void:
Call sequence
«Operation»
MODIFY_LINK
«Operation»
MODIFY_LINK
«Event»
ModelAfterRepositor yLoadEvent
«Event»
ModelAfterRepositor yLoadEvent
«Security»
Pre checks
Model Read
«Security»
Pre checks
Model Read
«Event»
ModelAfterLoadEvent
«Event»
ModelAfterLoadEvent
«Event»
RelationshipLinkAfterUpdateEvent
«Event»
RelationshipLinkAfterUpdateEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 4. Sequence of MODIFY_LINK calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
LOAD_ATTACHMENT_HEADER
Get AttachmentHeader of the attachment by attachmentId.
77

-- 77 of 334 --

Parameters
 attachmentId: java.lang.String
The attachment id.
 docRef: java.lang.String
The reference of the document to which the attachment is assigned.
Result
com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSpec: Object of type AttachmentHeader.
Call sequence
«Operation»	
LOAD_ATTACHMENT_HEADER
«Operation»	
LOAD_ATTACHMENT_HEADER
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
DocumentAfterLoadEvent
«Event»	
DocumentAfterLoadEvent
.authorization.internal.DefaultDocumentPermissionEvaluator#hasDocumentQueryPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 5. Sequence of LOAD_ATTACHMENT_HEADER calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
78

-- 78 of 334 --

LOAD_THUMBNAIL_URL
Get AttachmentThumbnailUrl which contains all thumbnail URLs of an attachment.
Parameters
 attachmentId: java.lang.String
The attachment id.
Result
com.mgmtp.a12.dataservices.attachment.AttachmentThumbnailUrl: Object of type
AttachmentThumbnailUrl with properties: smallThumbnailUrl:: type String, bigThumbnailUrl:: type
String.
Call sequence
«Operation»
LOAD_THUMBNAIL_URL
«Operation»
LOAD_THUMBNAIL_URL
Figure 6. Sequence of LOAD_THUMBNAIL_URL calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
LOAD_ATTACHMENT_URL
Get URL of attachment from Data Services. The link should be considered to be secure, which
means that it should be unpredictable and only temporarily accessible.
Parameters
 attachmentId: java.lang.String
The attachment id.
 docRef: java.lang.String
The reference of the document to which the attachment is assigned.
79

-- 79 of 334 --

Result
com.mgmtp.a12.dataservices.attachment.DataServicesAttachmentURL: Object of type
DataServicesAttachmentURL.
Call sequence
«Operation»	
LOAD_ATTACHMENT_URL
«Operation»	
LOAD_ATTACHMENT_URL
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Quer y
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
QueryAfterPostProcessPhaseEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.DefaultDocumentPermissionEvaluator#hasDocumentQueryPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 7. Sequence of LOAD_ATTACHMENT_URL calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
MODIFY_DOCUMENT
Update the content of the document.
The operation fires the following events during document modification: DocumentBeforeUpdateEvent,
DocumentAfterUpdateEvent, DocumentAfterRepositoryLoadEvent.
80

-- 80 of 334 --

Parameters
 docRef: java.lang.String
 document: tools.jackson.databind.JsonNode
A document in JSON format.
 locale: java.util.Locale
The locale against which the document will be validated (language of the locale must be present
in the language definition of the document model).
Result
void:
Call sequence
«Operation»	
MODIFY_DOCUMENT
«Operation»	
MODIFY_DOCUMENT
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Security»	
Pre checks	
D o c u m e n t U p d a t e
«Security»	
Pre checks	
D o c u m e n t U p d a t e
«Event»	
D o c u m e n t B e fo r e U p d a t e E v e n t
«Event»	
D o c u m e n t B e fo r e U p d a t e E v e n t
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
DocumentBeforeRepositor ySaveEvent
«Event»	
DocumentBeforeRepositor ySaveEvent
«Event»	
DocumentBeforeIndexEvent
«Event»	
DocumentBeforeIndexEvent
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
DocumentAfterRepositor yUpdateEvent
«Event»	
DocumentAfterRepositor yUpdateEvent
«Event»	
D o c u m e n t A f t e r U p d a t e E v e n t
«Event»	
D o c u m e n t A f t e r U p d a t e E v e n t
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.DefaultDocumentPermissionEvaluator#checkPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 8. Sequence of MODIFY_DOCUMENT calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
PARTIAL_MODIFY_DOCUMENT
Modify an existing document by specifying the parts that will be added, modified, or deleted.
Possible changes are:
• Altering the value of an existing field, including setting it to null (which results in deletion).
81

-- 81 of 334 --

• Adding a new field within an existing group.
• Adding a new field within a non-existent group, which implicitly creates all missing groups.
• Deleting a group or field.
The operation fires the following events during modification of the document:
DocumentBeforeUpdateEvent, DocumentAfterUpdateEvent, DocumentAfterRepositoryLoadEvent.
Parameters
 docRef: java.lang.String
 documentPart: java.util.List
A Set of DocumentPart describing the entity instances to be changed. A DocumentPart
encapsulates information about a specific segment or attribute within a document, defining how
it should be altered.
It consists of:
• path: The path to the segment or attribute within the document structure.
• value: The new value to be assigned to the specified segment or attribute. This can be null,
indicating deletion or removal.
• repetitions: An optional array specifying the repetition indices for multivalued attributes.
Example:
"documentPart": [
{
"path": "/Person/PersonalData/Nationality",
"value": "German",
"repetitions": [1,1,1]
}
]
 locale: java.util.Locale
The locale against which the document will be validated (language of the locale must be present
in the language definition of the document model).
Result
void:
Call sequence
82

-- 82 of 334 --

«Operation»	
PARTIAL_MODIFY_DOCUMENT
«Operation»	
PARTIAL_MODIFY_DOCUMENT
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Security»	
Pre checks	
D o c u m e n t Pa r t i a l U p d a t e
«Security»	
Pre checks	
D o c u m e n t Pa r t i a l U p d a t e
«Event»	
D o c u m e n t B e fo r e U p d a t e E v e n t
«Event»	
D o c u m e n t B e fo r e U p d a t e E v e n t
«Event»	
DocumentBeforeRepositor ySaveEvent
«Event»	
DocumentBeforeRepositor ySaveEvent
«Event»	
DocumentBeforeIndexEvent
«Event»	
DocumentBeforeIndexEvent
«Event»	
DocumentAfterRepositor yUpdateEvent
«Event»	
DocumentAfterRepositor yUpdateEvent
«Event»	
D o c u m e n t A f t e r U p d a t e E v e n t
«Event»	
D o c u m e n t A f t e r U p d a t e E v e n t
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.DefaultDocumentPermissionEvaluator#checkPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 9. Sequence of PARTIAL_MODIFY_DOCUMENT calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
MULTI_DELETE_DOCUMENTS
Efficiently deletes multiple documents in a single operation. It is designed to optimize multiple calls
to com.mgmtp.a12.dataservices.document.operation.internal.DeleteDocumentOperation. This
optimization is achieved by avoiding the retrieval of documents from the database, eliminating the
associated performance overhead.
If the document is absent, the operation gracefully concludes without triggering any errors,
maintaining an idempotent process. Before removing the document itself, all associated
relationship links are deleted. However, it’s important to note that if the document is used as a link
document within a relationship, the operation will encounter a failure.
It’s important to be aware that this approach comes with trade-offs. Firstly, it bypasses the standard
document Attribute-Based Access Control (ABAC) checks, and secondly, it does not provide fine-
grained control for selecting the appropriate document repository through the
com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository#supports(DocumentV2)
method. Instead, it is required to use
com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository#supports(String,
Optional).
Parameters
83

-- 83 of 334 --

 docRefs: java.util.Collection
A collection of document references to be deleted.
Result
void:
Call sequence
«Operation»	
MULTI_DELETE_DOCUMENTS
«Operation»	
MULTI_DELETE_DOCUMENTS
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
D o c u m e n t M u l t i D e l e t e
«Security»	
Pre checks	
D o c u m e n t M u l t i D e l e t e
«Event»	
DocumentsBeforeDeleteEvent
«Event»	
DocumentsBeforeDeleteEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
RelationshipLinkAfterDeleteEvent
«Event»	
RelationshipLinkAfterDeleteEvent
«Event»	
D o c u m e n t s A f t e r D e l e t e E v e n t
«Event»	
D o c u m e n t s A f t e r D e l e t e E v e n t
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.DefaultDocumentPermissionEvaluator#checkDocumentMultiDeletePermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 10. Sequence of MULTI_DELETE_DOCUMENTS calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
GET_DOCUMENT
Get DocumentSpec of the document by its DocumentReference. This operation is designed
especially for getting the content of a single document.
Parameters
 docRef: java.lang.String
The DocumentReference of requested document.
Result
com.mgmtp.a12.dataservices.document.DocumentSpec: An object of type DocumentSpec with
properties: docRef:: type DocumentReference, documentModelName:: type String, document:: type
Document.
Call sequence
84

-- 84 of 334 --

«Operation»	
GET_DOCUMENT
«Operation»	
GET_DOCUMENT
«Event»	
GetDocumentBeforeEvent
«Event»	
GetDocumentBeforeEvent
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
DocumentAfterLoadEvent
«Event»	
DocumentAfterLoadEvent
«Event»	
G e t D o c u m e n t A f t e r E v e n t
«Event»	
G e t D o c u m e n t A f t e r E v e n t
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.DefaultDocumentPermissionEvaluator#hasDocumentQueryPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 11. Sequence of GET_DOCUMENT calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
ADD_DOCUMENT
Create a new document of particular model. The handling of the document could vary depending
on the model, where the com.mgmtp.a12.dataservices.document.DocumentService will find all
implementations of the com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository
and take the first one which supports the document model of the persisted document.
The operation fires the following events during document creation:
DocumentBeforeCreateEvent,DocumentAfterCreateEvent.
 This operation is now used to create a document using JSON format instead of
formerly used /docs/:DOCUMENT_MODEL endpoint.
85

-- 85 of 334 --

Parameters
 documentModelName: java.lang.String
Model of the document.
 document: tools.jackson.databind.JsonNode
Content of the document.
 locale: java.util.Locale
The locale for document validations and computations.
Result
com.mgmtp.a12.dataservices.document.DocumentReferenceResult: In case no error occurs the response
will contain the docRef of the newly created document.
Call sequence
«Operation»	
ADD_DOCUMENT
«Operation»	
ADD_DOCUMENT
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Security»	
Pre checks	
D o c u m e n t C r e a t e
«Security»	
Pre checks	
D o c u m e n t C r e a t e
«Event»	
DocumentBeforeCreateEvent
«Event»	
DocumentBeforeCreateEvent
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
DocumentBeforeRepositor ySaveEvent
«Event»	
DocumentBeforeRepositor ySaveEvent
«Event»	
DocumentBeforeIndexEvent
«Event»	
DocumentBeforeIndexEvent
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
DocumentAfterRepositor yCreateEvent
«Event»	
DocumentAfterRepositor yCreateEvent
«Event»	
D o c u m e n t A f t e r C r e a t e E v e n t
«Event»	
D o c u m e n t A f t e r C r e a t e E v e n t
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.DefaultDocumentPermissionEvaluator#hasDocumentCreatePermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 12. Sequence of ADD_DOCUMENT calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
DELETE_DOCUMENT
Delete an existing document. If the document doesn’t exist, the operation silently finishes without
error to achieve idempotent behavior.
All relationship links in which the document participates will be deleted before the deletion of the
86

-- 86 of 334 --

document itself.
The operation fires the following events during document deletion: DocumentBeforeDeleteEvent,
DocumentAfterDeleteEvent, DocumentAfterRepositoryLoadEvent.
Parameters
 docRef: java.lang.String
Reference to the document in format DocumentModel/DocumentId.
 locale: java.util.Locale
The locale against which the document will be validated.
Result
void:
Call sequence
«Operation»	
DELETE_DOCUMENT
«Operation»	
DELETE_DOCUMENT
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Security»	
Pre checks	
D o c u m e n t D e l e t e
«Security»	
Pre checks	
D o c u m e n t D e l e t e
«Event»	
D o c u m e n t B e fo r e D e l e t e E v e n t
«Event»	
D o c u m e n t B e fo r e D e l e t e E v e n t
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
RelationshipLinkAfterDeleteEvent
«Event»	
RelationshipLinkAfterDeleteEvent
«Event»	
DocumentAfterRepositor yDeleteEvent
«Event»	
DocumentAfterRepositor yDeleteEvent
«Event»	
D o c u m e n t A f t e r D e l e t e E v e n t
«Event»	
D o c u m e n t A f t e r D e l e t e E v e n t
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.DefaultDocumentPermissionEvaluator#checkPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 13. Sequence of DELETE_DOCUMENT calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
VALIDATE_DOCUMENT
Validate the JSON document.

This operation results in the Data Services JSON version of the Kernel validation
result IDocumentValidationResult from artifact kernel-md-runtime-api. This is done
so that the result of the operation will not be directly dependent on the Kernel API.
This means that any change of the kernel interfaces will not lead to breaking
changes in the result of the operation.
87

-- 87 of 334 --


It is possible to also include a custom condition to the validation by implementing
com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory interface as a bean.
Spring will discover all beans of ICustomConditionFactory interface and inject them
to the Kernel validation engine. For more information about Custom conditions
please see Kernel documentation.
Parameters
 documentModelName: java.lang.String
The document model name of the document to validate.
 document: tools.jackson.databind.JsonNode
A document in JSON format.
 partial: java.lang.Boolean
Non-mandatory boolean flag indicating that the document has been provided partially, which is
supposed to be considered during validation. By default, full document validation will be
executed.
 locale: java.util.Locale
The locale against which the document will be validated (language of the locale must be present
in the language definition of the document model).
Result
java.util.List: The result is a list of DocumentValidationError which contains:
errorText
a string mapped from Kernel error text,
errorCode
a string mapped from Kernel error code,
messageType
a string mapped from Kernel message type,
rulePath
a string mapped from Kernel rule path,
referencedFields
a list of referenced field.
Call sequence
88

-- 88 of 334 --

«Operation»
VALIDATE_DOCUMENT
«Operation»
VALIDATE_DOCUMENT
«Event»
ModelAfterRepositor yLoadEvent
«Event»
ModelAfterRepositor yLoadEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 14. Sequence of VALIDATE_DOCUMENT calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
COPY_DOCUMENT
Copy the document together with its attachments to a new one with the new DocumentReference.
The attachment IDs stay the same for the new document, but the attachment content is duplicated
to be independent of the source document.
The operation fires the following events during document creation: DocumentBeforeCreateEvent,
DocumentAfterCreateEvent, DocumentBeforeRepositorySaveEvent, DocumentAfterLoadEvent.
Parameters
 docRef: java.lang.String
The DocumentReference of the source document.
 locale: java.util.Locale
The locale for document validations and computations.
Result
com.mgmtp.a12.dataservices.document.DocumentReferenceResult: In case no error occurs the response
will contain the docRef of the newly created document.
89

-- 89 of 334 --

Call sequence
«Operation»	
COPY_DOCUMENT
«Operation»	
COPY_DOCUMENT
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
DocumentAfterLoadEvent
«Event»	
DocumentAfterLoadEvent
«Security»	
Pre checks	
D o c u m e n t C r e a t e
«Security»	
Pre checks	
D o c u m e n t C r e a t e
«Event»	
DocumentBeforeCreateEvent
«Event»	
DocumentBeforeCreateEvent
«Event»	
DocumentBeforeRepositor ySaveEvent
«Event»	
DocumentBeforeRepositor ySaveEvent
«Event»	
DocumentBeforeIndexEvent
«Event»	
DocumentBeforeIndexEvent
«Event»	
DocumentAfterRepositor yCreateEvent
«Event»	
DocumentAfterRepositor yCreateEvent
«Event»	
D o c u m e n t A f t e r C r e a t e E v e n t
«Event»	
D o c u m e n t A f t e r C r e a t e E v e n t
.authorization.internal.DefaultDocumentPermissionEvaluator#hasDocumentQueryPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.DefaultDocumentPermissionEvaluator#hasDocumentCreatePermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 15. Sequence of COPY_DOCUMENT calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
CHECK_UNIQUENESS
Read-only operation that checks whether a document would violate any uniqueness constraints
defined in its Document Model.
Constraint violations are reported in the response as a list and do not cause an exception.
Exceptions are only thrown for infrastructure failures such as a missing model or an unexpected
database error.
Checks all uniqueness constraints defined for the given Document Model against the provided
document.
Returns a CheckUniquenessResponse with an empty violations list when the document satisfies all
constraints, or a non-empty list describing each violated constraint.
90

-- 90 of 334 --

Constraint violations are always reported in the response \u2014 this method never throws because
of them. Exceptions are only thrown for infrastructure failures such as a missing model or an
unexpected database error.
Parameters
 documentModelName: java.lang.String
the Document Model name.
 document: tools.jackson.databind.JsonNode
the full document content in JSON format.
 docRef: com.mgmtp.a12.dataservices.document.DocumentReference
optional document reference of the document being updated; when provided, that document is
excluded from conflict detection.
Result
com.mgmtp.a12.dataservices.uniqueconstraint.CheckUniquenessResponse: the uniqueness check
result; violations is empty when all constraints are satisfied.
Call sequence
«Operation»
CHECK_UNIQUENESS
«Operation»
CHECK_UNIQUENESS
«Event»
ModelAfterRepositor yLoadEvent
«Event»
ModelAfterRepositor yLoadEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 16. Sequence of CHECK_UNIQUENESS calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
QUERY
RPC operation for all requests that use the Data Services Query API.
Executes the query operation to fetch document tree results based on the provided query
91

-- 91 of 334 --

parameters.
Parameters
 query: com.mgmtp.a12.dataservices.query.topology.QueryRoot
The query parameters for fetching document results.
Result
com.mgmtp.a12.dataservices.rpc.query.PagedResultSet: The result set of document tree results.
Call sequence
«Operation»	
QUERY
«Operation»	
QUERY
«Event»	
Quer yBeforeOperationEvent
«Event»	
Quer yBeforeOperationEvent
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Quer y
«Security»	
Pre checks	
Model Read
«Security»	
Pre checks	
Model Read
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterRepositor yLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
ModelAfterLoadEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
Quer yBeforeExecutionPhaseEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
DocumentAfterRepositor yLoadEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
QueryAfterPostProcessPhaseEvent
«Event»	
Quer yAfterOperationEvent
«Event»	
Quer yAfterOperationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.DefaultDocumentPermissionEvaluator#hasDocumentQueryPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
. q u e r y. e n r i ch e m e n t . E n r i ch m e n t s # c o m p u t e M o d e l S u b t y p e s	
¬ .authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.authorization.internal.CachedPermissionEvaluator#hasModelReadPermission
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.model.persistence .AbstractModelReadRepositor y#readModel	
¬ .common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
.common.events.internal.CommonDataServicesListenerMethodAdapter#onApplicationEvent
Figure 17. Sequence of QUERY calls.
 This sequence is compliant with the default settings without caches and with rpc
enabled which should show most of the events and security checks. Using different
configuration could lead to different results.
Exception Handling in Data Services
Overview
Data Services uses two separate exception handling paths with different behaviors:
92

-- 92 of 334 --

• RPC operations - handled by JsonRpcOperationDispatcher, no anonymized messages
• REST endpoints - handled by DataServicesExceptionsHandler, uses anonymized messages
Exception Handling Paths
Table 2. Exception Handling Architecture
Aspect RPC Operations REST Endpoints
Handler JsonRpcOperationDispatcher DataServicesExceptionsHandler
Scope @RemoteOperation methods via
/v2/rpc
Direct controllers (e.g.,
AggregationController)
Exception Types RpcException (extends
RuntimeException)
BaseException family
(implements
AnonymityException)
Response Format JSON-RPC JsonError objects HTTP status codes + JSON
response entities
Anonymized Messages Not supported Full AnonymityException support
Privacy Protection Structured OperationError
objects
getAnonymityMessage() method
Design Rationale
RpcException extends RuntimeException directly because RPC error payloads (via OperationError) are
already prepared to be displayed to the client. BaseException, on the other hand, is a server-side
exception whose content requires transformation (anonymization, HTTP status mapping) before
reaching the client.
All exceptions in Data Services should extend BaseException except RpcException-based ones, which
are intended to be sent to the client as-is.
Usage Guidelines
RPC Operations
Use for @RemoteOperation methods:
@RemoteOperation(name = "MY_OPERATION")
@Component
public class MyOperation {
@JsonRpcMethod("1")
public void rpcV1() {
throw new RpcException(cause, operationError); // No anonymized messages
}
}
93

-- 93 of 334 --

REST Endpoints
Use for direct REST controllers with anonymized messages:
@RestController
public class MyController {
@PostMapping("/my-endpoint")
public ResponseEntity<?> process() {
throw new QueryInvalidInputException(VALIDATION, ERROR_KEY, null)
.withAnonymityMessage("Links are not allowed for aggregation queries");
}
}
Privacy Rules for Anonymized Messages
CAN log: Model names, document metadata (except creator/modifier), link data, attachment
metadata, field paths, error codes
CANNOT log: Model content, document content, attachment content, user-related data, personal
information
Key Classes
• JsonRpcOperationDispatcher.resolveError() - RPC exception processing
• DataServicesExceptionsHandler.handle() - REST exception processing
• AnonymityException - Privacy protection interface
• BaseException.withAnonymityMessage() - Fluent anonymity message setting
• RpcException - RPC-specific exceptions
Query API
Glossary
Term Explanation Remark
index DS internal structure which
holds data for querying.
Data is stored in un-encrypted
manner in document_search and
document_fields tables.
to index a field Make a field available for
querying, sorting and CDD
construction.
By default, all fields are
indexed.
94

-- 94 of 334 --

Term Explanation Remark
query selection Defines constraints using
operators defining what data
should be loaded.
If no selection is applied all
documents from
targetDocumentModel are
matched.
query projection Query projection specifies how
the data should be retrieved
(complete load of documents,
just metadata,…) and what
additional data should be
retrieved (add links to selected
documents or result should be
in the form of CDD instead of a
plain document)
High-level Overview
The Query API is a secure and efficient data retrieval API that allows you to load data from Data
Services via various methods, including the JSON-RPC QUERY operation, a Java client, a TypeScript
client, or direct service calls. The query specification is represented as a JSON object (or Java POJO),
with the properties of this object defining the query parameters. The Query API also provides an
A12 abstraction, which is used to generate SQL statements for a PostgreSQL database.
The query operation and its underlying search layer are the only secure methods for retrieving
documents and links from DS. Each query consists of two main components: selection and
projection. Selection determines which documents should be retrieved, while projection adds
additional data to the selected documents.
The QUERY operation is a query protocol, meaning no default values will be applied if a property is
missing (e.g., paging, sort, …) The client is responsible for providing all required properties.
However, the client can define default values for these properties and use them across all queries
made to the DS.
95

-- 95 of 334 --

DS Server
JSON-RPC Wrapper
Quer y layer
Quer y Generator
SQL execution
Internal Client
JSON-RCP Server
Query Operation
QueryService
SQL generators
PostgreSQL
External Client
Figure 18. High-level overview
There are two clients of the Query API:
1. "External Client"—This client is external to the DS server. Communication can occur either
directly via HTTP or through a proxy, such as the TypeScript or Java client.
2. "Internal Client"—This client runs within the same process as the DS server. It can call the
QueryService directly, bypassing the HTTP/JSON-RPC overhead.
96

-- 96 of 334 --

Both internal and external clients use the same lower-level APIs. The only difference lies in how the
Query API is accessed.
The Query API specification is validated, enriched, and then transformed into one or more SQL
statements. The number of SQL statements generated depends on the query’s complexity and the
selected projection. The results of these SQL queries are mapped to the query result, which is then
returned to the client.
QUERY JSON-RPC
The QUERY operation will address all data retrieval needs for external clients.
Both Java and TypeScript clients are provided for the QUERY operation, which handle the mapping of
requests and responses for the QUERY operation. Some queries require a localization specification, as
they work with localized enumerations. In such cases, the localization must be provided via the
Accept-Language header in the HTTP request.

The Accept-Language header has to be set to a valid A12 locale, e.g. using the locale
of the current A12 user, because it is checked if the provided locale is present as a
locale in the queried document model (DM). If the language is not present, the
query will fail with an error. Currently, this check is performed case-insensitively,
meaning that en and EN are treated the same. However, with a locale in upper case
no results are found for a query on localized fields.
Request
All DS query capabilities are accessible via the QUERY JSON-RPC operation, which accepts a single
parameter, query, containing the full query specification. The query parameter includes the
following properties:
1. targetDocumentModel (Mandatory): A reference to the document model (DM) type for the root
document. Heterogeneity and security rules will be applied (see the Authorization section
below).
a. This property is mandatory because query construction depends on introspection of the
model graph, and results must always produce heterogeneous lists based on a single DM
root.
2. sort (Optional): A specification for sorting the root documents. (See the Sorting section below
for more details.)
a. Sorting is optional because the underlying storage does not require it. However, DS does not
apply any default sorting. It is strongly recommended to include a sorting specification in
the query. Paging is mandatory, and paging without sorting may lead to inconsistent results.
3. paging (Mandatory): Specify the pagination of the query results. (See the Paging section below
for more information.)
4. constraint (Optional): Defines an expression that limits the query results based on specific
operators. (See the Selection section below for more details.)
5. fields (Optional): Specifies which subset of fields should be returned for the document
97

-- 97 of 334 --

projection. (See the Projection section below for more details.)
6. links (Optional): Specifies which links/link documents should be included in the query results.
(See the Projection section below for more details.)
7. projectionName (Mandatory): A reference to the type of query projection. (See the Projection
section below.)
a. The projection type determines the shape of the result without specification what type we
cannot derive the data result shape.
8. aggregation (Optional): Allows aggregation functions to be applied on the grouped result set.
(See the Aggregation section below for more details.)
9. exclude (Optional): This is a flag used for excluding ROOT documents from current document
projection. By default, its value is false means ROOT documents are included in query response
entries section.
Queries can include nested specifications. At every level, a targetDocumentModel must be
defined—either directly (as in the root query) or indirectly (via the links property or the has
operator). The targetDocumentModel from a higher level serves as the source for the
targetDocumentModel at the next, lower level in the query.
Response
The response contains the following properties:
1. fullSize The total count of results matching the query.
2. page The pagination object received from the client.
a. DS does not modify this object.
3. entries The root documents returned from the selection. The documents in this section have
type=ROOT.
4. links The linked documents (type=CHILD) and link documents (type=LINK) returned from the
projection.
5. otherResults The other result returned from the projection.
Query In the Service Layer
The query JSON parameter from the JSON-RPC operation is deserialized into a QueryRoot object,
which serves as the parameter for the QueryService. Additionally, the service layer requires a
language parameter, as the HTTP layer sits above the service layer in the architecture.
Selection
Selection is specified by the constraint property of the query (or by the linkDocumentConstraint in
the has operator or in links property). The query selection specification is achieved through a
combination of nestable operators. Each operator represents a condition that must be satisfied for
the query to match results. There are three types of operators available in the Query API:
1. field-aware operators
98

-- 98 of 334 --

a. These operators require a document model (DM) and field references.
2. has operator
a. This operator uses information from the relationship model (RM) and the relationship_link
table.
3. logic operators
a. Logical operators combine field-aware and has operators into logical expressions.
Example:
{
"query": {
"targetDocumentModel": "BusinessPartner",
"constraint": {
"operator": "or",
"operands": [
{
"operator": "exact_match",
"field": "/BusinessPartnerRoot/Name",
"value": "Ludovici"
},
{
"operator": "has",
"relationshipModel": "PolicyHolder",
"targetRole": "Contract"
}
]
}
}
}
The or logic operator will match if at least one of its operands matches.
1. The exact_match operator will match if the value Ludovici is found in the field
/BusinessPartnerRoot/Name of the BusinessPartner document model (DM).
2. The has operator will match if there is a link between a BusinessPartner document and a
document of the DM defined in the Contract role for the PolicyHolder relationship model (RM).
Field-Aware Operators
Field-aware operators expect fields from document models (DMs) to construct a query. The DM is
not explicitly provided in the parameters of these operators because it is inferred from the query
context where the operator is used. If the operator appears in the constraint, the DM is the
targetDocumentModel. The has operator, however, can change the DM for its sub-constraints. For
more details, refer to the section on the has operator.
A field-aware operator for a repeatable field will match if at least one of the repeatable fields
satisfies the condition specified by the operator. There is no option right now to change this
99

-- 99 of 334 --

behavior to match all repeatable fields.
Exact Match Operator
The exact_match operator matches results based on the following specification:
1. field (Mandatory): A kernel path reference to the field.
a. All kernel data types are supported, except for ranges, as there are specialized operators for
those.
b. ICustomFieldType values will be serialized to strings during indexing, and these string values
will be used for matching.
2. value (Optional): Value that matches the field value exactly. This is a mandatory field if values is
not provided. There is no substring or partial matching available. For partial matching, use the
simple_search operator.
a. The value must not be null or empty. To check for null or empty values, use the
undefined_match operator.
3. values (Optional): A list of values to match exactly against the field’s value. This is a mandatory
field if value is not provided. This is an alternative to the value parameter, allowing multiple
values to be matched simultaneously. The behavior is similar to value, but it matches documents
where the field equals any of the specified values (logical OR). The maximum number of values
is configurable via mgmtp.a12.dataservices.query.exactMatch.maxValuesCount (default: 100).
a. None of the values in the list may be null. To check for null or empty values, use the
undefined_match operator.
4. caseSensitive (Optional): A boolean flag that specifies whether the match is case-sensitive.
(Default is true.)
a. This property is only applicable for IStringType, IEnumerationType and ICustomFieldType
fields.
b. For IEnumerationType fields, caseSensitive applies to the enumeration key only. The Accept-
Language header has no effect on enumeration matching because exact_match always
compares against the stored key, not the localized display text.
c. For IStringType fields with locale-aware indexing, caseSensitive = true also means that the
locale (i.e., the value of the Accept-Language HTTP header of the query request) is interpreted
case-sensitively, so that an exact match search on a localized field with Accept-Language = EN
will return no rows because Kernel prohibits locales that cannot be validated by
LocaleUtils.toLocale(String localeString). Data Services therefore strongly recommend to
always use locales in the format accepted by LocaleUtils.toLocale(String localeString) (i.e.
lower case language, upper case country, like en_US or de-BY).
For example, to match a field /Fields/Price must exactly match the value 3000.
Example of exact_match operator
{
"operator": "exact_match",
"field": "/Fields/price",
100

-- 100 of 334 --

"value": "3000"
}
The /Fields/sport field will match the value BaSkEtBAlL without considering case sensitivity. The
field type definition is not explicitly specified in the query; it is inferred from the document model
(DM).
Example of exact_match operator with case insensitive
{
"operator": "exact_match",
"caseSensitive": false,
"field": "/Fields/sport",
"value": "BaSkEtBAlL"
}
The /BusinessPartnerRoot/CustomerDiscount field is of type IEnumerationType. The exact_match
operator always matches against the enumeration key (internal value), regardless of the Accept-
Language header. To match on the localized display text, use simple_search instead.
Example of exact_match operator with enumeration type
{
"operator": "exact_match",
"field": "/BusinessPartnerRoot/CustomerDiscount",
"value": "DISCOUNT_90"
}
The values property matches documents where the /Fields/sport field equals any of the listed
values. This is equivalent to combining multiple exact_match operators with an or operator, but
more concise.
Example of exact_match operator with multiple values
{
"operator": "exact_match",
"field": "/Fields/sport",
"values": ["Football", "Basketball", "Tennis"]
}
Limitation:
To avoid issues with overly complicated or lengthy regular expression patterns that databases may
reject, a configurable size restriction has been implemented. This limit is managed via the
mgmtp.a12.dataservices.query.exactMatch.maxInputValueLength=100 property.
 A note on partial dates:
For the exact_match operator partial dates look exactly as they are passed into the
101

-- 101 of 334 --

database, i.e. a partial date of precision MONTH_OPTIONAL has a value of e.g. "2025-00-
00". Therefore, it is found if and only if the value of the exact_match operator is also
in this partial date format ("2025-00-00" in the example, searching with a value
"2025-01-01" will not return this item).
Range Operators
Range operators match if the value of the indexed field falls within the bounds defined by the from
and to parameters.
Each range operator includes three parameters:
• field (Mandatory): A kernel reference to the field.
• from (Optional): The lower bound of the range.
• to (Optional): The upper bound of the range.
Both from and to are optional, but at least one must be specified. The range is considered open on
the lower end if from is not present, and similarly, the range is open on the upper end if to is not
provided.
Different range operators expect specific types for the from and to parameters:
• The double_range operator expects INumberType.
• The date_range operator expects IDateType or IDateRangeType.
• The datefragment_range operator expects IDateFragmentType.
Double Range Operator
The double_range operator has the following parameters:
• from (Optional, Inclusive): A numeric value representing the lower bound of the range.
• to (Optional, Inclusive): A numeric value representing the upper bound of the range.
The operator matches values that fall within the range defined by from and to.
Date Range Operator
The date_range operator has the following parameters:
• from (Optional, Inclusive): A date value serialized to a string based on the format defined in the
document model (DM).
• to (Optional, Inclusive): A date value serialized to a string based on the format defined in the
DM.
• value (Optional): A date value serialized to a string based on the format defined in the DM. This
field is used for reverse range.
◦ This parameter is mutually exclusive with the from and to parameters.
◦ For IDateRangeType fields, this parameter also supports ISO 8601 interval format using a
102

-- 102 of 334 --

forward slash (/) as the separator:
▪ "2015-01-01/2020-12-31" - closed interval (both from and to specified)
▪ "2015-01-01/" - open interval with only the lower bound (from only)
▪ "/2020-12-31" - open interval with only the upper bound (to only)
• reverse (Optional): If true, checks if the specified date (in case of value) or date range (in case of
from / to) is within the range stored in the database. If false (default), checks if the database
value is within the specified date range.
The accepted types for this operator are:
• IDateType
• ITimeType
• IDateTimeType
• IDateRangeType
For a match to occur, the value of the DateRange must be fully contained within the range specified
by the date_range operator.
Example of date_range operator with open upper bound
{
"operator": "date_range",
"field": "/Contract/SignedAt",
"from": "2020-01-01"
}
This query filters all documents whose contract is signed from 2020-01-01 onwards.
Example of date_range operator with reverse range (specifed value is within the range stored in the
database)
{
"operator": "date_range",
"field": "/Fields/DateRanges/FullDateRange",
"value": "2020-01-01",
"reverse": true
}
This query filters all documents whose FullDateRange includes January 1, 2020.
Example of date_range operator with interval format on IDateRangeType field
{
"operator": "date_range",
"field": "/Contract/ValidityPeriod",
"value": "2015-01-01/2020-12-31"
103

-- 103 of 334 --

}

A note on partial dates:
The date_range operator sees partial date values filled up with defaults. For
example, if you pass a value of "2025-00-00" as a partial date with precision
MONTH_OPTIONAL to the database, it is filled up to "2025-01-01".
Correspondingly, the from and to values of a range_operator on a field with a partial
date are also filled up with defaults. Therefore, the same result will be returned
when you pass "2025-00-00" or "2025-01-00" or "2025-00-01" or "2025-01-01".
Date Fragment Range Operator
The datefragment_range operator has the following parameters:
• from (Optional, Inclusive): A date fragment as defined in the document model (DM).
• to (Optional, Inclusive): A date fragment as defined in the DM.
• value (Optional, Inclusive): A kernel-formatted value of type IDateFragmentType, which contains
the complete range.
◦ This parameter is mutually exclusive with the from and to parameters.
Undefined Match Operator
The undefined_match operator matches, if the field value is either null, empty, or the field is not
existing in the document. DS does not distinguish between these states, also they are not
propagated to the index for better performance.
The undefined_match operator has just one parameter:
• field (Mandatory): A kernel path reference to the field. If the supplied field value points to a
group, the query validation will reject the query. If validation is turned off, and the field points
to a group, the behavior of the operator is undetermined.
For example, the undefined_match operator matches if /Fields/sport is either null or empty.
Example of undefined_match operator
{
"operator": "undefined_match",
"field": "/Fields/sport"
}
Simple Search Operator
The simple_search operator searches across all indexed fields of a document model (DM). It is
called "simple" because it provides an easy-to-use interface (one field) for users who are unsure
which field contains the searched term. The operator is using substring case-insensitive match
algorithm.
104

-- 104 of 334 --

Parameters
1. fields (optional): Specifies the fields to be searched. This is optional; by default, the
simple_search operator will search all available indexed fields of the DM.
◦ All kernel data types are included. INumberType, IStringType, and IEnumerationType fields of
the DM are included in the search.
a. IEnumerationType fields require localization to be present in the HTTP header of the
JSON-RPC request or provided via the service layer. Only localization text matching the
query’s locale will be considered for simple_search if the fields property is specified.
Otherwise, the search will be performed on all localized labels.
b. The operator searches for enumeration labels, not enumeration values, since the labels
are displayed to the user, not the underlying values.
◦ IDateType fields be searched, but only in the format defined in DM.
◦ Other field types, such as IBooleanType and IConfirmType fields, or ICustomType fields are
turned into strings.
2. value (optional): The search term to be queried. This is a mandatory field if values is not
provided.
a. There is no query-time or index-time preprocessing applied to the values used in
simple_search. There is no special handling of any characters (whitespace included). What is
specified in the value field will be expected to be found in the index (case is ignored).
b. It must be at least 3 characters long.
i. While technically possible, querying for terms shorter than 3 characters is blocked by DS
to prevent potential DoS (Denial of Service) attacks. DS have a configuration key
mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize that allows changing
this behavior, although it is not recommended. The default value is 3. Searching for
smaller characters in larger data sets will cause performance issues.
ii. Special characters have no special handling. There is no query parser attached to value
field.
A. There is only one exception ~, which is removed from the value because it is
necessary in the underling implementation
3. values (optional): A list of search terms to be queried. This is a mandatory field if value is not
provided. This is an alternative to the value parameter, allowing multiple terms to be searched
simultaneously. The behavior is similar to value, but it matches documents containing any of the
specified terms (logical OR).
This operator provides a flexible and user-friendly search interface, especially when the exact field
containing the searched term is not known. However, it comes with some restrictions, especially
regarding the types of fields and the minimum term length.
Properties of Simple Search
• Case insensitivity: The search is case-insensitive, meaning the case in the document does not
need to match the case in the search value. For example, searching for "contract" will match
"Contract", "CONTRACT", or any another case variation. This also applies to the locale which is
105

-- 105 of 334 --

passed with the Accept-Language HTTP header of the query request: The locales en and EN are
treated identically. But please note, that this will change in future when Data Services will
introduce a more strict locale validation. Therefore, it is strongly recommended to always use a
locale that can be validated by LocaleUtils.toLocale(String localeString), which demands
lower case language and upper case country, like en_US.
• Substring match: The input string must be a substring of the value in the field being searched.
This allows for partial matches within indexed fields.
• Minimum length: The search term must be at least 3 characters long. This is to prevent
performance issues and potential DoS attacks.
• No Composed Data Documents (CDD) in the index: Since simple_search does not operate on
Composed Data Documents (CDDs), repeatable groups of CDM (Composed Document Models)
cannot be included in the search. Only the fields of the queryRoot document (CRD) are available
for searching.
• No special character handling: The search term is treated as a simple string, and no special
handling is applied to characters. For example, searching for "contract&name" will not yield
results if the indexed field contains "contract name" or "name contract". The & character is
treated as part of the string.
◦ ~ is an exception. I.e. term Somet~hing will be changed to Something
This is a default search option for search toolbar in Overview, Relationship and Tree Engines.
Limitation
To avoid issues with overly complicated or lengthy regular expression patterns that databases may
reject, a configurable size restriction has been implemented.
This limit is managed via the mgmtp.a12.dataservices.query.simpleSearch.maxInputValueLength
property.
Behavior Example
This section demonstrates the expected results of a default simple_search configuration, providing
justifications for what results will be returned based on sample data. For simplicity, we assume the
following conditions for our example data:
• Only simple documents without links: The data consists of simple documents without any
links. Querying for linked documents is done using the has operator, which is not relevant for
simple_search. Therefore, links are excluded from this example.
• No repeatable groups: The example data does not contain repeatable groups. While content in
repeatable groups would be searchable, we omit these fields for simplicity in this example. This
does not affect the results, as the content of repeatable groups is still searchable via
simple_search.
• No CDMs (Composed Data Documents): simple_search operates only on the root group fields of
CDMs in the same way it works for regular DMs (Document Models). Since there are no CDMs in
this example, this factor does not impact the demonstration.
• No heterogeneity: The example assumes that heterogeneity has been resolved during the
106

-- 106 of 334 --

query enrichment phase, meaning the query will operate against a single DM. Therefore, we do
not need to consider heterogeneity in this case.
Example Data
Enumeration values from the model
Enum key Enum en Enum de
IT IT Informationstechnologie
Banking Banking Bankwesen
Accountancy Accountancy Buchhaltung
Healthcare Healthcare Gesundheitswesen
Legal Legal Gesetzlich
Documents and their fields
DocRef ContractName
(TEXT)
LengthOfContrac
t (NUMBER)
Industry (ENUM) createdAt
(DateTimeType -
yyyy-MM-
dd’T’HH:mm:ss)
1 ContractName 013 IT 2021-10-
01T12:00:00
2 contract&Name 012 Banking 2022-10-
01T12:00:00
3 This contr@ct has
a ridiculously long
contract name
without any
particular reason
66.6 Healthcare 2023-10-
01T12:00:00
4 Ludovici Cole Est
Frigus
1 Legal 2024-10-
01T12:00:00
5 tracol 301200 Accountancy 2025-10-
01T12:00:00
6 Name contract 012 IT 2026-10-
01T12:00:00
Queries:
107

-- 107 of 334 --

Use-case number Search term Documents
returned
Justification Remark
1 Contract 1,2,6 Not matched
documents
contain no fields
with a value that
contains Contract
search term.
-
2 Contract& 2 & is a regular
character, and the
literal contract&
can only be found
in document 2.
Searching for
ract& will also
match only
document 2.
3 Contract Name 3 Only document 3
contains the
searched term
contract name.
1,2,4,5,6 do not
contain the term
completely.
4 Name Contract 6 Only document 6
contain a searched
term name
contract.
1,2,3,4,5 do not
contain the term
5 Con 1,2,3,6 1,2,3,6 contain the
term.
4,5 do not contain
the term.
6 Either "Col" or
"COL" or "cOL" etc.
4,5 4,5 contain the
term.
1,2,3,6 do not
contain the term.
7 ridiculously has a - No documents will
be matched. For
document 3 the
search terms are
correct but in a
wrong order.
-
8 Hi I am Al - There is no string
like this in the
index.
-
9 012 2,5,6 1,6 matched
completely, 5 was
found in value
301200.
10 Either "66.6" or
"66." or "66" or
"6.6"
3 . is not considered
special characters,
therefore it can be
used in search as
is.
-
108

-- 108 of 334 --

Use-case number Search term Documents
returned
Justification Remark
11 ` 301 ` - Because
whitespaces are
handled as regular
characters.
-
12 2021- 1 This value is found
in the field
createdAt of
document 1.
-
13 01-10-2024 0 Searched term
does not have the
same format as it
was created in.
Therefore,
document 4 is not
matched.
-
14 Wes 2,3 The term Wes
would be found in
the localization
values Bankwesen
and in
Gesundheitswesen,
which are
assigned via
Banking and
Healthcare to
documents 2,3, if
the fields
property refers to
the Industry field
and German
localization was
provided for the
Query RPC
request.
If fields is
missing, the
documents would
match regardless
of the localization
of the user.
Even US localized
user would get
match from de
localized
enumeration if
fields would be
missing.
109

-- 109 of 334 --

Logic Operators
In the Query API, there are three logic operators available to combine different conditions in a
query:
1. and: The and operator takes two or more operands, and all operands must match for the query to
match. It is used when you want to enforce multiple conditions that must all be satisfied.
2. or: The or operator also takes two or more operands, but at least one operand must match for
the query to match. It is used when you want to allow any of the conditions to satisfy the query.
3. not: The not operator takes a single operand, and the operand must not match for the query to
match. This is used for negation, where you want to exclude certain results.
Below is an example of a complex query that combines logic operators (and, or, not) with field-
aware operators. These examples illustrate how you can build queries that require multiple
conditions to be matched or excluded.
In this example, we want to find all BusinessPartner documents where:
• The Name is equal to "Ludovici".
• The Industry is "Technology".
• The Country is "Italy".
We use the and operator because all conditions must match.
{
"query": {
"targetDocumentModel": "BusinessPartner",
"constraint": {
"operator": "and",
"operands": [
{
"operator": "exact_match",
"field": "/BusinessPartnerRoot/Name",
"value": "Ludovici"
},
{
"operator": "exact_match",
"field": "/BusinessPartnerRoot/Industry",
"value": "Technology"
},
{
"operator": "exact_match",
"field": "/BusinessPartnerRoot/Country",
"value": "Italy"
}
]
},
"projectionName": "document",
"paging": {
110

-- 110 of 334 --

"pageSize": 10,
"pageNumber": 1
}
}
}
Explanation: This query will return documents where the Name field matches "Ludovici", the
Industry field matches "Technology", and the Country field matches "Italy". All three conditions
must be true for the document to be returned.
Now, let’s say we want to find BusinessPartner documents where:
• The Industry is either "Technology" or "Healthcare".
• The Country is not "Italy".
We use the or, and, not operators to create a query:
Example of complex query with and, or, not operators
{
"query": {
"targetDocumentModel": "BusinessPartner",
"constraint": {
"operator": "and",
"operands": [
{
"operator": "or",
"operands": [
{
"operator": "exact_match",
"field": "/BusinessPartnerRoot/Industry",
"value": "Technology"
},
{
"operator": "exact_match",
"field": "/BusinessPartnerRoot/Industry",
"value": "Healthcare"
}
]
},
{
"operator": "not",
"operand": {
"operator": "exact_match",
"field": "/BusinessPartnerRoot/Country",
"value": "Italy"
}
}
]
},
111

-- 111 of 334 --

"projectionName": "document",
"paging": {
"pageSize": 10,
"pageNumber": 1
}
}
}
Explanation: This query will return documents where the Industry is either "Technology" or
"Healthcare" and the Country is not "Italy". The or operator allows flexibility in the condition, and
the and operator ensures that the Country is never "Italy".
The exact_match operator was used in all queries for simplicity but any field-aware operator could
be used instead.
Has Operator
The has operator is used to find documents that are linked to other documents through a
relationship model (RM) at the current level. This operator is helpful when working with
documents that have links to other documents in the system, and it allows querying based on the
properties of these linked documents.
The has operator operates on the following parameters:
1. relationshipModel (Mandatory): This is a reference to the relationship model (RM) that defines
the link between two document models (DMs). The user must have the necessary permissions to
use this RM.
2. targetRole (Mandatory): This refers to the role of the target document in the relationship model
(RM). It specifies which side of the relationship is being queried. The targetRole points to the
document model (DM) of the target side of the relationship. This DM will be enriched by actual
DMs from the model graph, and the enriched model will be used as the targetDocumentModel of
the parent for constraints in the query.
3. constraint (Optional): The main query constraint is applied to the target documents identified
by the targetRole in the RM. This constraint can be any logic or field-aware operator, such as
exact_match, that applies to the fields of the target document model (DM).
4. linkDocumentConstraint (Optional): This constraint is applied to the link document itself, i.e., the
document that represents the relationship between the two linked documents. The
linkDocumentConstraint can also include field-aware operators that apply to the fields of the link
document. The targetDocumentModel for the linkDocumentConstraint is automatically determined
based on the relationshipModel and is set to the DM specified in the RM for the link document.
The following example illustrates how the has operator is used in a query to find BusinessPartner
documents that are linked to HomeInsurance documents (through the PolicyHolder relationship
model), where the insurer’s name is "ING" and the link document has a non-null TerminatedAt field.
Example of has operator
{
112

-- 112 of 334 --

"targetDocumentModel": "BusinessPartner",
"constraint": {
"operator": "and",
"operands": [
{
"operator": "exact_match",
"field": "/BusinessPartner/Name",
"value": "Tomas"
},
{
"operator": "has",
"relationshipModel": "PolicyHolder",
"targetRole": "Contract",
"constraint": {
"operator": "exact_match",
"field": "/HomeInsurance/Insurer/Name",
"value": "ING"
},
"linkDocumentConstraint": {
"operator": "undefined_match",
"field": "/InsuranceLinkFields/TerminatedAt"
}
}
]
}
}
Explanation of the Example:
• targetDocumentModel: The root document model is BusinessPartner. All the fields in the query
must be from this model (or its subtypes).
• First Operand of and Operator (exact_match): The first operand checks if the Name of the
BusinessPartner document is equal to Tomas. The query will match only documents from the
BusinessPartner DM (or its subtypes).
• Second Operand of and Operator (has): The second operand uses the has operator to find linked
HomeInsurance documents through the PolicyHolder relationship model. Specifically, it looks
for linked documents where:
◦ The /HomeInsurance/Insurer/Name of the HomeInsurance document is ING.
◦ The link document (represented by the InsuranceLinkFields model) has a non-null
/InsuranceLinkFields/TerminatedAt field. In this example, the linkDocumentConstraint is
checking this field.
The has operator cannot create relationships between link documents (e.g., InsuranceLinkFields)
and other document models (e.g., HomeInsurance) outside of the relationship model. Therefore,
the linkDocumentConstraint cannot apply to relationships that involve link documents directly
connected to other DMs.
113

-- 113 of 334 --

Exclude Option
The exclude property in the query root is used to exclude all root documents from being included
in the selection results while still allowing those documents to be included in projections if defined.
This property offers flexibility when constructing complex queries where certain root documents
are not required in the selection but may still be relevant for linked data in projections.
By default exclude is set to false. If it is set to true means there is no ROOT document in query
response entries section, and pagination in query request will be applied to CHILD documents in
query response links section. (See the Paging section below for more information.)
Example query request with exclude is true:
[
{
"jsonrpc": "2.0",
"id": "QueryLink",
"method": "QUERY",
"params": {
"query": {
"projectionName": "document",
"exclude": true,
"targetDocumentModel": "Contract",
"constraint": {
"operator": "exact_match",
"field": "/__meta/docRef",
"value": "$(CONTRACT_DOC_REF)"
},
"links": [
{
"relationshipModel": "ContractCoInsuredPartner",
"targetRole": "Partner"
}
],
"paging": {
"pageNumber": 0,
"pageSize": 2
}
}
}
}
]
Example query response with pagination is applied to links section when exclude is true. Full size is
three but there are only two CHILD nodes and related two LINK documents in links list while entries
property is empty:
[
{
114

-- 114 of 334 --

"jsonrpc": "2.0",
"id": "QueryLink",
"result": {
"fullSize": 3,
"page": {
"pageNumber": 0,
"pageSize": 2
},
"entries": [
],
"links": [
{
"docRef": "BusinessPartner/c04ae5d6-285a-40d7-94f3-9e42a8375fe0",
"relationshipModel": "ContractCoInsuredPartner",
"sourceRole": "Contract",
"sourceDocRef": "Contract/777f9273-575d-4181-8a2f-2021fa4796a3",
"targetRole": "Partner",
"targetDocRef": "BusinessPartner/c04ae5d6-285a-40d7-94f3-9e42a8375fe0",
"document": {
"BusinessPartnerRoot": {
"Industry": "IT",
"PersonOrEntity": "Natural Person",
"TimeOfContractSignature": "23:59:59",
"StartOfRelationship": "2022-06-01",
"CustomerDiscount": "90%",
"Name": "Eva",
"Employment": {
"income": 3333.33,
"signingDateTime": "2020-12-31T23:59:59"
},
"SubtypeGroup": {
"Company": "None"
},
"Attachment": [
{
"original_filename": "Attachment4",
"size": 100,
"mime_type": "image/jpg",
"internal_filename": "AttachmentInternal4",
"description": "Category empty string",
"category": "",
"content": "content"
},
{
"original_filename": "ExpectedAttachment",
"size": 100,
"mime_type": "image/jpg",
"internal_filename": "ExpectedAttachment4",
"description": "Expected attachment to be found",
"category": "Expected Category",
115

-- 115 of 334 --

"content": "ExpectedContent"
}
]
},
"__meta": {
"createdAt": "2025-04-22T06:19:06",
"creator": "admin",
"docRef": "BusinessPartner/c04ae5d6-285a-40d7-94f3-9e42a8375fe0",
"modelReference": "BusinessPartner",
"modelVersion": null,
"modifiedAt": "2025-04-22T06:19:06",
"modifier": "admin"
}
},
"type": "CHILD",
"linkId": "44391b72-ef33-4926-aeda-8ccd03cd6bce",
"depth": 0,
"documentModelName": "BusinessPartner"
},
{
"docRef": "BusinessPartner/194d55bb-7c19-4061-8b70-dfba3edd8e1e",
"relationshipModel": "ContractCoInsuredPartner",
"sourceRole": "Contract",
"sourceDocRef": "Contract/777f9273-575d-4181-8a2f-2021fa4796a3",
"targetRole": "Partner",
"targetDocRef": "BusinessPartner/194d55bb-7c19-4061-8b70-dfba3edd8e1e",
"document": {
"BusinessPartnerRoot": {
"Industry": "IT",
"PersonOrEntity": "Natural Person",
"PremiumPartner": true,
"TimeOfContractSignature": "15:00:00",
"StartOfRelationship": "2022-07-01",
"CustomerDiscount": "90%",
"Name": "Konstantin",
"Employment": {
"income": 0.01,
"signingDateTime": "2021-06-30T15:00:00"
},
"SubtypeGroup": {
"Company": "None"
},
"Attachment": [
{
"original_filename": "Attachment2",
"size": 300,
"mime_type": "image/jpg",
"internal_filename": "AttachmentInternal2",
"description": "Category equals null",
"category": null,
"content": "content"
116

-- 116 of 334 --

}
]
},
"__meta": {
"createdAt": "2025-04-22T06:19:06",
"creator": "admin",
"docRef": "BusinessPartner/194d55bb-7c19-4061-8b70-dfba3edd8e1e",
"modelReference": "BusinessPartner",
"modelVersion": null,
"modifiedAt": "2025-04-22T06:19:06",
"modifier": "admin"
}
},
"type": "CHILD",
"linkId": "002a66fc-9528-4238-8d91-ad6e896d08ed",
"depth": 0,
"documentModelName": "BusinessPartner"
},
{
"docRef": "CoInsuredAdditionalFields/1bac1274-c252-4299-967f-19d05ca1a9b5",
"relationshipModel": "ContractCoInsuredPartner",
"sourceRole": "Contract",
"sourceDocRef": "Contract/777f9273-575d-4181-8a2f-2021fa4796a3",
"targetRole": "Partner",
"targetDocRef": "CoInsuredAdditionalFields/1bac1274-c252-4299-967f-
19d05ca1a9b5",
"document": {
"CoInsuredRoot": {
"Name": "Alexander"
},
"__meta": {
"createdAt": "2025-04-22T06:19:12",
"creator": "admin",
"docRef": "CoInsuredAdditionalFields/1bac1274-c252-4299-967f-
19d05ca1a9b5",
"modelReference": "CoInsuredAdditionalFields",
"modelVersion": null,
"modifiedAt": "2025-04-22T06:19:12",
"modifier": "admin"
}
},
"type": "LINK",
"linkId": "44391b72-ef33-4926-aeda-8ccd03cd6bce",
"depth": 0,
"documentModelName": "CoInsuredAdditionalFields"
},
{
"docRef": "CoInsuredAdditionalFields/659252c5-919f-4f57-b0ca-1894d918a103",
"relationshipModel": "ContractCoInsuredPartner",
"sourceRole": "Contract",
"sourceDocRef": "Contract/777f9273-575d-4181-8a2f-2021fa4796a3",
117

-- 117 of 334 --

"targetRole": "Partner",
"targetDocRef": "CoInsuredAdditionalFields/659252c5-919f-4f57-b0ca-
1894d918a103",
"document": {
"CoInsuredRoot": {
"Role": "EXP",
"Name": "Otto"
},
"__meta": {
"createdAt": "2025-04-22T06:19:12",
"creator": "admin",
"docRef": "CoInsuredAdditionalFields/659252c5-919f-4f57-b0ca-
1894d918a103",
"modelReference": "CoInsuredAdditionalFields",
"modelVersion": null,
"modifiedAt": "2025-04-22T06:19:12",
"modifier": "admin"
}
},
"type": "LINK",
"linkId": "002a66fc-9528-4238-8d91-ad6e896d08ed",
"depth": 0,
"documentModelName": "CoInsuredAdditionalFields"
}
],
"otherResults": {
}
}
}
]
{
"query": {
"targetDocumentModel": "BusinessPartner",
"exclude": true,
"links": [
{
"relationshipModel": "PolicyHolder",
"targetRole": "Contract"
}
]
}
}
The exclude property is particularly useful in scenarios where:
1. Root documents are required for projection purposes but should not clutter the selection
results.
118

-- 118 of 334 --

2. Query performance optimization: By excluding unnecessary root documents from the
selection, the query performance can be optimized, especially when the root documents are
large or numerous, but their inclusion in the result set isn’t needed.
3. Links need to be paged. Enabling this property will push paging specification to links property
because it is unnecessary for entries since they are skipped.
The exclusion affects only the selection phase of the query. If the excluded documents are
referenced in projections (via linked relationships or other document dependencies), they will still
appear in those projections.

Supported scope of paging push-down with exclude
When exclude is true, paging is pushed from entries to links only under the
following conditions:
• Single top-level link: The query root must contain exactly one top-level link
entry. When the root has multiple top-level links, paging push-down does not
apply.
• Single nesting depth: Paging push-down applies only to the top-level link.
Nested links (second level and deeper) are not scoped to the paged top-level
result. As a consequence, when the top-level link is paged with exclude=true
and contains nested links:
◦ Nested-link CHILD entries may reference source documents that are not on
the current page of the top-level link.
◦ The LinkFields documents (LINK entries) for nested links may be missing
from the response.
If a use case requires exclude=true together with nested links, retrieve the nested
data with separate follow-up queries (one per page entry of the top-level link), or
do not use exclude and consume the unpaged projection result.
Projection
Once the root documents are resolved, the projection specification defines the additional data
that should be loaded alongside the root document metadata. Additionally, the projection allows
you to customize the structure of the query results by specifying which parts of the data to retrieve,
whether it’s the complete document, a subset of fields, or even a custom data graph.
There are several options for projecting data:
1. Complete Documents: You can choose to load the full root documents. This is also applicable in
nested links properties.
2. Subset of Fields: Instead of loading the entire document, you can specify which fields of the
document should be returned. This is useful when you only need specific pieces of information
from a document, reducing the payload size and improving query performance.
3. Constructing CDD (Composed Data Document): Projection can be used to construct a CDD
based on the CDM (Composed Document Model) definition. This allows you to retrieve a
119

-- 119 of 334 --

structured document that conforms to the CDM, with links between related documents
represented according to the CDM.
4. Custom Data Graph: For more complex use cases, projection allows the creation of a custom
data graph. This means you can retrieve documents linked together by relationship definitions
other than those specified in the CDM, offering a flexible way to represent the data structure in
the query results.
It is possible to write a custom projection. See the extension points section below for more info.
Pagination and Projection
It’s important to note that pagination is applied only to the selection phase of the query (i.e.,
determining which documents are included in the result set). The projection, on the other hand,
does not involve pagination. All projected data will be returned in full, regardless of the number of
documents selected. This ensures that the query result contains all the necessary data linked to the
root documents, without the additional complexity of paginating the projected content. Number of
links and documents projected must be restricted in entityCharacteristics#upperLimit property of a
relationship model.
The Query API enables various projections for the selected roots. DS provides four built-in
projections:
Projection Description projectionName
Document Projection Offers different methods for
loading selected documents.
document
CDD Projection Generates CDDs as the query
result instead of a data graph.
cdd
Document Graph Projection Utilizes CDM to create a
targeted data graph.
document-graph
Export CDD Projection Generates a link to a CSV export
stored in content store based on
the query specification. The
query must be based on the
CDM.
exportCddCsv
Document Projection
Document projection defines how the data related to a root document is retrieved. It can either
involve loading the entire document from the persistent store during the Post-execution phase, or
constructing the document using specific fields from the document_search table. This flexibility
allows for more efficient data retrieval based on the query’s needs.
The document projection is enabled by providing document value in the projectionName property in
the query root.
120

-- 120 of 334 --

Fields Property
One way to optimize the query and reduce a data load is by using Fields property. Instead of
loading the entire document, you can specify a subset of fields to be returned. This is achieved by
providing the fields property in the query root.
Key characteristics of Fields property:
1. Indexed Fields: Only fields that are indexed can be used in fields projection.
a. By default, all fields are indexed.
2. Subset of Fields: The fields property allows you to specify exactly which fields from the root
document (or its subtypes) should be included in the result. This approach reduces the amount
of data returned, optimizing performance and avoiding over-fetching data. This is particularly
useful when you want to retrieve a specific portion of the data from the document without the
overhead of loading the entire document.
3. Subtypes and Field Availability: If you specify fields in a subtype of the targetDocumentModel,
some documents might not contain those fields due to different subtype could be defined for
those documents.
Imagine a query where you only need the name and address of a customer from a larger
BusinessPartner document. Instead of retrieving the entire document (which might include orders,
payment details, etc.), you can specify the fields property to only load the name, id and Company
fields:
{
"targetDocumentModel": "Customer",
"fields": ["/BusinessPartnerRoot/Name", "/BusinessPartnerRoot/id",
"/BusinessPartnerRoot/SubtypeGroup/Company"]
}
In this case, the query will return just the name and id of the BusinessPartner, reducing the amount
of data retrieved and improving performance. If a matched document will be of
BusinessPartnerCompany DM (subtype of BusinessPartner DM), the field
/BusinessPartnerRoot/SubtypeGroup/Company will be projected to the result as well.
The implementation expects the values to be un-encrypted. If the field is encrypted, the query will
return encrypted values. The custom code in the post-execution phase needs to decrypt the value.
Links Property
When using the links property in a query, the goal is to return additional related documents,
connected through a relationship model (RM) and target role defined in the query. These additional
documents (links) are retrieved based on the selection of a root document and its relationships. The
links property is used to specify how documents should be linked, and any constraints or
restrictions to be applied on those links.
Each link definition in the links property has the following parameters:
121

-- 121 of 334 --

1. relationshipModel (Mandatory): Specifies the relationship model (RM) that should be used to
find related documents. This value must be a valid string that refers to a specific RM.
2. targetRole (Mandatory): Defines the target role in the relationship that should be linked to the
root document. Due to self-referencing relationships, this role cannot be inferred from the
context and must be explicitly provided for all RMs
3. constraint (Optional): Defines the constraints that should be applied to the links (target
document). This is similar to the constraint property in the root query and allows you to filter
links based on specific conditions. If no constraint is provided, all links will be returned without
restrictions.
4. linkDocumentConstraint (Optional): Defines the constraint that should be applied to a link
document of a link. If no linkDocumentConstraint is provided, all links will be returned without
restrictions. This property is only allowed if the relationship defined in relationshipModel has a
Link Document Model defined.
5. fields (Optional): The property allows partially loading document fields without a need to load
a complete document. The fields property is applicable to the target documents of the link. The
behavior is the same as in the Query Root.
6. linkDocumentFields (Optional): The property allows partially loading only desired fields from a
link document (if the relationship allows it). The behavior is the same as in the Query Root.
7. links (Optional): Allows for further linking to additional documents from the already retrieved
links. This creates a recursive or nested linking structure where additional links can be
specified for the previously linked documents.
In this example, we retrieve all documents of type Contract and their related documents through
the Partner role of the PolicyHolder RM:
{
"query": {
"targetDocumentModel": "Contract",
"links": [
{
"relationshipModel": "PolicyHolder",
"targetRole": "Partner"
}
]
}
}
This query will return the first page of all Contract documents and all their associated Partner
documents, based on the PolicyHolder RM. The links will be established between the Contract
documents and the related BusinessPartner documents defined by the Partner role in the
PolicyHolder relationship model.
In a more complex scenario, you might want to apply specific constraints to both the root document
and the linked documents. In the example below, we first select Contract documents and link to
Partner documents where the /BusinessPartner/Name field is "Ludovici". Then, for the related
Partner documents, we link further documents through the PermanentAddress RM where the
122

-- 122 of 334 --

Address/City is "Berlin":
{
"query": {
"targetDocumentModel": "Contract",
"links": [
{
"relationshipModel": "PolicyHolder",
"targetRole": "Partner",
"constraint" : {
"operator": "exact_match",
"field": "/BusinessPartner/Name",
"value": "Ludovici"
},
"links" : [
{
"relationshipModel": "PermanentAddress",
"targetRole": "Address",
"constraint" : {
"operator": "exact_match",
"field": "/Address/City",
"value": "Berlin"
}
}
]
}
]
}
}
1. First, the query retrieves all Contract documents from the database.
2. Then, it finds all Partner documents related to the Contract documents through the PolicyHolder
RM, but only those Partner documents where the /BusinessPartner/Name field is "Ludovici".
3. Next, for each of the Partner documents that were retrieved, the query finds the related Address
documents via the PermanentAddress RM where the /Address/City field is "Berlin".
It is also possible to select only certain fields from the documents retrieved by links projections.
{
"query": {
"targetDocumentModel": "Contract",
"projectionName" : "document",
"fields": ["/Contract/Name", "/__meta/docRef"],
"links": [
{
"relationshipModel": "PolicyHolder",
"targetRole": "Partner",
"fields": ["/PartnerInfo/Name", "/__meta/docRef"],
123

-- 123 of 334 --

"links" : [
{
"relationshipModel": "PermanentAddress",
"targetRole": "Address",
"fields": ["/Address/City", "/Address/Street", "/Address/ZIP"]
}
]
}
]
}
}
In this example, only a few properties from the Contract, Partner, and Address documents will be
loaded. The fields must be defined for each link definition; otherwise, the entire document will be
loaded, potentially impacting query performance.
Dynamic Link Retrieval
In the links section, it is possible to define self-referencing relationship models. For such link
definitions, DS requires the maxDepth property to be specified, as it will recursively traverse the
relationship. To prevent potential DoS attacks, DS enforces a hard limit on the maximum depth that
can be loaded in a single query. The maxDepth provided in the query must be below this limit to pass
the validation phase.
{
"jsonrpc": "2.0",
"id": "LOAD_ALL_AMENDMENTS_OF_CONTRACT",
"method": "QUERY",
"params": {
"query": {
"targetDocumentModel": "Contract",
"projectionName": "document",
"constraint":{
"operator": "exact_match",
"field": "/__meta/docRef",
"value": "Contract/1"
},
"links": [
{
"relationshipModel": "ContractAmendment",
"targetRole": "Amendment",
"maxDepth" : 10
}
],
"paging": {
"pageNumber": 0,
"pageSize": 1
}
}
}
124

-- 124 of 334 --

}
The query above will generate a tree starting with Contract/1 and extending up to a maximum
depth of 10. It’s important to note that more than 10 links may be found, each level of the tree could
contain many amendments, depending on how many documents are linked via ContractAmendment
RM. Besides several amendments, the query may return also link documents (if the relationship
model allows them).
Additionally, the paging specification in the query only applies to the root selection and not the
projection. Therefore, specifying a pageSize of 1 ensures that only a single root is selected, but it
does not limit the number of documents loaded through the links section.
Queries using dynamic link projections can potentially load huge results. To ensure acceptable
query performance, DS recommends the following best practices:
• Know your data: DS cannot predict the data structure in customer projects. If your trees are
deep but narrow, you may not experience issues. However, if you have vast trees, consider
loading 2–3 levels at a time or avoid loading entire trees at once.
• Avoid loading forests: The query’s selection section allows you to specify (or omit) which roots
will be selected. By executing a query like this, you’re loading trees for each root selected.
Ensure your selection is as specific as possible to meet your use case. For larger trees, consider
issuing multiple queries per root.
• Avoid over-fetching: Use the fields property to load only the necessary fields for your use case,
rather than loading complete documents. For most tree overviews, complete documents are
unnecessary. You can load the full document later if the user requests it.
Document Graph Projection
The Document Graph projection leverages the CDM feature in conjunction with the fields property
to load document graphs, which can then be used on the client side to construct CDDs. This
projection provides a single document graph for the form engine. Technically, it is possible to load
more than one graph, but there is no known use-case so far.
The document projection is enabled by providing document-graph value in the projectionName
property in the query root.
Example of document-graph projection
{
"query": {
"projectionName": "document-graph",
"targetDocumentModel": "ContractCDM",
"fields": ["/ContractBusinessPartner"],
"constraint" : {
"operator": "exact_match",
"field": "/__meta/docRef",
"value": "Contract/169c57d2-ad32-4bf8-b533-c108c0f5c6cf"
},
"paging": {
125

-- 125 of 334 --

"pageNumber": 0,
"pageSize": 1
}
}
}
The projection will transform the query into one that utilizes links properties, based on the CDM
specification provided in the targetDocumentModel. The following transformations will occur:
1. targetDocumentModel will be adjusted:
a. If the fields property is empty, the model will be set to the Root Document Model of the
CDM.
b. If the fields property is defined, the model will be set to the Document Model (DM) of the
target role found in the fields property.
2. fields property: The fields property will be removed. The selected fields will be determined
from the CDM specified in the original targetDocumentModel. The fields property of document-
graph projection does not expect document fields but groups which are filled via CDM
relationship annotations.
3. links section: This section will be added based on the CDM specification from the original
targetDocumentModel. If the underlying relationship is of self-referencing type, the maxDepth
property of this link is programmatically set to 1. links can also be added to the query by the
caller. For more info about the implications please see this section.
It’s important to note that the constraints apply to the Contract document, not the CDD, even though
the targetDocumentModel refers to the CDM. This is because the constraints are enforced after the
transformation, which occurs during the initial step of query execution (executed in the preprocess
method of the projection implementation). Ensure that the constraints applied will still be valid
after the transformation.
To optimize performance, unnecessary root documents can be excluded from the query using the
exclude option, preventing unnecessary data from being loaded.
CDD Projection
The CDD projection expects a CDM in the targetDocumentModel and constructs CDDs instead of a
document graph. The query below will load the first 100 ContractCDM CDDs that have a Health
Insurance value in /ContractRoot/Type. The result will not include any documents in the links
section. Only the root entries will be returned, as a CDD will be constructed for each root that
matches the query, based on the CDM specification.
Example of CDD projection
{
"jsonrpc": "2.0",
"id": "LIST_CDDS_USE_CASE",
"method": "QUERY",
"params": {
"query": {
126

-- 126 of 334 --

"targetDocumentModel": "ContractCDM",
"projectionName": "cdd",
"constraint": {
"operator": "exact_match",
"field": "/ContractRoot/Type",
"value": "Health Insurance"
},
"paging": {
"pageNumber": 0,
"pageSize": 100
}
}
}
}
The CDD projection is activated by specifying the cdd value in the projectionName property within
the query root. Using links and fields is possible but not necessary, as the CDD will be constructed
based on the CDM definition. For more information please see the documentation below.
The CDD projections have the following restrictions:
• Sorting is only possible on non-repeatable fields: In CDD projections, every group mapped
via a relationship is repeatable. Fields within a repeatable group will have multiple values for a
single document. As a result, it is not possible to sort based on any fields of repeatable groups.
This same restriction applies to other projections as well, but in the case of CDD, linked
documents are also affected.
• Projection for mapped documents must use the has operator: The Query API does not allow
CDM field paths. To restrict access to linked documents, the has operator must be used with the
original field path. Since overviews do not support filtering by repeatable fields (including
mapped fields), this is a feature parity issue. Refer to the example below for clarification. Using
exact_match operator on the field defined in CDM will not work in this case without has operator
because the query API is not aware of CDM field paths. This implies that the caller needs to
provide the original field path.
• Self-referencing relationships are followed to the first level only: If a relationship inside a
CDM is of self-referencing type (i.e. source model and target model are the same), only the first
child document is added to the CDD.
Constraint on linked document
{
"jsonrpc": "2.0",
"id": "CONSTRAINT_ON_LINKED_DOCUMENT_IN_CDD",
"method": "QUERY",
"params": {
"query": {
"targetDocumentModel": "ContractCDM",
"projectionName": "cdd",
"constraint": {
"operator": "has",
127

-- 127 of 334 --

"relationshipModel": "PolicyHolder",
"targetRole": "Partner",
"constraint": {
"operator": "exact_match",
"field" : "/Partner/FirstName",
"value": "Ludovici"
}
},
"paging": {
"pageNumber": 0,
"pageSize": 100
}
}
}
}
• The SIMPLE_SEARCH operator only works on root documents: The has operator must be used in
combination with the SIMPLE_SEARCH operator to match results on the complete CDD, especially
when using the or operator. Refer to the example below for details.
SIMPLE_SEARCH on complete CDD
{
"jsonrpc": "2.0",
"id": "SIMPLE_SEARCH_COMPLETE_CDD",
"method": "QUERY",
"params": {
"query": {
"targetDocumentModel": "ContractCDM",
"projectionName": "cdd",
"constraint": {
"operator": "or",
"operands": [
{
"operator": "simple_search",
"value": "Ludovici"
},
{
"operator": "has",
"relationshipModel": "PoliciyHolder",
"targetRole": "Partner",
"constraint": {
"operator": "or",
"operands": [
{
"operator": "simple_search",
"value": "Ludovici"
},
{
"operator": "has",
128

-- 128 of 334 --

"relationshipModel": "PermanentAddress",
"targetRole": "Address",
"constraint": {
"operator": "simple_search",
"value": "Ludovici"
}
}
]
}
}
]
},
"paging": {
"pageNumber": 0,
"pageSize": 100
}
}
}
}
• Computed fields cannot be used in sorting or in constraints: Computed fields are not filled
by default in cdd projection. This can be changed using
mgmtp.a12.dataservices.query.cdd.computation.enabledForModels configuration key. Mentioned
configuration key will fill computed values in the postprocess method of the cdd projection,
therefore these values will not be available during query time. This means that computed
values cannot be used in constraints or in sorting specifications. Non-transient computed fields
can be still used in constraints and sorting in root document models of cdd projection.
You can specify links in the cdd projection, but they must exactly match the links defined in the
CDM. This feature allows you to define constraints on linked documents in your query, since
constraints cannot be set directly in the CDM. Note that the links section in the cdd projection does
not load additional documents; it is used solely to apply constraints to the linked documents as
defined by the CDM. DS will not validate these links. If they do not match the CDM, the query will
still execute, but the results may not be as expected.
links Property In cdd and document-graph Projections
If you explicitly provide a links section, DS uses it verbatim. If you omit it, DS derives the links
section from the CDM during preprocessing.
Typical reasons to define links manually:
• Omit relationship groups you do not need (to reduce payload & latency) without creating a new
CDM.
• Add constraints to specific linked documents or link documents to limit which data goes to the
client.
• Restrict loaded fields via fields / linkDocumentFields to avoid full document loading.
◦ This is mainly needed for the cdd projection to keep performance acceptable. The document-
129

-- 129 of 334 --

graph projection loads full documents by default.
When defining links manually, ensure they align with the CDM. If a relationship present in the
CDM is omitted, it will not appear in the result. For each link specified in this way DS will require
property backReference to be filled. This property defines how the link is referenced in the parent
document. The value must point to the group in the CDM where the relationship is defined. DS
cannot guest this value reliably because the same relationship model could be used in multiple
groups in the CDM.
Example of links property in cdd projection
{
"jsonrpc": "2.0",
"id": "CddWithLinksRestricted",
"method": "QUERY",
"params": {
"query": {
"projectionName": "cdd",
"targetDocumentModel": "ContractCDM",
"constraint": {
"operator": "exact_match",
"field": "/__meta/docRef",
"value": "Contract/777f9273-575d-4181-8a2f-2021fa4796a3"
},
"links": [
{
"relationshipModel": "ContractBusinessPartner",
"targetDocumentModel": "BusinessPartnerSuper",
"targetRole": "Partner",
"backReference": "/ContractBusinessPartner",
"links": [
{
"relationshipModel": "PartnerPostalAddress",
"targetDocumentModel": "Address",
"sourceRole": "Partner",
"targetRole": "Address",
"backReference":
"/ContractBusinessPartner/PartnerPostalAddress"
}
]
},
{
"relationshipModel": "ContractCoInsuredPartner",
"targetDocumentModel": "BusinessPartnerSuper",
"targetRole": "Partner",
"backReference": "/ContractCoInsuredPartner"
}
],
"paging": {
"pageSize": 100,
"pageNumber": 0
130

-- 130 of 334 --

}
}
}
}
In the example above we see that ContractCDM has 2 root level relationships defined:
ContractBusinessPartner and ContractCoInsuredPartner. Both are included in the links section. The
ContractBusinessPartner relationship has a nested relationship PartnerPostalAddress, which is also
included in the links section. The backReference properties point to the corresponding groups in the
CDM where these relationships are defined.
Caution: DS does not validate that supplied links match the CDM. Incorrect or partial specifications
will still execute but may yield incomplete or misleading results. Validation of correctness is the
caller’s responsibility.
Export CDD Projection
The exportCddCsv projection expects a CDM in the targetDocumentModel, constructs CDDs with
supporting sort and constraint, converts them to a file, and saves the file to the Content Store. It
then returns a downloadUrl in otherResults. Refer to the example below for details.
Export CDD
{
"jsonrpc": "2.0",
"id": "EXPORT_CDD_CSV",
"method": "QUERY",
"params": {
"query": {
"targetDocumentModel": "ContractCDM",
"projectionName": "exportCddCsv",
"paging": {
"pageNumber": 0,
"pageSize": 100
}
}
}
}
Overwriting Existing Projections
If project requirements necessitate changing the behavior of existing projections or adding new
ones, implement the IQueryProjection interface and define a projectionName. A non‑existing
projectionName registers a new projection (clients must then explicitly request it). To override an
existing projection, reuse its projectionName and supply a higher precedence (e.g. lower numeric
value) via the @Order annotation—your implementation will take precedence. Overriding is
discouraged because it can introduce unexpected client behavior and block upstream fixes and
improvements.
131

-- 131 of 334 --

Prefer adding a new projection unless you fully control all consumers. See the examples module for
a reference implementation.
Aggregations
The Query API supports aggregations for two main use cases:
Query Construction Support:
The engines provide a smooth user experience for users creating queries in the browser.
Aggregation functions assist in constructing constraints that generate meaningful results,
preventing the creation of constraints that would eliminate all results from the result set. Fast
suggestions as users type are crucial. To achieve this, DS introduces a separate endpoint, POST
/api/aggregation, which takes the query as the request body. This endpoint is optimized for
performance, as it avoids complicated transactional handling, batching, or JSON-RPC wrappers.

The query result may be improper, if any of the aggregation function fields or
grouping fields specified in the query request do not exist in the target model. If a
field is misspelled, or if it has been renamed, or it does not exist in the model, the
query execution will return incorrect result.
Statistical Information About Results:
The engines allow the retrieval of additional statistical information about the current result set.
This data can be displayed in dashboards or simple charts for analysis.
Currently, Query API aggregations are not designed for large-scale aggregations over massive
datasets intended for complex reporting use cases. The full reporting use case has not been
considered for the Query API at this time.
Aggregation Functions
There are five aggregation functions available in DS:
• avg: Computes the average value for a field. This function is available for INumberType.
• min: Computes the minimum value for a field. This function is available for INumberType,
IDateType, IDateTimeType, and ITimeType.
• max: Computes the maximum value for a field. This function is available for INumberType,
IDateType, IDateTimeType, and ITimeType.
• sum: Computes the sum of all values for a field, allowed only for INumberType.
• count: Counts unique occurrences of a value in a field. This aggregation function is supported
for all kernel data types.
To provide specific aggregated results, the query may group the results by fields.
Aggregation example
{
"query": {
132

-- 132 of 334 --

"targetDocumentModel": "Contract",
"aggregation": {
"aggregations": [
{
"function": "sum",
"field": "/ContractRoot/Liability"
}
],
"group": [
{
"field": "/ContractRoot/Type"
}
]
}
}
}
In the example, the results are grouped by field /ContractRoot/Type and for each group the sum of
the values in /ContractRoot/Costs is provided. Result will contain generated document with the
group.
If the group property is not specified, the aggregation will be applied to the entire result set, and the
result will contain a single document with the aggregated value.
Restrictions
• One-Level Support: Aggregations currently do not support nesting. This means that only a
single level of aggregation is allowed. However, constraints from grouped results can be passed
as query constraints to simulate additional levels of aggregation. In the case of more complex,
multi-level aggregations, JSON-RPC can be used to issue multiple QUERY operations in a single
request, effectively simulating multi-level aggregation.
• No has Operator Support: Aggregations do not work over linked documents, which means it is
not possible to group by properties of links or linked documents. The aggregation is limited to
the fields of the root documents and cannot traverse or aggregate based on linked data.
• Cannot Be Combined with Regular Queries: Aggregations work by grouping results, and as a
result, they cannot be combined with regular queries that are not grouped. This limitation
means that aggregation results must be handled separately from queries that retrieve
ungrouped or non-aggregated data.
• No links Property is Allowed: The links property requires roots that can be used in links.
Aggregated results do not contain the root documents but rather aggregations. Currently it is
not possible to join any data to the aggregated result.
Endpoint /api/aggregation Support
The aggregation endpoint can be accessed via the POST HTTP method, with the query definition
provided in the request body. The response is a two-dimensional array, returning both the groups
and their corresponding aggregation function results.
133

-- 133 of 334 --

This endpoint is designed specifically for aggregation use cases and does not support any other
query functionality. Its main purpose is to facilitate faster query construction in the client UI. For
more complex queries or additional features, users should utilize the QUERY JSON-RPC operation.
Paging
When working with the Query API, there are important considerations regarding pagination and
fields. These help in managing performance, ensuring that query results are efficiently returned,
and preventing overloading both the client and server with excessively large datasets. Here’s a
breakdown of how pagination works and why projection data is handled differently:
Pagination in Selection
1. Mandatory Pagination: The query selection (the root documents being queried) must always
include pagination parameters in the request. These parameters are:
a. pageNumber: Specifies the page number to retrieve.
b. pageSize: Specifies the number of results per page.
c. These parameters are mandatory for the query to be considered valid. If either is missing,
the query will fail during the validation phase. There are no default values applied by the
Data Services — the client is required to explicitly set these values.
2. Hard Limits:
◦ Both pageNumber and pageSize have configurable hard limits. The query will fail validation
if either of these values exceeds the defined thresholds.
a. These limits help ensure that the system remains scalable and performs well under a
heavy load, preventing large and unmanageable queries that could impact both server
performance and stability.
b. The configuration of these limits is managed within the Query configuration.
c. Result of Selection: The result of selection will always be paged, meaning only a subset
of root documents (based on pageSize) will be returned for each request. The client is
expected to handle pagination by specifying the appropriate page to retrieve.
d. Pagination works on the selection of documents themselves and not on the projection
data (linked documents or custom data graphs).
Pagination in Projection
1. No Pagination on Projection: The result of projection cannot be paged because the projection
data is derived from the relationship models (RM), not directly from the root document set.
a. Projection queries often request the entire document graph in order to fully construct
Composed Data Document (CDD). The projection request will retrieve all necessary related
documents (linked by relationships), and paging them would not be useful in this context.
i. Only exception is usage of exclude property. Please see Exclude option section for more
details.
2. Complete Document Graph: Projection needs to load the full set of documents, including the
root documents and all related documents, to form a complete graph. Trying to page, this would
134

-- 134 of 334 --

impose complicated code on the client, which would have to issue multiple queries to load
complete data to construct CDD.
3. Upper Limit for Projection: For the projection, especially for CDD projection, an upperLimit
value is used to control the number of documents and relationships allowed.
a. Setting the upperLimit value too high, or using unbounded as a value, can result in
unmanageable result sets. This could lead to performance issues both on the client and
server side.
b. Therefore, it is advised that customer projects set a reasonable upperLimit for projection
queries to balance the need for complete data with performance considerations.
Example request:
"paging": {
"pageNumber": 5,
"pageSize": 100
}
Example response:
"fullSize": 9000,
"paging": {
"pageNumber": 5,
"pageSize": 100
}
Paging object is copied from the request. fullSize property should be used to count the number of
pages on the client side.
Sorting
The Query API allows for sorting of results based on indexed fields only. Sorting helps organize the
returned documents in a way that matches the client’s requirements, and each sorting specification
consists of multiple properties:
1. field (Mandatory): Specifies the field path that should be used for sorting the results.
2. direction (Mandatory): Determines the direction of the sorting. This property can have one of
two values:
a. ASC: Ascending order (from lowest to highest)
b. DESC: Descending order (from highest to lowest)
3. nullHandling (Mandatory): This property determines how null values should be handled in the
sorting result. Possible values:
a. NULLS_FIRST: Null values appear first in the result set.
b. NULLS_LAST: Null values appear last in the result set.
135

-- 135 of 334 --

4. ignoreCase (Mandatory): This boolean is used to switch between case-insensitive sorting and
case-sensitive sorting for IStringType fields (string-based fields).
a. If set to true, the case will be ignored.
b. If set to false, the case will be considered when sorting string values.

Data Services treats a value as null if:
• The field is missing in the document.
• The field value is explicitly set to null.
Sorting example:
{
"query": {
"targetDocumentModel": "Contract",
"sort": [
{
"field": "/Contract/name",
"direction": "ASC",
"nullHandling": "NULLS_FIRST",
"ignoreCase": true
}
]
}
}
Properties of Sorting
• Query API supports multi-level sort specification.
• Sorting is only supported for indexed fields. If a field is not indexed, it cannot be used in the
sorting specification.
• Sorting is applied to the root documents selected by the query.
• Sorting is only possible on non-repeatable fields, this especially applies to CDDs where every
group mapped via a relationship is repeatable.
Relationship-Based Sorting
The Query API supports sorting by fields on related documents through to-1 relationships. This
allows you to sort query results by properties of linked documents without loading the complete
relationship into the result.
Overview
Instead of sorting only by fields directly on the queried document, you can now sort by fields on
documents linked through relationships. For example:
• Sort Contract documents by the name field of their related BusinessPartner
136

-- 136 of 334 --

• Sort Contract documents by the city field of their partner’s Address (nested relationship)
• Combine direct field sorting with relationship field sorting in the same query
Syntax
Relationship-based sorting uses the relationshipField property (JSON) or relationshipOrder
property (Java) in the sort specification:
{
"query": {
"targetDocumentModel": "Contract",
"sort": [
{
"direction": "ASC",
"ignoreCase": false,
"nullHandling": "NULLS_LAST",
"relationshipField": {
"relationshipModel": "ContractBusinessPartner",
"targetRole": "Partner",
"field": "/BusinessPartner/Name"
}
}
],
"paging": {
"pageNumber": 0,
"pageSize": 100
},
"projectionName": "document"
}
}
 The property is named relationshipField in JSON but relationshipOrder in Java.
This mapping is handled automatically by the serialization layer.
Nested Relationship Sorting
You can traverse multiple relationships to sort by fields on documents several hops away. Use the
sortBy property to specify nested traversals:
{
"query": {
"targetDocumentModel": "Contract",
"sort": [
{
"direction": "ASC",
"ignoreCase": false,
"nullHandling": "NULLS_FIRST",
"relationshipField": {
137

-- 137 of 334 --

"relationshipModel": "ContractBusinessPartner",
"targetRole": "Partner",
"sortBy": {
"relationshipModel": "PartnerAddresses",
"targetRole": "Address",
"field": "/Address/City"
}
}
}
],
"paging": {
"pageNumber": 0,
"pageSize": 100
},
"projectionName": "document"
}
}
This query sorts Contract documents by the city field of their partner’s address, traversing two
relationships: Contract → BusinessPartner → Address.
Field Exclusivity
In the sort specification:
• Either field OR relationshipField must be specified (mutually exclusive)
• Both cannot be present in the same sort node
In the RelationshipField specification:
• Either field OR sortBy must be specified (mutually exclusive)
• field indicates a terminal sort (sort by this field)
• sortBy indicates nested traversal (continue to another relationship)
Null Handling Requirement
When using relationship-based sorting, the nullHandling property is mandatory. You must
explicitly specify how null values should be handled:
• NULLS_FIRST: Documents with missing relationships appear first
• NULLS_LAST: Documents with missing relationships appear last
Documents without the specified relationship are treated as having null values for the sort field.
Configuration Properties
Two configuration properties control relationship sorting limits:
138

-- 138 of 334 --

Property Default Description
mgmtp.a12.dataservices.que
ry.relationshipOrder.maxNe
stingDepth
5 Maximum depth of relationship traversal. Prevents
excessively deep nesting and protects against circular
references.
mgmtp.a12.dataservices.que
ry.relationshipOrder.maxCo
unt
5 Maximum number of relationship-based sorts per
query. Limits the number of JOINs generated.
These limits protect server performance and prevent potential circular reference issues.
Validation Rules
Relationship-based sorting includes comprehensive validation:
1. Field Exclusivity: sort (or Order in Java) must have exactly one of field or relationshipField
2. Relationship Field Exclusivity: relationshipField (or RelationshipOrder in Java) must have
exactly one of field or sortBy
3. Null Handling Required: nullHandling must be explicitly specified for relationship orders
4. Relationship Model Exists: The specified relationship model must exist
5. Relationship Model Accessible: User must have permission to access the relationship model
6. Target Role Exists: The specified target role must exist in the relationship
7. Target Model Accessible: User must have permission to access the target document model
8. Cardinality Restriction: Only to-1 relationships are supported (to-many relationships are
rejected)
9. Field Exists: The terminal field must exist on the target document model
10. Field Not Repeatable: The terminal field must not be in a repeatable group
11. Depth Limit: Nesting depth must not exceed maxNestingDepth
12. Count Limit: Number of relationship orders must not exceed maxCount
Validation Error Messages
Error Description
Order must specify exactly one of
'field' or 'relationshipField'
Both or neither field and relationshipField are specified
RelationshipOrder must specify
exactly one of 'field' or 'sortBy'
Both or neither field and sortBy are specified in
relationship order
Number of relationship orders [X]
exceeds configured maximum [Y]
Too many relationship orders in the query
Relationship model [X] not found The specified relationship model does not exist
User does not have permission to
access relationship model [X]
User lacks access to the relationship model
Target role [X] not found in
relationship model [Y]
The specified role does not exist in the relationship
139

-- 139 of 334 --

Error Description
User does not have permission to
access target document model [X]
User lacks access to the target document model
Relationship [X] with role [Y] has
to-many cardinality
The relationship is to-many (not supported for sorting)
Field [X] not found on document model
[Y]
The terminal field does not exist on the target model
Field [X] is in a repeatable group The terminal field is in a repeatable group (not sortable)
Relationship order nesting depth [X]
exceeds configured maximum [Y]
The relationship traversal is too deep
Explicit nullHandling is required for
relationship-based sorting
nullHandling property is missing for relationship order
Performance Considerations
Relationship-based sorting generates SQL LEFT JOINs to traverse relationships. Each relationship
level requires 4 JOINs:
1. Source role table
2. Relationship link table
3. Target role table
4. Target document table
JOIN Count Examples:
• Single-level (Contract → Partner): 4 JOINs
• Two-level (Contract → Partner → Address): 8 JOINs
• Three-level (Contract → Partner → Address → Country): 12 JOINs
Performance Recommendations:
1. Use sparingly: Only use relationship sorting when necessary for the use case
2. Limit nesting depth: Avoid deep relationship traversals (3+ levels) in performance-critical
queries
3. Use indexes: Ensure the terminal sort field is indexed on the target document model
4. Combine with pagination: Always use pagination to limit result set size
5. Monitor performance: Test queries with realistic data volumes
6. Consider denormalization: For frequently sorted relationship fields, consider denormalizing
the data
7. Profile queries: Use database query profiling to identify performance bottlenecks
8. Limit relationship order count: Minimize the number of relationship orders per query
When NOT to use relationship sorting:
140

-- 140 of 334 --

• Performance-critical queries with large result sets
• Queries that already have complex joins or aggregations
• Cases where denormalized fields are available
• Deep relationship traversals (3+ levels) with high cardinality
Security
• Permission checks are performed for all relationship models in the traversal path
• Permission checks are performed for all target document models
• Users must have access to traverse each relationship in the chain
• Validation errors do not expose unauthorized model structures
Mixed Sorting Example
You can combine direct field sorting with relationship field sorting in the same query:
{
"query": {
"targetDocumentModel": "Contract",
"sort": [
{
"field": "/Contract/CreatedDate",
"direction": "DESC",
"ignoreCase": false,
"nullHandling": "NULLS_LAST"
},
{
"direction": "ASC",
"ignoreCase": true,
"nullHandling": "NULLS_LAST",
"relationshipField": {
"relationshipModel": "ContractBusinessPartner",
"targetRole": "Partner",
"field": "/BusinessPartner/Name"
}
}
],
"paging": {
"pageNumber": 0,
"pageSize": 100
},
"projectionName": "document"
}
}
This query first sorts by Contract.CreatedDate (descending), then by BusinessPartner.Name
(ascending, case-insensitive).
141

-- 141 of 334 --

Circular Reference Protection
The maxNestingDepth configuration property protects against circular references in relationship
models. If you have self-referencing relationships or circular relationship chains, the depth limit
prevents infinite loops.
For example, if you have a relationship chain like A → B → C → A, the query will be rejected if it
attempts to traverse beyond the configured depth limit.
Query Execution Overview
Every query execution follows the predefined number of steps which ensure the most efficient
evaluation of the query.
142

-- 142 of 334 --

JSON-RPC QUERY
QueryService
Pre-process Projection
Resolve Projection
yes
found no
Pre-process Projection
Validation Phase
validate query
Prepare JSON-RPC error response
no
isValid yes
Enrichment Phase
Enrich by heterogeneity
Apply model authorization
Inject ABAC authorization conditions
Execution Phase
Generate SQL statements
Execute SQL statements
PostExecution Phase
Apply projection
Construct response object
Prepare JSON-RPC response
Figure 19. Query Execution Overview
143

-- 143 of 334 --

Handle Projection
Every query requires a valid projection specified in the projectionName property. Each projection
consists of both preprocess and postprocess methods.
The projection look-up is initiated first, followed by the execution of the preprocess method. This
preprocess method enables the mutation of the query, allowing for modifications or adjustments
before the query is executed. These mutations are crucial for ensuring that the query conforms to
the specific requirements and behaviors defined by the projection.
Validation Phase
The validation phase in DS ensures that all required properties and fields are correctly specified in
the query before it is processed. This validation occurs at two levels: Query Root Validation and
Operator Completeness Validation. The purpose is to guarantee that the query is structured
correctly, adheres to required constraints, and does not contain any missing or incompatible fields.
The primary goal of the validation phase is not to fail quickly but to provide a comprehensive list of
all errors found in the query.
However, it’s important to note that the richness of the validation response is mutually exclusive
with localization. Since server-side validation has not yet been implemented, it’s not possible to
provide localized validation messages. Therefore, the validation phase is intended to be used by
developers and customer projects integrating the DS query feature, not by end users.
Queries constructed using our TypeScript and Java clients will always result in valid JSON.
However, the query itself can still be invalid in terms of logic or functionality. Developers can use
the validation phase to identify and correct these issues. End users, however, should never
encounter validation errors directly. The user interface should always prevent the creation of
invalid queries by guiding users to build valid queries from the start.
DS recommends not using validation in the production environment. The validation phase is
primarily intended for development and testing purposes, where developers can identify and
correct issues in the query structure before deploying it to production. The validation phase does
not affect security, as invalid queries will produce invalid results. Furthermore, the more complex
validations will consume resources and execution time which should be avoided in production.
Validation of the fields might also show information about non-existing fields of the models user
cannot see. This might pose a security issue if used in the production environment. This is because
DS aggregates the error messages and returns them all at once. If a user queries for fields they do
not have access to, the validation error will indicate that the field does not exist, potentially
revealing information about the data model that the user should not be aware of.

If query validation is disabled in the configuration, invalid queries will still be
executed. This can lead to hard-to-interpret errors, because the system does not
expect such input. Generic errors like Unexpected error during query execution
may be returned.
144

-- 144 of 334 --

Query Root Validation
This level checks the integrity of the query root and ensures that all mandatory properties are
specified.
1. Mandatory Properties: All mandatory properties must be present in the query root. For
example, properties like targetDocumentModel and paging must be provided, depending on the
query’s structure.
a. Paging: The query must include valid pagination properties (pageNumber, pageSize). There are
configurable hard limits for these values (e.g., pageSize has a maximum configurable limit),
and the query will fail validation if they exceed these limits.
2. Mutually Exclusive Properties: Certain properties are mutually exclusive. For example,
aggregations and links are mutually exclusive and cannot appear in the same query. If a query
includes both, it will fail validation.
Operator Completeness Validation
Once the query root has been validated, DS moves on to operator completeness validation. This
phase recursively checks each operator in the query and validates that the operator’s properties
are correctly defined. Each operator defines its own set of validation checks.
1. Field-Aware Operators: These operators (such as exact_match, range, etc.) must refer to fields
that are valid within the target document model defined in the query. If an operator refers to an
invalid or non-existent field, the validation will fail.
a. For instance, an exact_match operator must reference an existing field in the
targetDocumentModel. If the field does not exist in the DM, an error will be raised.
2. Recursive Validation: The validation process is recursive, meaning that for every operator, the
system will check its properties, ensuring that each condition is satisfied. If an operator refers to
another operator (such as using has with constraints), these will also be recursively validated to
ensure that they are well-formed and valid.
3. Valid Operators for Specific Data Types: The validation ensures that only valid operators are
applied to fields of the correct data type. For example:
a. A double_range operator should only be applied to fields of type INumberType.
b. A date_range operator should only be applied to fields of type IDateType or IDateRangeType.
Enrichment Phase
The enrichment phase is a crucial step in the query execution process. It takes place before the
actual query execution (which involves SQL or other low-level operations) and is responsible for
enhancing the query context by adding additional data and applying logic that prepares the query
for efficient execution. Key tasks in the enrichment phase include handling heterogeneity,
authorization and other data transformations such as date formatting.
Here is a breakdown of the main functions of the enrichment phase:
145

-- 145 of 334 --

Enrichment Workflow and Timing
Enrichment is not a single, monolithic step that runs at one fixed point. Conceptually, an
enrichment is any computation that populates query-context enrichment data (for example field
type descriptors, resolved target document models, or model subtype sets) that downstream
components require to operate on the query.
Enrichment data on the query context is treated as lazy-loaded: each piece is computed at most
once, the first time it is needed, and is then cached on the context for the remainder of the query
lifecycle. Repeated requests for the same piece return the cached value without recomputing it.
The Query API observes the following timing contract:
1. Partial enrichment may happen at any point before query execution. Some validators need
parts of the enrichment data on the context in order to perform their checks (for example, to
resolve the field type descriptor for the validated field, the target document model for a link, or
to enrich a date_range operator). These on-demand enrichments are written into the query
context and remain there for the rest of the query lifecycle.
2. A full enrichment pass runs immediately before query execution and forces materialization
of every enrichment that the execution phase needs. It traverses the entire query topology and
constraint tree. For data that is already cached on the context (because a validator populated it
earlier in the lifecycle), the lazy accessors short-circuit and no extra computation is performed.
Anything that has not yet been populated is computed and cached at this point. After this pass
completes, the query is guaranteed to be fully enriched.
3. No enrichment runs after query execution has started. Once the full enrichment pass
completes, the query context is considered complete and downstream components (SQL
generators, repositories, projections) rely on the enrichment data being present and stable.
This split design lets validators surface precise, type-aware error messages without forcing the
system to perform a full enrichment pass for invalid queries, while the pre-execution pass still
provides a single guaranteed point at which the query is known to be fully enriched.
Heterogeneity Resolution
In the context of a multi-model environment, heterogeneity refers to situations where different
DMs (e.g., BusinessPartner, BusinessPartnerLegalEntity, AbstractPartner) might be used in a single
query. The enrichment phase addresses these complexities by transforming the query so that it can
work seamlessly across different DMs.
During enrichment, the system resolves references to multiple data models, transforming them into
a unified query context that is consistent across all involved models. This allows the system to treat
heterogeneous data as if they belong to a single logical structure. The model graph plays an
essential role in resolving heterogeneity. It maps the relationships and dependencies between
different document models, ensuring that the query can properly traverse them and retrieve the
necessary data.
Authorization and Security
During the enrichment phase, the system also evaluates authorization rules to ensure that users are
146

-- 146 of 334 --

allowed to query certain data. This process involves verifying the user’s access rights and applying
any necessary restrictions based on security policies.
1. Access Control: The system checks the user’s permissions to ensure they are authorized to
access the document models and fields referenced in the query.
2. Enrichment with Security Context: The query may be modified to exclude any data the user is
not authorized to view, ensuring that the results respect the user’s access control settings. This
also includes checking the user’s ability to access certain RMs.
Authorization in the enrichment phase is critical for ensuring that sensitive or restricted data is not
included in query results, protecting user privacy and data security.
Date and Other Data Preparations
Another task in the enrichment phase is the pre-formatting of dates and other data types to ensure
that they are compatible with the query operators used later in the execution phase.
1. Date Pre-formatting: Dates may need to be converted into a specific format that can be used by
the query operators, such as date_range or exact_match. For instance, dates could be serialized
into ISO 8601 format, or another format defined by the data model (DM).
2. Data Transformation: Other transformations, such as standardizing numerical values,
handling enumerations, and ensuring that fields are properly indexed for efficient search, may
also take place during enrichment.
For instance, the system may take a DateTime field and pre-format it into a string representation that
can be used more efficiently with a range query.
Avoiding SQL Execution in Enrichment Phase
It’s important to note that during the enrichment phase, no SQL queries are executed. Instead, this
phase is focused entirely on preparing the query and resolving all model-specific logic,
relationships and security concerns. This is a preparatory phase that happens entirely within the
application’s internal logic and works with the model graph, security configurations, and various
other A12 concepts.
Once the enrichment phase is complete, the system is ready to execute the query, which typically
involves translating the enriched query into SQL to retrieve the relevant data from the underlying
database.
Execution Phase
While the enrichment phase prepares the query context, it’s important to note that at this point in
the process, no A12-specific concepts are directly involved in query execution. Instead, this phase
essentially focuses on preparing SQL statements for execution based on the query’s requirements.
Once the enrichment phase concludes, SQL queries are generated and executed to retrieve data.
The generation of SQL queries depends on the complexity of the query and the specific operators
and projections defined. The process can be broken down into a series of steps that result in one or
more SQL statements. Here are the main types of SQL statements that might be generated based
on the query:
147

-- 147 of 334 --

1. Query to Load Root Documents If the query involves selecting root documents (e.g.,
documents of type Contract or BusinessPartner), one SQL statement will typically be generated
to select the root documents based on the query criteria, including any filtering conditions
specified (e.g., exact_match, range, etc.). If complete documents should be loaded additionally,
another SQL query will be executed to load them from document persistent store.
2. SQL for Complex Projections (CDD) When a CDD projection is requested, the system must
load not only the root documents but also any related documents that form part of the complete
CDD. The number of SQL statements generated will vary depending on the number of linked
documents and the complexity of the relationships. For a query that retrieves n root documents
with a pageSize of m, the number of SQL statements generated for linked documents will be
approximately n * m.
Post-execution Phase
After all SQL statements have been executed, the result of the query is being prepared. This phase
will vary for different projections, but its goal is to prepare the result of the query either by loading
additional data (loading full documents from the persistence store), mutating data (creating CDDs
from the document graph) or mutating the page content (see section Black-box authorization).
The postprocess method of the selected IQueryProjection implementation will be executed in this
phase.
Indexing
In DS, efficient querying is crucial for performance and scalability. As such, indexing plays a critical
role in determining which fields from DMs can be used in queries and projections. The goal is to
ensure that only relevant fields are indexed, helping optimize the query execution process. .
Field Annotation: Every field is by default indexed. To remove field from the index, please
annotate the field with indexed annotation with value false. In the example below we demonstrate
how to remove field "Notes" from indexing:
Example of field annotation to remove field from indexing
{
"type": "Field",
"id": "field_b83ff",
"name": "Notes",
"annotations": [
{
"name": "indexed",
"value": "false"
}
],
"Field": {
"fieldType": {
"type": "StringType"
},
"label": [
148

-- 148 of 334 --

{
"locale": "en",
"text": "Notes"
}
]
}
}
DS will only accept value false in the annotation indexed all other values will be ignored. This
annotation can be only placed on the fields (not groups).
CDMs inherit field definitions from their underlying DMs. However, cdds are not redundantly
indexed. Therefore, DS will ignore the indexed annotation in CDMs. Only fields from the
underlying DMs that are indexed will be considered available for querying, even within CDMs. No
additional indexing is required or performed for CDMs. The underlying DM must already have the
necessary indexing for fields to be used in queries. CDDs are constructed during query execution,
and therefore it is not possible to load complete documents and provide good performance. DS will
expect all DMs fields participating in CDMs to be properly annotated, construction of the CDD for
cdd projection will fail otherwise. This is only true for cdd projection. document-graph projection will
always load complete documents because the client needs complete documents to construct CDDs
on the client-side.
Document Metadata: DS maintains a special model for document metadata, which is referred to as
document-meta-data. DS will automatically index all fields from this model, which means that they
will be available for querying and projection without any need for additional annotations.
Customer projects are free to remove some of those fields from indexing by adding indexed
annotations with value false. But fields like /__meta/docRef are particularly important because they
help DS locate documents within the system. If you add the indexed=false annotation from such
fields, those documents may no longer be available for querying.
To optimize query performance, it’s strongly recommended to only index fields that are actively
used in queries. Indexing unnecessary fields will slow down query execution and negatively impact
system scalability. If fields that are never used in queries are indexed, it can increase the size of the
index, which may make queries slower due to the larger data volume in the index. Larger indexes
require more time to maintain and query. Over-indexing fields that aren’t used will increase the
load on the database and reduce overall query speed.
Heterogeneity Support
Modelers must ensure that the subtypes are semantically consistent with their parent DM. This
principle has already been applied to the definitions of groups and fields, and it also extends to the
data types of fields and rules. The same approach should be followed when defining indexed fields.
If a field is designated for indexing in the parent type, the same definition must be replicated across
all subtypes. Failing to do so constitutes a modeling error. This could lead to query failures (if the
field is not found in the DM) or incomplete results (if some subtypes lack the indexed annotation).
149

-- 149 of 334 --

Model Changes
Runtime model changes (RM and DM) can potentially corrupt the internal index table. Therefore,
modifications to the DM and RM should be made during downtime, not while the system is
running. If needed, the index can be rebuilt using the re-indexing feature. Please note that runtime
re-indexing is not supported due to missing document locking features on the DS side.
On the other hand, CDM models can be modified during runtime because they are not involved in
indexing but only in query execution.
Query API Authorization
Authorization is automatically applied, when using QUERY operation or underlying service layer.
Data Read Authorization Scopes
All data read functionality will be checked with Query authorization scope, which will have a
resource of the Document model name. This scope will be applied:
1. on Query root targetDocumentModel
2. on all links properties on the targetRole DM
3. on has operator targetRole DM
Model Authorization and Heterogeneity
When a model reference is provided to the query — either directly through the targetDocumentModel
property or indirectly via the RM — it will be translated into a list of model names in IN statement
based on the model graph. There are two possible scenarios:
1. User has permissions to load by reference: In this case, the model graph is consulted, and all
available non-abstract subtypes are included in the query recursively (subtypes, subtypes of
subtypes, and so on). A subtype and its subtypes will be excluded from the query if the user
does not have permission to read a particular DM. If no non-abstract DMs are available due to
permission restrictions, the system will respond with an empty result.
2. User lacks permissions to load the model: There are 2 possible variations of this scenario:
a. The user has no permission to load root query targetDocumentModel or Relationship
model. DS will consider this query invalid because the model graph cannot be resolved. In
this scenario, DS will respond with a QueryValidationException with message Access Denied,
as the query cannot be completed due to the lack of required permissions.
b. The user lacks permissions of some subtypes. In this case, the query will be executed, but
the results will be filtered to exclude the subtypes the user is not allowed to see. The user
will not be able to see the filtered results, but the query will still return a response. If all
subtypes are filtered out, the query will return an empty result.
150

-- 150 of 334 --

ABAC and Repository Access
All access to the data (documents & links) will be controlled via single scope Query with resource of
document model reference. Customer project can define repository access policies per model, and
DS will inject these constraints to the query automatically.
ABAC and repository access authorizations rely on the document fields that are readily available
for querying. If those fields are not present in the index, they cannot be used. The modeler should
not be forced to put indexed annotations on the fields because of security requirements.
Furthermore, the user might not be allowed to see the fields which are used in authorization
checks. Customer projects must take care of this situation and make sure that all fields that are
relevant for security concerns are indexed.
This authorization will be applied for all documents that are matching target expression in
authorizationDefinition.
Everytime targetDocument model is resolved during enrichment phase, DS will consult
authorizationDefinition to check if there are any repository policies that should be applied. If there
are, DS will inject the constraints defined in the policy into the query. Original query constraints
will be moved to operands of the newly introduced and operator and injected constraints will be
added as additional operands.
UAA will resolve SpEL expressions that are used inside the constraint. Please note, that UAA does
not provide defaults for non-existing additional properties. So, all users for which an ABAC rule
applies must have a value for all properties that are used in SpEL.
Example
Let’s consider the following authorization definition:
Example of ABAC authorization
{
"repositoryPolicies": [
{
"name": "Contract_MaxContractValueFilter_Policy_forNonManager",
"description": "Add constraint to the query for non-manager users when listing
contracts",
"target": "!hasRole('manager') && #resource == 'Contract'",
"templates": [
{
"operator": "double_range",
"field": "/ContractRoot/ContractValue",
"to": "principal.additionalProperties['maxContractValue']"
}
]
},
{
"name": "BusinessPartnerSuper_IndustryMustMatch_Policy",
"description": "Add Industry constraint to the query for BusinessPartnerSuper",
"target": "#resource == 'BusinessPartnerSuper'",
151

-- 151 of 334 --

"templates": [
{
"operator": "exact_match",
"field": "/BusinessPartnerSuper/Industry",
"value": "principal.additionalProperties['Industry']"
}
]
}
],
"permissions": [
{
"name": "Query Data",
"description": "Allows the user to query the data securely",
"repository-refs": [
"Contract_MaxContractValueFilter_Policy_forNonManager",
"BusinessPartnerSuper_IndustryMustMatch_Policy"
],
"call-parent-scope": false,
"scopes": [
"Query"
]
}
]
}
This example defines two repository policies:
Contract_MaxContractValueFilter_Policy_forNonManager and
BusinessPartnerSuper_IndustryMustMatch_Policy.
The first policy adds a double range constraint to the query for non-manager users when listing
contracts. The second policy adds an industry constraint to the query for BusinessPartnerSuper. The
permissions section defines a permission that allows the user to query the data securely. The
permission references the two repository policies.
Now let’s consider the following query:
{
"jsonrpc": "2.0",
"id": "QueryWithABACNotYetApplied",
"method": "QUERY",
"params": {
"query": {
"projectionName": "document",
"targetDocumentModel": "BusinessPartner",
"constraint": {
"operator": "not",
"operand": {
"operator": "has",
"relationshipModel": "ContractCoInsuredPartner",
"targetRole": "Contract",
152

-- 152 of 334 --

"targetDocumentModel": "Contract",
"constraint": {
"operator": "exact_match",
"field": "/__meta/docRef",
"value": "Contract/1"
}
}
},
"links": [
{
"relationshipModel": "PartnerAddresses",
"targetRole": "Address"
}
],
"sort": [
{
"field": "/BusinessPartnerRoot/Name",
"direction": "DESC",
"nullHandling": "NATIVE",
"ignoreCase": true
}
],
"paging": {
"pageNumber": 0,
"pageSize": 20
}
}
}
}
The query above will have to be extended by the ABAC authorization policies. The resulting query
will look like this for a non-manager user with a maxContractValue of 1000 and an Industry of IT:
{
"jsonrpc": "2.0",
"id": "QueryWithNonMangerUser",
"method": "QUERY",
"params": {
"query": {
"projectionName": "document",
"targetDocumentModel": "BusinessPartner",
"constraint": {
"operator": "and",
"operands": [
{
"operator": "exact_match",
"field": "/BusinessPartnerSuper/Industry",
"value": "IT"
},
{
153

-- 153 of 334 --

"operator": "not",
"operand": {
"operator": "has",
"relationshipModel": "ContractCoInsuredPartner",
"targetRole": "Contract",
"targetDocumentModel": "Contract",
"constraint": {
"operator": "and",
"operands": [
{
"operator": "double_range",
"field": "/ContractRoot/ContractValue",
"to": 1000
},
{
"operator": "exact_match",
"field": "/__meta/docRef",
"value": "Contract/1"
}
]
}
}
}
]
},
"links": [
{
"relationshipModel": "PartnerAddresses",
"targetRole": "Address"
}
],
"sort": [
{
"field": "/BusinessPartnerRoot/Name",
"direction": "DESC",
"nullHandling": "NATIVE",
"ignoreCase": true
}
],
"paging": {
"pageNumber": 0,
"pageSize": 20
}
}
}
}
The root not constraint in line 10 was moved to one operand of newly introduced and operator. The
other operand is an exact_match injected from authorizationDefinition file. Additionally, exact_match
operator of the has constraint was similarly moved to the new and operator. The double_range
154

-- 154 of 334 --

operator was added to the and operator as well. This double_range would not be injected for
manager users.
For running example please see example-authorization_uaa configuration profile in examples
module.
Black-box Authorization
For projects that cannot define their authorization requirements through expressions or queries,
DS offers an alternative: authorization can be applied after the query is executed. In this case, the
customer project can implement the IQueryResultAuthorization interface to filter the results based
on the current user’s access rights, effectively limiting the entries in the paged results to only those
the user is permitted to see. However, this approach will disrupt paging, and it is essential that all
fields involved in the authorization process are indexed to avoid loading of the complete document.
The authorization happens after the SQL statements are executed and before postprocess method of
the selected projection is executed. For custom projections, the customer projects must handle their
security by themselves if they decide to load more data in postprocess method.
Please see authorization_black_box in examples module.
Extensions Points
Query API provides several extension points to allow for customization and enhancement of the
query functionality. These extension points are designed to be used by customer projects to
implement specific use cases or to extend the capabilities of the Query API.
Projection Extension Point
IQueryProjection interface provides extension points for custom projections. These projections can
be used to modify the query results or to provide additional data that is not available in the default
projections. Custom projections can be implemented by extending the IQueryProjection interface
and providing the necessary logic in the preprocess and postprocess methods.
Example how to do that can be found in example module in class
BusinessPartnerTaxAuthorityRegistrationStatus.
Event Extension Points
The Query API publishes four additional events to support advanced extension and customization:
1. QueryBeforeOperationEvent: Triggered before the query is processed by QueryService. This
event allows you to modify the query before execution.
Note: It is only fired for QUERY operations and not when calling QueryService or QueryRepository
directly.
2. QueryAfterOperationEvent: Published after the query has been evaluated but before the
results are returned to the caller.
This event is sent from the RPC layer and is not triggered for direct calls to QueryService or
QueryRepository.
155

-- 155 of 334 --

3. QueryAfterPostProcessPhaseEvent: Emitted after the query has been executed and post-
processed.
The results are available in the QueryPage and can be further modified by event listeners. Use
this event to adjust query results system-wide, not just for JSON-RPC operations.
4. QueryBeforeExecutionPhaseEvent: Published from QueryService to allow bypassing the call to
QueryRepository.
Listeners for this event must provide the query results themselves, as QueryRepository will not
be invoked.
If you are considering implementing a listener for QueryBeforeExecutionPhaseEvent, please contact
the DS team. DS team aims to natively support all customer use cases to minimize the need for
custom extension points, as extensive custom code can complicate future migrations.
Operator Enrichment Extension Point
The IQueryAPIOperatorEnricher interface provides an extension point for enriching individual query
operators during the enrichment phase. A custom implementation receives each operator in the
constraint tree together with the current QueryContext and can populate enrichment data that is
required before query execution.
When to Use
Implement IQueryAPIOperatorEnricher when a custom operator type requires enrichment logic that
cannot be expressed through the existing extension points. For example, a custom operator that
references an external model or computes derived field type information can use this interface to
integrate seamlessly with the standard enrichment pipeline.
Do not use this extension point to modify operator structure or to execute SQL queries. The
enrichment phase is a preparatory stage; no database queries are performed during this phase.
Contract
The interface declares a single method:
QueryAPIOperatorEnricher interface
boolean enrich(ILogicOperator operator, QueryContext context);
The enrich method must return true if the enricher handled the operator, or false if the operator
was not applicable. The walker always calls all registered enrichers for each operator regardless of
the return value. An enricher must not throw unchecked exceptions for unsupported operator
types; use an instanceof check and return false instead.
Invocation Timing
A registered IQueryAPIOperatorEnricher is invoked by QueryAPIOperatorWalker during the enrichment
pass that DefaultQueryEnricher performs immediately before query execution (see Enrichment
Workflow and Timing). The walker traverses the constraint tree and calls every registered enricher
for each operator.
156

-- 156 of 334 --

Because enrichment data on QueryContext is lazy-loaded (see Enrichment Workflow and Timing),
implementations should follow the same pattern when they populate enrichment data: before
computing a value, check whether the corresponding piece of data is already cached on the context
for the given operator, and if so, reuse it instead of recomputing or overwriting it. Storing values
through the computeIfAbsent-style accessors on Enrichments (getOperatorEnrichment,
getFieldDescriptor, computeModelSubtypes, computeModelLocale) makes this automatic; for plain
setters, add an explicit guard. This keeps the enricher consistent with the lazy-load contract and
safe against future code paths that might invoke it more than once on the same operator.
Registration
Register a custom implementation as a Spring bean. The Data Services query infrastructure
discovers all beans that implement IQueryAPIOperatorEnricher automatically and invokes them
during the enrichment phase.
Example QueryAPIOperatorEnricher implementation
@Component
public class CustomOperatorEnricher implements QueryAPIOperatorEnricher {
@Override
public boolean enrich(ILogicOperator operator, QueryContext context) {
if (!(operator instanceof CustomOperator customOperator)) {
return false;
}
// Populate context enrichments for customOperator.
return true;
}
}
Simple Search
The default simple_search implementation is not using any PostgreSQL full-text feature. It is a
simple string search that will match the input string with the field value. The search is case-
insensitive and will match any part of the field value. This means that if the input string is found
anywhere in the field value, it will be considered a match.
PostgreSQL full-text search is a powerful feature that allows for more sophisticated searching
capabilities. It can handle stemming, synonyms, and other linguistic features that can improve the
search experience. However, it is not used by default in DS because it can lead to unexpected
results and may not be suitable for all use cases. DS will evaluate options to switch SIMPLE_SEARCH to
PostgreSQL full-text search in the future based on a configuration key with ticket A12S-5373. This
change will degrade the performance, because DS will need to fill another column with redundant
data that will be pre-processed.
Performance
This section focuses on known performance pitfalls and ways how to avoid them.
157

-- 157 of 334 --

 Please be noticed that we support Postgres 16, 17, and 18 but all our
Performance/Load tests are not performed on all versions. We’re performing
Performance/Load tests against Postgres 16.3.0.
Which Queries Will Be Slow?
Large Result Sets
Large result sets will cause PostgreSQL to store many rows into the memory because sorting will
have to be applied on those rows. The more data in the result set the slower the query will be.
Additionally, such queries will cause spike in
1. memory consumption by PostgreSQL
a. potentially also disc space usage if PostgreSQL decides to swap.
2. CPU usage because sorting a large results sets will require CPU usage.
In our Performance/Load tests we have created vague queries that resulted in result sets in
millions. Such a queries were rarely executed below 20 seconds.
How to avoid this problem?
1. Overview engine, Tree engine and Relationship engines provide modellable option to not load
data to fill first pages on the page reload. The user will have to provide constraints to execute
the query to load what actually needs to be loaded. This will allow customer projects to have
options to configure this model by model.
a. Skip data loading toggle exists in Application model to prevent the first page from loading
all data.
2. Restrict queries by extension points/ABAC/client side request intercepts/modeling. Loading huge
result sets for users to dig through is not a great UX. We should aim and providing more
complex queries that will provide fewer results of higher precision to the user.
a. Please see Model-ability section for details.
Simple_search
Short Input Length
By default, DS comes with limitation of the simple_search operator input length (3 characters
minimum). This was created for performance and security concerns with too broad searches
causing slow queries or even DoS attacks.
How to avoid this problem?
Increasing the value of mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize to at
least 4 should prevent the worst issues. It is also recommended to remove as many irrelevant fields
from search as possible. Doing this will make the data in which DS searches smaller. Less data
means faster queries and less resources used by PostgreSQL to resolve queries.
158

-- 158 of 334 --

Efficient Usage of the simple_search Operator
Performance tests show that the simple_search operator is one of the slowest in DS. This is mainly
due to its use of regular expressions, which are computationally expensive, especially on large
datasets.
How to optimize usage:
1. Choose the right operator. Use exact_match instead of simple_search when searching for a
specific value in a field. This is much faster.
2. Avoid combining with the or operator. Using simple_search together with or can significantly
slow down queries. If you need to match multiple values, use the values property instead of
multiple simple_search operands within an or.
3. Limit the number of fields. The more fields you specify for simple_search, the more complex
and slower the query becomes, as each field adds another or clause to the internal regular
expression. If you do not need to restrict the search to specific fields, omit the fields property
for better performance.
Queries Using the or Operator
The or operator is generally slower than the and operator because PostgreSQL must evaluate each
condition separately and then merge the results. This increases CPU and memory usage, especially
with large datasets.
How to mitigate this issue
While you cannot fully avoid the performance impact of the or operator, you can reduce it by
minimizing its use. Where possible, replace or with and using De Morgan’s law. Additionally, apply
more specific constraints to limit the result set, which can further improve query performance.
When matching a field against multiple known values, use the values property of the exact_match
operator instead of combining multiple exact_match operands within an or. This is more concise and
avoids unnecessary or overhead.
Performance Optimizations By Modeling
Removing Irrelevant Fields
Please see Model-ability section for details on how to remove irrelevant fields from indexing. This
will help to reduce the size of the index and improve query performance.
CDM Modeling For Better Performance
There are two primary use cases for Composed Data Documents (CDDs) in A12:
1. Single CDD Mutation: This scenario involves working with a single document and its related
documents. The complete documents are loaded, allowing the client to construct the CDD on the
client side. CDMs for this use case can be easily built in SME by including the necessary
underlying DMs to ensure all required data and relationships are present.
159

-- 159 of 334 --

a. The form engine retrieves data using the document-graph projection, which always loads all
documents and links in full. In this context, the fields property serves a different purpose
then other projections.
b. Performance is generally not a concern for this use case.
2. Listing Multiple CDDs: In this scenario, the goal is to retrieve multiple root documents and
their related documents, typically for overviews or listings. Only a subset of fields from the CDM
is needed, usually for display in forms. Constraints are applied to filter results, and the cdd
projection is used to load the CDDs. For optimal performance, CDMs should be tailored
specifically for the overview, containing only the fields relevant for display rather than all fields
from the underlying DMs. Any link or field not removed from the CDM will be filled by DS,
which can negatively impact performance.
a. DS must load all documents and links defined in the CDM. Such CDMs cannot be created
directly in SME, as DM includes are hardwired. Instead, these models must be created
manually by editing the CDM annotations in JSON and then opening them in SME, or by
manual modification without SME support.
b. Performance is critical when using the cdd projection for this use case, so the following
section focuses on performance optimization strategies. All fields used in cdd projection
must be indexed in the underlying DMs. DS will not index fields in CDMs, it will only use the
fields from the underlying DMs that are already indexed.
When creating CDMs for overviews, keep performance in mind:
1. Remove all validation rules and computations that are not relevant for the overview from the
CDM. This reduces the amount of data loaded and processed, improving performance.
a. Computation fields present in the underlying DMs can remain as long as they are not
transient. Computed fields defined only in CDMs (not present in the underlying DMs) should
be avoided, as they are not computed by DS by default. Computation can be enabled via
configuration, but these fields will only be computed after the query is resolved, which
slows down execution and prevents their use in constraints and sorting.
b. DS will not execute validation aftere construction of CDD therefore validation rules would
only make sense for the client-side validation.
2. Avoid repeatable fields, as overviews do not support them. While DS queries can operate on
repeatable fields in CDMs, overviews cannot construct such queries, and the results cannot be
displayed in the UI.
a. Some repeatable fields may be useful in the UI (e.g., Policyholder of the Contract), but others,
such as all addresses assigned to a Business Partner, are not useful for overviews. Construct
CDMs based on the specific use case rather than relying on the default CDM.
3. The closer the CDM is to the root document model, the better the performance. Include only the
fields necessary for the overview, not all fields from the underlying DMs. The more fields
included, the more data DS must load and process, which can slow down query execution.
a. Ideally, each overview should have its own CDM tailored to the specific use case, containing
only the fields that are relevant for that overview. This will help to reduce the amount of
data loaded and processed, improving performance.
b. It is recommended to create a CDM for each overview that is used in the system. This will
160

-- 160 of 334 --

help to reduce the amount of data loaded and processed, improving performance.
c. These models can be even provided during runtime since they do not require any special
handling by DS.
Client-side Performance Optimizations
The queries that engines generate might not be the fastest queries for your use-case. The Query API
is designed to be flexible and extensible, allowing for client-side optimizations. This means that you
can modify the JSON-RPC requests on the client side to improve performance. The query coming
from the client can be modified to include additional constraints, sorting, or can be replaced by
custom operation or by other extension points. This allows you to optimize the query for your
specific use case and improve performance.
For more info please see a guide how to replace JSON-RPC requests on the client side.
Sorting Performance
Sorting is computationally expensive, especially on large result sets. When pagination is combined
with sorting, sorting is always performed first and pagination is applied afterwards, because the
system must know the globally sorted order before it can determine which items belong to a
particular page.
Database Performance Optimizations
This section describes possible database tuning measures to improve the search performance of the
Query API.
Not all presented options will be applicable for all workloads. As with every tuning measure, this
should be accompanied by baseline tests to be able to measure the effect of each step.
Except for the memory settings, the tuning measures presented here should be considered an
exception, rather than the rule.

All links to the PostgreSQL manual use the "current" version which always refers
to the most recent PostgreSQL version. Especially system catalogs and views
change between PostgreSQL releases. If an older PostgreSQL version is used, that
version should be selected in the manual’s navigation bar to see the manual that
matches the PostgreSQL version in use.
Configuration Changes
Changing configuration properties should not be done blindly. The resources available to the
PostgreSQL instance, as well as the expected load have to be taken into account. This has to be
coordinated with the DBA.
Memory Settings
The SQL queries generated by the Query API will benefit from increasing PostgreSQL’s default
values of the properties controlling memory usage.
161

-- 161 of 334 --

work_mem
Search queries will typically result in a Bitmap Index Scan on the indexes of the document_search
table which will be more efficient if enough (work) memory is available.
Increasing work_mem to at least 32MB is recommended, 64MB might improve performance
further.
A single query can allocate this amount of memory multiple times while its running. This
recommendation assumes that the PostgreSQL instance has enough (physical) memory to use
this memory without hitting an out-of-memory error given the expected workload.
temp_buffers
Increasing the value of temp_buffers will avoid spilling large intermediate results to disk. A value
of 250MB is recommended.
shared_buffers
Increasing shared buffers will improve caching of data during retrieval. PostgreSQL relies on an
efficient file system cache. Increasing this value isn’t as important with PostgresSQL as with
other database systems.
As of PostgreSQL 17, a good starting point for this value is roughly 30%-40% of the available
RAM of the system. Higher values rarely show further improvement due to double-caching
between the file system and PostgreSQL.
The PostgreSQL development team is in the process of using direct and asynchronous I/O for
data retrieval which will put more importance on shared_buffers in the future (PostgreSQL 18
and beyond).
Parallel Query
To support queries using the "simple search" and "exact match" operators, GIN indexes are created
on the document_search table.
While PostgreSQL can use parallel query to process B-Tree indexes, this is not the case with other
index types. Tuning parallel query (e.g. by adjusting max_parallel_workers_per_gather or
max_parallel_workers) is therefor not necessary.
One situation where this would make a difference is if the query optimizer keeps using "Seq Scan"
operations. In that case parallel query can mitigate the performance problems to a certain extent,
assuming the system is able to support the additional load and I/O that parallel query imposes.
Compression
PostgreSQL compresses values automatically that are larger than approximately 2kB. The method
used to compress the values can be configured.
If the majority of the documents in the database is larger than 2kB, then using lz4 instead of pglz
can improve query performance as the lz4 algorithm (available since Postgres 14) is faster than the
"old" algorithm pglz.
162

-- 162 of 334 --

Setting the default compression algorithm to lz4 through default_toast_compression to lz4 is highly
recommended.
If changing this setting system-wide is not an option, the compression algorithm can be changed
just for the columns containing document data:
Changing compression algorithm for document content and search data
ALTER TABLE document
ALTER content SET compression lz4;
ALTER TABLE document_search
ALTER value SET compression lz4,
ALTER original_value SET compression lz4,
ALTER search_data SET compression lz4;
Changing this (or the global configuration) will not rewrite the table (nor "re-compress" the values),
so this is a very quick operation. The new compression algorithm will only be applied for new or
updated values.
If the table document_search was partitioned before changing the compression method, the above
needs to be repeated for each partition.
If the compression method is set directly when creating the partitioned table, then this will be
applied to all partitions automatically.
Database Indexes
The simple_search operator uses a regular expression to match the search value against the fields in
the document. The performance of this operation depends on the size of the documents and thus
the size of the value in the column search_value. By reducing the number of indexed fields, the
"search value" and the corresponding index will be smaller, potentially making the regular
expression evaluation faster.
Queries involving document models where only few documents exist (compared to the total
number of documents), might benefit from filtered indexes (aka "partial indexes").
In general, filtered indexes (on the document_search table) aren’t expected to make a huge
performance difference. The default index uses model_name as the leading column and all queries
include conditions on the model name.
If filtered indexes are created, the model_name column does not need to be part of the index.
By default, there is one GIN index on the column original_value that is used for exact matches and
one GIN/trigram index on the column search_value which is used for simple search queries.
Depending on what kind of queries need improvement, one or both indexes can be created as
filtered indexes:
163

-- 163 of 334 --

Filtered Indexes for Contract documents
-- Index for "exact_match" queries on Contract documents
CREATE INDEX idx_document_search_orginal_value_contract
ON document_search USING GIN (original_value);
WHERE model_name = 'Contract';
-- Index for "simple_search" queries on Contract documents
CREATE INDEX idx_document_search_data_contract
ON document_search USING GIN (search_data gin_trgm_ops)
WHERE model_name = 'Contract';
When using many queries that combine the exact_match and the simple_search operators, a
combined index helps as well:
Combined Filtered Index for Contract documents
-- Index for queries on Contract documents using "exact_match" and "simple_search"
operators
CREATE INDEX idx_document_search_contract
ON document_search USING GIN (original_value, search_data gin_trgm_ops);
WHERE model_name = 'Contract';
If these indexes are substantially smaller than the default indexes, it is likely that the PostgreSQL
query optimizer will use them, which should lead to quicker responses.
It is recommended to validate the usage of the index using explain (analyze). If the optimizer
doesn’t use it, it is recommended to drop it again, as it will slow down updates to documents and
the re-indexing process.
If filtered indexes for every data model are created, the default indexes can (and should) be
dropped.
Required PostgreSQL Extension: pg_trgm (TRGM)
Data Services requires the pg_trgm extension in PostgreSQL for simple_search and related features.
By default, Data Services enables this extension via a Liquibase changeset. If you are not using Data
Services Liquibase migrations, ensure the extension is installed manually:
-- As a user with sufficient privileges
CREATE EXTENSION IF NOT EXISTS pg_trgm;
Partitioning
An efficient index based access to the search data might not always be possible. Depending on the
actual queries, the PostgreSQL query planner might select a sequential scan ("Seq Scan") on the
entire table instead.
164

-- 164 of 334 --

This can be mitigated by partitioning the document_search table by data model. While this will not
get rid of the "Seq Scan", it will limit this to just the partition for that specific data model. This will
help if the documents are distributed across all models equally. If most of the documents are base
on one or two models, then partitioning will not make a substantial difference.
Using partitioning will require more (and manual) administrative work, e.g. the partitions for all
data models must be present before the re-indexing is started. Before deploying new models, the
necessary partitions must be created manually.

While the Liquibase schema migration provided by Data Services are in theory
independent of an existing partitioning scheme, it might be possible in the future
that manual steps will be needed when deploying a new Data Services version to a
database that uses partitioning.
An existing table can not be "converted" into a partitioned table. It is necessary to create a new
partitioned table and copy the data from the existing one to the partitioned table.
The GIN indexes that support the "simple search" and "exact match" operators do not need to
include the model_name column anymore, so they need to be re-created with a different definition.
The following example will convert an existing document_search table to a partitioned one:
First the existing table is renamed and the constraints and indexes are dropped to avoid naming
conflicts:
Renaming existing table and dropping constraints and indexes
ALTER TABLE document_search RENAME TO document_search_old;
ALTER TABLE document_search_old DROP CONSTRAINT document_search_pkey;
DROP INDEX idx_document_search_original_value;
DROP INDEX ix_jsonb_search_data;
Then a new document_search table can be created as a partitioned table:
Creating new partitioned table
CREATE TABLE document_search
(
LIKE document_search_old,
PRIMARY KEY (model_name,doc_ref)
)
PARTITION BY list (model_name);
Then one partition needs to be created for each document model:
Creating partitions for each document model
CREATE TABLE document_search_model_one
165

-- 165 of 334 --

PARTITION OF document_search
FOR VALUES IN ('ModelOne');
CREATE TABLE document_search_model_two
PARTITION OF document_search
FOR VALUES IN ('ModelTwo');
-- do this for every possible data model
...
When using psql, this step can be automated by generating the necessary DDL statement based on
the existing documents and execute them directly using psql’s \gexec command that runs the result
of a query as a script:
Creating partitions for each document model automatically
SELECT format('create table %I partition of document_search for values in (%L);',
concat('document_search_',translate(lower(model_name), ' -', '__')),
model_name) as ddl
FROM (
SELECT DISTINCT model_name
FROM document
) t
\gexec
Once the table and all partitions are created, the existing search data can be copied:
Copying existing search data to new partitioned table
INSERT INTO document_search (model_name, doc_ref, value, fulltext_string,
original_value, search_data)
SELECT model_name, doc_ref, value, fulltext_string, original_value, search_data
FROM document_search_old;
The GIN indexes to support the search don’t need to include the model_name column anymore. To
recreate them, use:
Creating indexes on partitioned table
CREATE INDEX ON document_search USING GIN (original_value, search_data gin_trgm_ops);
CREATE INDEX ON document_search USING GIN (search_data gin_trgm_ops);
If it can’t be guaranteed that new partitions are created before deploying new document models, it
is recommended to create a default partition. This will avoid failures when creating new
documents for models, where no partition is available.
CREATE TABLE document_search_model_default
PARTITION OF document_search
166

-- 166 of 334 --

FOR VALUES DEFAULT;
Monitoring Options
PostgreSQL offers several monitoring tools to investigate performance bottlenecks and slow
queries.
Queries generated by Data Services can get quite long. To make sure the logged queries are not cut
off in pg_stat_statements or when written to the log file, the propery track_activity_query_size
should be increased to (at least) 16kB. The memory required by this value is determined by
max_connections * track_activity_query_size. With a default setting 100 for connections, this will
allocate roughly 2 MB memory during startup. If the query text is still cut off in pg_stat_statements,
consider increasing this value even more.
Logging Slow Queries
If performance problems can not be reproduced in a reliable way, enabling the logging of slow
queries (in the PostgreSQL log file) can help identifying the problematic query.
log_min_duration_statement
This property can be used to log all statements that are slower than the threshold to the
PostgreSQL logfile.
Once this is enabled, the log can be analyzed regularly to detect performance problems with
specific queries. The execution plan of those queries can then be investigated.
It is recommended to start with a higher value, then lower it subsequently if no problematic
queries are logged.
auto_explain
The auto-explain extension is an alternative to log_min_duration_statement. It can be configured
to automatically log the execution plan of a query if its runtime exceeds a threshold.
By enabling this, the (slow) query plan is available immediately rather than having to run
explain manually on the query.
pg_stat_statements extension
The extension pg_stat_statements provides a more fine-grained log of all queries in the database
compared to the approach using log_min_duration_statement.
 Enabling this extension is highly recommended, even if there are no immediate
performance problems. It’s an invaluable tool for every DBA.
The extension will store details about the resource usage of each query, e.g. time spent in I/O, or the
number of blocks fetched from the cache.
Values to investigate in the view pg_stat_statements:
167

-- 167 of 334 --

Column Name Description
temp_blks_writ
ten
High values indicate that temp_buffers (and possibly work_mem) should be
increased. This can also be determined when logging temp file usage through
log_temp_files.
shared_blks_hi
t
The total number of blocks found in the buffer cache. Low values indicate that
increasing shared_buffers might improve performance.
blk_read_time The total amount of time spent reading data from disk. High values indicate that
shared_buffers should be increased (named shared_blk_read_time in PostgreSQL 17
and later).
Note that pg_stat_statements normalizes the query text, replacing constant values with parameter
placeholders. To analyze the runtime based on the original query text (including the parameter
values), using log_min_duration_statement as described above can be used.
Temp File Usage
It is recommended to set log_temp_files to 0 to log all usages of temp files. With sufficient memory
allocated to PostgreSQL, temp files (buffers) should not be used at all.
This is a bit more detailed than pg_stat_statements.temp_blks_written as it logs this on a per-query
basis.
Index Usage
When creating indexes specifically for certain workloads it is recommended to validate their usage.
The system views pg_stat_user_indexes and pg_statio_user_indexes will contain usage details about
each index. By monitoring changes to these system views, the usage can be tracked over time.
If some custom indexes are not used, they should be dropped, in order to speed up (re)indexing of
documents.
Other Database Tuning Options
Upgrade to the latest PostgreSQL version
Almost all PostgreSQL releases improve performance. Either by improving I/O throughput, data
processing or the query optimizer to find better execution plans.
The performance tests for the Query API run approximately 5%-10% faster on PostgreSQL 17
compared to PostgreSQL 16.
Data Migration Support
Predefined Migrations
Data Services (DS) provides predefined migration steps for common use cases. It is important to
168

-- 168 of 334 --

note that these migration steps are valid for a single release line only. For more information, refer
to the section on breaking change management.
All predefined migration steps provided by Data Services are associated with public classes that
extend the com.mgmtp.a12.dataservices.migration.AbstractMigrationStepConfiguration which
provide values for configurable parameters such as number of threads, batch size… Each migration
step includes a default implementation for its associated configuration, which you can customize by
simply defining your own bean using @Bean and @Primary annotations. Once defined, your
configuration will be automatically detected and applied.
For instance, the com.mgmtp.a12.dataservices.migration.AbstractMigrationStepConfiguration class
includes a field called errorHandling, which controls the error handling behavior. Here’s an
example of how this configuration can be changed:
Example of migration step configuration
@Primary @Bean public ExampleMigrationStepConfiguration
exampleMigrationStepConfiguration() {
return new ExampleMigrationStepConfiguration(ErrorHandling.CONTINUE);
}
Custom Migration
 Refer to the DS examples section for an example of custom migration.
Data Services support data migration with a simple framework which allows you to mark a class
which is responsible for the data migration with @MigrationStep annotation. The migration can be
executed during application initialization or via dataservices-server-init-app. After the execution,
we persist the information about all executed migration tasks.
To implement a migration you have to mark a class which is responsible for the migration, which in
the most cases will be a migration of documents or models but, in fact, it can be any kind of
migration. It automatically publishes this class to a Spring container and processes all injects.
A migration class (i.e. a class annotate with @MigrationStep) doesn’t have any specific constraints.
All migration tasks defined in a migration step (see Migration Task) will be executed.
The order of the method execution is not guaranteed. If you need a strict ordering, you have to
handle it on migration step level (see Migration Execution).
Migration Step
The base unit of a migration is a @MigrationStep annotation which has a couple of properties.
Required properties:
name Human readable step name. Usually should express what the step is good for.
169

-- 169 of 334 --

version The Data Services version which the step belongs to. It is highly recommended to
use DS versioning here to make sure that the step will be executed in the correct
order.
Optional properties:
author Username or full name.
description Brief description what the step is doing.
runAlways Indicates if the step should be executed with every migration execution.
Default is false.
onFailure Defines the error handling for the step. Default is HALT. For more info see
section Error Handling.
executedClassName The name of the class that is annotated with @MigrationStep is part of the
migration step identifier. Thus, moving this class or changing its name
will change the id of the migration step which will cause the migration
step to be executed again. To avoid this problem you may use this
optional parameter to use the previous class name to match already
executed migration step.
A migration step represented by a class annotated with @MigrationStep. Each migration step has one
or many migration tasks. A migration task is a public/protected method in the migration step class
which has no arguments and which is annotated with @MigrationTask.
The creation of a custom annotation by composition of @MigrationStep and other annotations is
possible. For an example, please refer to the examples-dataservices-init-app artifact.
Migration Task
A migration task is a visible (not private) method of the class annotated by @MigrationStep. The
method must have no parameters and must be annotated by the @MigrationTask annotation.
Properties of @MigrationTask (all optional):
name Migration step name, default is "[unassigned]".
runAlways Defines if the step should run with each migration or only once, default = false.
onFailure Defines the error handling in case of migration step failure, default = HALT. For
more info see section Error Handling.
 There is no implicit migration task concept anymore. All methods which should be
executed must be annotated by @MigrationTask.
170

-- 170 of 334 --

Migration Execution
When the migration process is started, all migration steps are loaded. After loading, they are
grouped by their version. In case that version doesn’t follow semantic versioning, an exception is
thrown, the server stops to initialize and terminates.
In the next step we sort the versions in ascending order and start executing all steps which belong
to a version. All steps which belongs to a particular version are executed based on their @Order
annotation, if any. Steps without @Order annotation are executed after those with @Order annotation.
Based on the error handling we persist information about executed step/tasks into the table
migration_step.
Error Handling
Each step or task can have defined an error handling in case of failure. There are 3 possible cases:
CONTINUE Ignore the error and continue with the execution. Don’t store the information that
the migration step or task has been executed.
HALT Stop the migration process. Don’t store the information that the migration step or
task has been executed.
MARK_RUN Ignore the error and continue with the execution. Store the information that the
migration step or task has been executed.
Example Usage
Let’s consider as an example that you have two models Model1 and Model2. For both models you
need to always check that a new (optional) field /Partner/name is filled in all documents. If the field
is missing or empty just report it. Then, you need to upgrade both models to a new version and fill
the new optional field.
This leads to 3 migration classes which need to be ordered. First, we have to execute the model
migration and then the field check. In the MigrationStepOne you can see an application of the
@MigrationTask annotation where it is used for redefining the task name.
To inspect this example you can check
com.mgmtp.a12.dataservices.example.migrations.MigrationStepOne,
com.mgmtp.a12.dataservices.example.migrations.MigrationStepTwo and
com.mgmtp.a12.dataservices.example.migrations.MigrationStepThree in examples-dataservices-init-
app.
All steps belong to a single version which means that we have to order them by the @Order
annotation.
The MigrationStepOne and MigrationStepTwo will be executed only once. After the execution there
will be a new row for each step in the table migration_step.
171

-- 171 of 334 --

The MigrationStepThree will be executed (as last step because it is not annotated with @Order) with
each migration (since the flag runAlways=true), and after each execution there will be again a new
row in the table migration_step.
Transaction Management and Concurrency
This chapter describes how Data Services manages transactions, handles concurrent document
operations, and ensures index consistency.
JSON-RPC Batch Transaction Handling
Data Services processes JSON-RPC requests within a single database transaction. When you send a
batch request containing multiple operations to the JSON-RPC endpoint, the following transaction
semantics apply:
• Single transaction scope: All operations in the JSON-RPC request array are executed within the
same database transaction
• Sequential execution: Operations are processed in the order they appear in the request array
• All-or-nothing semantics: If any operation fails, the entire transaction is rolled back - no
partial results are committed
• Deferred constraint validation: Relationship model constraints (upper/lower limits) are
validated after all operations complete, before the transaction commits
Example: Multiple operations in a single transaction
[
{
"jsonrpc": "2.0",
"id": "op1",
"method": "ADD_DOCUMENT",
"params": { ... }
},
{
"jsonrpc": "2.0",
"id": "op2",
"method": "ADD_LINK",
"params": { ... }
}
]
In this example, both ADD_DOCUMENT and ADD_LINK execute in the same transaction. If ADD_LINK fails,
the document added by ADD_DOCUMENT is also rolled back.
 Data Services does not support alternative transaction handling modes for JSON-
RPC requests. Each request is always processed as a single atomic transaction.
172

-- 172 of 334 --

For detailed information on JSON-RPC operations, see Operation and JSON-RPC Endpoint.
Transaction Isolation Level
Data Services uses PostgreSQL’s default isolation level: READ_COMMITTED.
Configuration Value
Spring @Transactional No explicit isolation parameter (uses database default)
PostgreSQL Default READ_COMMITTED
Effective Behavior Row-level locking with automatic rollback of index changes
What READ_COMMITTED Means
• Dirty reads are prevented: Transactions cannot see uncommitted changes from other
transactions
• Non-repeatable reads are allowed: If a transaction reads the same row twice, it may see
different data if another transaction committed changes in between
• Phantom reads are allowed: If a transaction executes the same query twice, new rows may
appear if another transaction inserted them
Index Consistency Guarantees
Document updates and index updates occur within the same transaction. This provides the
following guarantees:
• If a document update fails, index changes are automatically rolled back
• If a document update succeeds, index changes are committed together
• Concurrent updates to the same document are serialized via PostgreSQL’s row-level locking
• No index corruption can occur from transaction failures
Transaction Boundaries
The transaction boundary for JSON-RPC requests is established at the endpoint level. When a JSON-
RPC request arrives, a single transaction is started that encompasses all operations in the request.
The @Transactional annotation on Data Services service methods ensures that both document
persistence and index updates occur within this same transaction boundary. If any step fails, the
entire transaction is rolled back, including both document and index changes for all operations in
the batch.
Concurrent Document Updates
When multiple JSON-RPC requests attempt to update the same document concurrently (from
different clients or parallel requests), Data Services leverages PostgreSQL’s row-level locking to
ensure data consistency.
173

-- 173 of 334 --


Within a single JSON-RPC batch request, operations are executed sequentially, so
concurrent access between operations in the same batch does not occur. The
concurrency handling described here applies to parallel requests from different
sources.
How Concurrent Updates Are Handled
1. PostgreSQL applies row-level locking on the document record
2. The first transaction to acquire the lock proceeds with the update
3. The second transaction waits for the first to complete (commit or rollback)
4. If the first transaction commits, the second transaction sees the updated data
5. Both document and index updates are atomic (all-or-nothing)
Error Handling and Rollback
Transaction Failure Behavior
If a transaction fails (for example, due to validation error, constraint violation, or external
optimistic locking):
• The entire transaction is rolled back, including index changes
• Other concurrent transactions are not affected
• The database and index remain consistent
What Happens on Successful Commit

The transaction commit occurs at the end of the entire JSON-RPC request, not
after each individual operation. All operations in the batch share the same
transaction, and changes are only persisted when all operations complete
successfully.
When a JSON-RPC request completes successfully:
1. All database changes from all operations are committed together
2. All index changes from all operations are committed together
3. Both happen atomically - No intermediate state is observable
4. Events are published - Document change events are sent after commit
READ_COMMITTED Implications Within a Batch
Due to the READ_COMMITTED isolation level, operations within the same JSON-RPC batch may
observe data changes made by other concurrent transactions that committed during the batch
execution:
174

-- 174 of 334 --

• Operation 1 reads document A
• External transaction modifies and commits document A
• Operation 2 (in the same batch) reads document A and sees the new values from the external
transaction
This means:
• Data loaded by earlier operations may differ from data loaded by later operations in the same
batch
• Each SELECT statement sees a snapshot of committed data at the time the statement executes,
not at the time the transaction started
• Changes made by operations within the same batch are visible to subsequent operations (they
share the same transaction)
• Changes made by external transactions become visible as soon as they commit

If your batch logic depends on consistent reads of the same data across multiple
operations, consider loading all required data in the first operation and passing it
through SpEL placeholders, or implement application-level checks to detect
concurrent modifications.
What Happens on Transaction Rollback
When any operation in a JSON-RPC request fails:
1. Database changes from all operations in the batch are rolled back
2. Index changes from all operations in the batch are rolled back
3. No partial state exists - Either the entire batch succeeds or fails (atomic operation)
4. Other concurrent transactions are not affected - Each transaction is isolated
Read Replica Routing
When a PostgreSQL read replica is configured (see Read Replica Configuration (Optional)), Data
Services routes JSON-RPC transactions to the most appropriate datasource. Routing applies only to
JSON-RPC requests — background jobs, scheduled tasks, and other internal transactions always
use the primary datasource.
Routing activates automatically when spring.datasources.dataservices-read-replica.url is present
and is disabled entirely when it is absent — the behavior is then identical to previous versions.
JSON-RPC Batch Routing
Before opening a transaction the dispatcher inspects every operation in the incoming request:
• If all operations in the request are read-only, the entire request is executed in a read-only
transaction (@Transactional(readOnly = true)) and routed to the replica.
175

-- 175 of 334 --

• If any operation is mutating, the request is executed in a read-write transaction and routed to
the primary.
This ensures that mixed batches containing at least one mutating operation are never sent to the
replica.
The dispatcher sets a thread-local datasource context before opening the transaction so that
RoutingDataSource selects the correct connection at acquisition time.
Eventual Consistency and Replication Lag

Reads routed to the replica may observe a short replication lag — typically 10 to
100 milliseconds, depending on network conditions and the PostgreSQL replication
configuration.
This means that data written to the primary datasource may not yet be visible on
the replica at the moment a subsequent read-only transaction is executed.
The following scenarios are affected:
• Read-after-write within the same HTTP request: If a request first performs a write (routed to
primary) and then a read (routed to replica), the read may not see the write due to replication
lag.
• Cross-request consistency: A client that immediately reads after a successful write may
receive stale data if the replica has not yet caught up.
Mitigating Replication Lag
1. Synchronous replication — Configure PostgreSQL with synchronous_commit = remote_apply on
the primary to guarantee that writes are durable on the replica before the transaction commits.
This eliminates replication lag at the cost of higher write latency.
2. Keep read and write in the same JSON-RPC request — Include a mutating operation in the
same batch as the subsequent read. The entire request will be routed to the primary and sees all
committed data.
3. Emergency rollback — To disable replica routing entirely without a code change, remove the
spring.datasources.dataservices-read-replica.url property and restart the server. All traffic
will immediately revert to the primary datasource.
Restriction: No Write Transactions in Non-Mutating Operations

A non-mutating RPC operation (annotated with @RemoteOperation(isMutation =
false)) must not open a write transaction — including via
Propagation.REQUIRES_NEW without readOnly = true — anywhere in its call chain.
The replica datasource is read-only. Any attempt to write against it causes a
database error.
If an extension or interceptor invoked from a non-mutating operation requires a
176

-- 176 of 334 --

write transaction, that operation must be declared as mutating (isMutation =
true), ensuring it is always routed to the primary datasource.
Known Limitations
Thread Boundaries
The datasource context is thread-local and is not propagated to child threads. Any thread
spawned inside a transaction — for example via @Async, CompletableFuture, ExecutorService, or
virtual threads — starts with an empty context and always routes to the primary datasource,
regardless of the datasource context of the parent thread.
 Do not perform database operations inside spawned threads during request
handling if you need replica routing. Spawned threads always connect to the
primary datasource.
Best Practices
For Extension Developers
When developing custom extensions or interceptors for Data Services, preserve transaction
boundaries by following these rules:
1. Use default transaction propagation - Do not use Propagation.REQUIRES_NEW unless you
specifically need a separate transaction
2. Avoid manual transaction management - Let Spring manage transactions via @Transactional
annotations
3. Do not use asynchronous processing during document operations - Async operations run
outside the transaction boundary
For detailed guidelines including code examples of correct and incorrect patterns, see the
Transaction Boundaries section in Extending the Server documentation.
For Client Applications
1. Handle exceptions properly - If an operation throws an exception, assume the entire
operation failed (both document and index)
2. Do not attempt to fix the index independently - Trust Data Services' transaction management
3. Use retry logic for transient failures - Fetch fresh data before retrying after a conflict
Documents
Documents are the core data entity in Data Services, consisting of document data and document
metadata.
As modeling and other aspects are described elsewhere, this section focuses on metadata.
177

-- 177 of 334 --

Two models control the structure of a document:
• The document model, which is specific to each document type. Multiple document models may
exist in the application, defining documents of different structures. This model is used for
validation and computation when a document is uploaded to Data Services.
• The document metadata model, which is singular across the application. Documents are
enriched with metadata upon upload. Metadata is generated automatically by the application
and should not be provided by the client. When a document is downloaded, metadata is already
included, so the downloaded document contains additional metadata fields compared to the
uploaded one.
The metadata model is bundled with the application and cannot be updated by users.
 Metadata fields are included by default in the simple_search operator, but can be
excluded by setting the mgmtp.a12.dataservices.query.simple-search.excluding-
metadata.enabled=false configuration property.
Unique Constraints
Unique constraints enforce that a combination of field values is unique across all documents of the
same Document Model. They are defined in the Document Model JSON and validated by Data
Services on every document create and update operation.
Defining Unique Constraints in a Document Model
Unique constraints are declared in the Document Model under content.documentUniquenessCriteria.
Each constraint has a name, an optional localized error message, and a list of field full-names
whose combined value must be unique.
Example: Document Model with a unique constraint
{
"content": {
"documentUniquenessCriteria": [
{
"name": "UniquePersonName",
"errorMessage": [
{
"locale": "en",
"text": "Duplicate: person with this name already exists"
}
],
"fields": [
{ "fullName": "/PersonRoot/firstName" },
{ "fullName": "/PersonRoot/lastName" }
]
}
]
}
178

-- 178 of 334 --

}
Multiple constraints can be defined per model. Each constraint is evaluated independently: a
document is rejected only if it violates at least one constraint.
 Field full-names must reference fields that exist in the Document Model. Data
Services validates constraint field paths when the model is saved and rejects the
model if any path is invalid.
How Uniqueness Is Enforced
Data Services computes a SHA-256 hash of the concatenated field values for each constraint on
every document create and update. The hash is stored in the DOCUMENT_UNIQUE_CONSTRAINT database
table together with the model name, constraint name, and document reference. A unique index on
(model_name, constraint_name, field_values_hash) prevents duplicate entries at the database level.
On document delete, all uniqueness tracking entries for the deleted document are removed.
Uniqueness is tracked per constraint at the level of the topmost Document Model in the inheritance
hierarchy that still defines that specific constraint. This means that documents of different sub-
models sharing a common ancestor that defines the constraint compete for the same constraint
namespace.
Sub-models may define additional constraints that are not present in any ancestor. Those
constraints are tracked under the sub-model itself, so their uniqueness scope is limited to
documents of that sub-model and its own subtypes.
Example
BaseDocument (defines: isbn_unique)
├─ ElectronicBook (inherits: isbn_unique, adds: format_unique)
└─ PrintBook (inherits: isbn_unique, adds: edition_unique)
• isbn_unique is tracked under BaseDocument for all three model types — an ISBN used by an
ElectronicBook document cannot be reused by a PrintBook document.
• format_unique is tracked under ElectronicBook — uniqueness is only enforced within
ElectronicBook documents.
• edition_unique is tracked under PrintBook — uniqueness is only enforced within PrintBook
documents.
Violation Behavior
When a unique constraint is violated during a create or update operation, Data Services throws a
UniqueConstraintViolationException (error code -32060) and the document is not persisted.
The exception contains the name of the violated constraint in the error details.
179

-- 179 of 334 --

Pre-flight Uniqueness Check: CHECK_UNIQUENESS Operation
Before creating or updating a document, you can perform a pre-flight check using the
CHECK_UNIQUENESS JSON-RPC operation. This is a read-only operation that does not persist any data.
The operation checks all uniqueness constraints defined for the given document model in a single
call.
Request Parameters
Parameter Required Description
documentModelName Yes The name of the document model whose uniqueness
constraints are to be checked.
document Yes The full document content as a JSON object. Field values
are extracted from this object using the field paths defined
in each uniqueness constraint.
docRef No The document reference of the document being updated.
When provided, any existing constraint entry that belongs
to this document reference is excluded from the conflict
check (self-exclusion for update scenarios).
Request Examples
Example: CHECK_UNIQUENESS request (new document)
{
"jsonrpc": "2.0",
"id": "checkName",
"method": "CHECK_UNIQUENESS",
"params": {
"documentModelName": "Person",
"document": {
"PersonRoot": {
"firstName": "John",
"lastName": "Doe"
}
}
}
}
When validating an update to an existing document, provide the docRef parameter to exclude the
document being updated from conflict detection:
Example: CHECK_UNIQUENESS for an update (self-exclusion)
{
"jsonrpc": "2.0",
"id": "checkNameForUpdate",
"method": "CHECK_UNIQUENESS",
180

-- 180 of 334 --

"params": {
"documentModelName": "Person",
"document": {
"PersonRoot": {
"firstName": "John",
"lastName": "Doe"
}
},
"docRef": "Person/42"
}
}
Response
The operation returns a response object with the following top-level fields. When violations is
empty, all uniqueness constraints are satisfied. A non-empty violations list is a normal validation
outcome, not an error — the operation never throws for constraint violations.
Field Description
violations Array of violated constraints. Empty when all constraints are satisfied.
Each element in violations represents a single constraint violation and contains the following
fields:
Field Description
modelName The topmost Document Model name that defines this specific constraint.
May differ from the requested documentModelName when a sub-model is
passed and the constraint is inherited from an ancestor. Different
violations in the same response may carry different modelName values
when some constraints are inherited and others are defined only on the
sub-model.
constraintName The name of the violated uniqueness constraint as defined in the
document model.
conflictingDocRef The document reference of the existing document that holds the
conflicting field value combination.
errorMessage A map of locale code to localized error message text, as defined in the
document model. May be empty if no error messages are configured for
the constraint.
fieldFullNames The ordered list of field paths that form this constraint (for example,
"/PersonRoot/firstName").
errorKey A key composed as
error.document.unique.constraint.violation.{modelName}.{constraintName
}, where {modelName} is the modelName field of this violation and
{constraintName} is the name of the violated constraint.
181

-- 181 of 334 --

Response: no violations
{
"jsonrpc": "2.0",
"id": "checkName",
"result": {
"violations": []
}
}
Response: one constraint violated
{
"jsonrpc": "2.0",
"id": "checkName",
"result": {
"violations": [
{
"modelName": "Person",
"constraintName": "UniquePersonName",
"conflictingDocRef": "Person/42",
"errorMessage": {
"en": "Person name must be unique",
"de": "Personenname muss eindeutig sein"
},
"fieldFullNames": ["/PersonRoot/firstName", "/PersonRoot/lastName"],
"errorKey":
"error.document.unique.constraint.violation.Person.UniquePersonName"
}
]
}
}
Response: sub-model with mixed inherited and own constraints violated
{
"jsonrpc": "2.0",
"id": "checkName",
"result": {
"violations": [
{
"modelName": "BaseDocument",
"constraintName": "isbn_unique",
"conflictingDocRef": "PrintBook/99",
"errorMessage": {},
"fieldFullNames": ["/BookRoot/isbn"],
"errorKey":
"error.document.unique.constraint.violation.BaseDocument.isbn_unique"
},
{
182

-- 182 of 334 --

"modelName": "ElectronicBook",
"constraintName": "format_unique",
"conflictingDocRef": "ElectronicBook/55",
"errorMessage": {},
"fieldFullNames": ["/BookRoot/format"],
"errorKey":
"error.document.unique.constraint.violation.ElectronicBook.format_unique"
}
]
}
}
 An empty violations list does not guarantee that the document will still be unique
at write time due to concurrent operations.
 CHECK_UNIQUENESS must be added to
mgmtp.a12.dataservices.jsonRpc.allowedOperations before it can be used.
Java API
CheckUniquenessOperation.rpc() returns CheckUniquenessResponse, a record in
com.mgmtp.a12.dataservices.uniqueconstraint with the following fields:
Field Type Description
violations List<CheckUniquene
ssResult>
The list of violated constraints. Empty when all constraints
are satisfied.
CheckUniquenessResult is a record with the following fields:
Field Type Description
modelName String The topmost Document Model name that defines this
specific constraint. May differ from the requested model
name when the constraint is inherited from an ancestor.
Different results in the same response may carry different
modelName values.
constraintName String The name of the violated uniqueness constraint.
conflictingDocRef DocumentReference The document reference of the conflicting document.
errorMessage Map<String,
String>
Map of locale code to localized error message text, as
defined in the document model.
fieldFullNames Collection<String> The ordered list of field paths that form this constraint.
errorKey String Key composed as
error.document.unique.constraint.violation.{modelName}.{
constraintName}, where {modelName} matches this result’s
modelName() field.
183

-- 183 of 334 --

TypeScript API
• CheckUniquenessJsonRpc2Request.params requires documentModelName: string, document: object,
and optionally docRef: string.
• The response type is CheckUniquenessJsonRpc2Response where result is CheckUniquenessResponse.
• CheckUniquenessResponse carries violations: CheckUniquenessViolation[].
• Each CheckUniquenessViolation carries modelName: string (the topmost model defining this
constraint), constraintName: string, conflictingDocRef: string, errorMessage: Record<string,
string>, fieldFullNames: string[], and errorKey: string.
• Different violations in the same response may carry different modelName values when some
constraints are inherited and others are defined only on the sub-model.
Error Codes
Error Code Description
-32060 UNIQUE_CONSTRAINT_VIOLATION — A uniqueness constraint was violated
during a document create or update operation. The error detail contains
the constraint name. This error is not thrown by CHECK_UNIQUENESS, which
returns a result instead.
-32061 MODEL_UNIQUE_CONSTRAINT_VALIDATION — A unique constraint definition in
the Document Model is invalid (for example, a field path does not exist in
the model). This error is returned when saving the model.
Attachments
An attachment is any file which can be attached to an A12 document.
• Attachments are stored in a configurable store and the document contains only references to
those attachments.
• An attachment API is used for handling of attachments, Document API is used for handling the
attachment assignments (reference management).
• Data Services provide extension points (see here).
Attachment Definition and Usage
All attachments are defined by a group with "usageType": "attachment".
Example of an attachment group definition
{
"type": "Group",
"id": "G2",
"name": "MyAttachment",
"externalDescription": [
{
184

-- 184 of 334 --

"locale": "en",
"text": "External Description Text"
}
],
"internalDescription": [
{
"locale": "en",
"text": "Internal Description Text"
}
],
"Group": {
"repeatability": 10,
"required": false,
"usageType": "attachment",
"elements": [
{
"type": "Field",
"id": "F3",
"name": "original_filename",
"Field": {
"fieldType": {
"type": "StringType",
"StringType": {}
}
}
},
{
"type": "Field",
"id": "F4",
"name": "internal_filename",
"Field": {
"fieldType": {
"type": "StringType",
"StringType": {}
}
}
},
{
"type": "Field",
"id": "F6",
"name": "attachment_id",
"Field": {
"fieldType": {
"type": "StringType",
"StringType": {}
},
"required": true
}
},
{
"type": "Field",
185

-- 185 of 334 --

"id": "F8",
"name": "mime_type",
"Field": {
"fieldType": {
"type": "StringType",
"StringType": {}
}
}
},
{
"type": "Field",
"id": "F9",
"name": "category",
"Field": {
"fieldType": {
"type": "StringType",
"StringType": {}
}
}
},
{
"type": "Field",
"id": "F10",
"name": "description",
"Field": {
"fieldType": {
"type": "StringType",
"StringType": {}
}
}
}
]
}
}
The only required field in this group is attachment_id, which references an existing attachment.
Other fields in the group may be used by Kernel logic to improve user experience during rendering.
Data Services processes this group like any other in the document. The attachment model is not
persisted in DS, so validation must be handled in SME by reviewing model validation rules. DS
relies solely on the attachment_id field for groups with usageType=attachment.
Example of a document with an attachment group
{
"MyAttachment": {
"original_filename": "",
"internal_filename": "internal",
"attachment_id": "1bcf66bf-fc49-48a3-b386-c07b5563924f",
"mime_type": "jpg",
186

-- 186 of 334 --

"category": "",
"description": ""
}
}
Attachment Upload
Attachments can be uploaded to the server using the endpoint.
Attachment upload endpoint
POST v2/attachment?filename=&documentModelName=&pathToField=
which will add attachment metadata to the ATTACHMENT_HEADER table and the attachment itself will be
stored in the Content Store (Attachment Store). For attachments of types: JPEG, PNG, BMP, WBMP , GIF or
SVG, thumbnails will be generated with .png extension in 2 configurable sizes. It is also possible to
generate custom thumbnails which will overwrite DS thumbnail generation using extension points.
 SVG is only supported for Thumbnailator. Enabling
mgmtp.a12.dataservices.attachments.thumbnail.optimization.url.enabled would
return an empty url.
All filename, documentModelName and pathToField are mandatory query parameters.
• filename is not only the attachment identifier the user knows but also with correct file extension
might be a hint for supporting attachment mime type detection more precisely, therefore it is
vital for attachment handling.
• documentModelName is needed for security purposes. The user of this endpoint must have
MODEL_READ permissions because the attachment is always meant for some document, and the
user needs read access to the model of this document.
• pathToField Not yet implemented, but it is mandatory parameter. Empty string could be used.

There is no attachment update available. To change attachment create new
attachment and un-assign old one.
For more information on the attachment storing and downloading please see
Content store section.
The table below shows the differences in mime type detection between uploading
attachments with and without correct filename.
File Type File Name Detected Mime Type
Json Attachment text/plain
Json Attachment.json application/json
csv Attachment text/plain
187

-- 187 of 334 --

csv Attachment.csv text/csv
There are differences in mime type detection for MS Word doc and docx files.
Based on the official Microsoft MimeType formats, MS Word doc file will have
mime type application/msword and MS Word docx file will have mime type
application/vnd.openxmlformats-officedocument.wordprocessingml.document.
Attachment Download
Before an attachment or a thumbnail can be downloaded it must become available via
LOAD_ATTACHMENT_URL (for attachments) or via LOAD_THUMBNAIL_URL (for thumbnails).
• LOAD_THUMBNAIL_URL will return stable public URLs (no security will be applied during download)
which will contain caching instructions for the browser.
◦ Only the parameter attachmentId is mandatory which will be used to load the thumbnail ids.
• LOAD_ATTACHMENT_URL will generate expirable public URLs (no security will be applied during
download) with instruction for the browser not to cache. The expiration period is configurable.
◦ attachmentId is mandatory because it is needed to locate the attachment in the system.
◦ docRef of the document where the attachmentId is assigned. The URL will be provided only
for attachments that are already assigned to a document. This check ensures that
attachments will be available only to users that have permissions to see the document. This
parameter is mandatory for security reasons.
• The diagram below shows how attachment can be retrieved from Data Services and Content
Store:
Data Services 	Content Store
client
client
Data Services
Data Services
ATTACHMENT_HEADER
ATTACHMENT_HEADER
AttachmentRepository
AttachmentRepository
Content Store Service
Content Store Service
Content Store Database
Content Store Database
Request the attachment URL
Get Attachment header
Attachment header
Check permissions
a l t 	[permissions OK]
Ask for URL
Create ticket for the attachment with ID,
Attachment store coordinate
and validity timestamp
Temporary valid URL of the attachment with the ticket ID
Temporary valid URL of the attachment with the ticket ID
[no access]
403: Unauthorized
Ask for the attachment content by temporary URL with ticket ID
Get ticket by ID from URL
return ticket by ID
a l t 	[validate ticket OK]
Get content stream with valid ticket
[ i nva l i d t i ck e t ]
404: Not Found
Find content by coordinate from the ticket
Content download stream
Get the attachment content
Figure 20. Content download diagram
188

-- 188 of 334 --

Attachment Deletion
There is no API to delete attachments from the store directly. An attachment is marked for deletion
if the document has unassigned this attachment from its references.
Attachments marked for deletion are then picked up by CleanUpDirtyAttachmentsJob which will
delete them from the system if there are no existing references for the respective attachment. This
behavior can be extended by adding IDirtyAttachmentCleanupCondition bean to add additional
decision logic.
Attachments that have been created but not assigned will be picked up by
CleanUpStaleAttachmentsJob which will remove attachments with no references that has been
created a configurable amount of time ago.
Attachment Assignment
All attachments of a document are synchronized everytime the document is saved (ADD_DOCUMENT,
MODIFY_DOCUMENT, PARTIAL_MODIFY_DOCUMENT). All attachments referenced from a document must exist
before the document can be saved. All attachments that are no longer used in the document will be
marked as dirty and deleted in due time.
Attachment assignment is transactional. Multiple attachment assignments in multiple document
update operations in a single JSON-RPC will transition from one consistent state to the next for all
assignments.
Attachment Extension Points
 Refer to DS examples section for an example of using attachment extension points.
Attachment handling is separated between DS code (metadata, extension points) and Content Store
code (content, extension code). See diagram to see what can be used for extensions. There are also
examples available (encryption of attachments,..).
189

-- 189 of 334 --

j ava
lang 	io
dataservices-core	
dataservices	
a t t a c h m e n t
h e a d e r
persitence
v 2	
persistence
content-store	
contentstore	
service
c o n t e n t
dataservices-models	
dataservices	
a t t a c h m e n t
r e fe r e n c e
CharSequence 	Serializable
AttachmentService
createAttachment(	
is: java.io.InputStream	
filename: java.lang.String	
documentModelName: java.lang.String	
pathToField: java.lang.String	
annotations: java.util.List	
): com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
findAttachmentUrl(	
attachmentId: java.lang.String	
docRef: com.mgmtp.a12.dataservices.document.DocumentReference	
): java.util.Optional	
findThumbnailUrl(	
attachmentId: java.lang.String	
type: com.mgmtp.a12.dataser vices.attachment.ThumbnailType	
): java.util.Optional	
findThumbnailUrl(	
header : com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
type: com.mgmtp.a12.dataser vices.attachment.ThumbnailType	
): java.util.Optional
AttachmentHeaderService
create(
header : com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
): com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
delete(
attachmentId: java.lang.String	
): void
load(
attachmentId: java.lang.String	
): java.util.Optional	
loadUnassignedAttachmentsOlderThan(	
tmpAttachmentExpireHours: int	
): java.util.List	
assignAttachment(	
header : com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
reference: com.mgmtp.a12.dataser vices.attachment.AttachmentReference	
): void
unAssignAttachment(	
header : com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
reference: com.mgmtp.a12.dataser vices.attachment.AttachmentReference	
): void
unAssignAttachments(	
documentReferences: java.util.Collection	
): void
IAttachmentRepository
create(
id: java.lang.String	
is: java.io.InputStream	
filename: java.lang.String	
type: com.mgmtp.a12.dataser vices.attachment.TypeOfTheContent	
mimeType: java.lang.String	
): com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult	
findUrl(
id: java.lang.String	
filename: java.lang.String	
type: com.mgmtp.a12.dataser vices.attachment.TypeOfTheContent	
): java.util.Optional	
delete(
id: java.lang.String	
): void
AttachmentService 	IAttachmentRepository
ContentPersistenceResult
contentId: java.lang.String	
size: long	
url: java.util.Optional<java.lang.String>	
contentType: java.lang.String
ContentPersistenceResultBuilder
ContentStoreService
requestContentUrl(	
contentId: java.lang.String	
duration: long	
): java.lang.String	
findPublicContentUrl(	
contentId: java.lang.String	
): java.util.Optional	
getContent(
id: java.lang.String	
): com.mgmtp.a12.contentstore.content.ContentStream	
exists(
id: java.lang.String	
persistentType: java.lang.String	
): boolean	
saveContent(	
contentId: java.lang.String	
persistentType: java.lang.String	
inputStream: java.io.InputStream	
filename: java.lang.String	
): com.mgmtp.a12.contentstore.ContentPersistenceResult	
saveContent(	
contentId: java.lang.String	
persistentType: java.lang.String	
inputStream: java.io.InputStream	
filename: java.lang.String	
mimeType: java.lang.String	
): com.mgmtp.a12.contentstore.ContentPersistenceResult	
deleteById(
contentId: java.lang.String	
): void
ContentStream
contentSupplier: java.util.function.Supplier<java.io.InputStream>	
contentType: java.lang.String	
ready: boolean	
isPublic: boolean	
readyLock: java.util.concurrent.locks.Lock	
isReadyCondition: java.util.concurrent.locks.Condition	
setReady(): void	
awaitReady(	
timeoutMs: long	
): boolean
ContentStreamBuilder	ContentPersistenceResult
AttachmentAnnotation
name: java.lang.String	
value: java.lang.String
AttachmentHeader
attachmentId: java.lang.String	
thumbnailBigId: java.lang.String	
thumbnailSmallId: java.lang.String	
filename: java.lang.String	
references: java.util.List<com.mgmtp.a12.dataser vices.attachment.AttachmentReference<com.mgmtp.a12.dataser vices.reference.GenericReference>>	
createdAt: java.time.Instant	
createdBy: java.lang.String	
modifiedAt: java.time.Instant	
modifiedBy: java.lang.String	
mimeType: java.lang.String	
size: java.lang.Long	
typeOfTheContent: com.mgmtp.a12.dataservices.attachment.TypeOfTheContent	
annotations: java.util.List<com.mgmtp.a12.dataser vices.attachment.AttachmentAnnotation>
AttachmentUrl
location: java.lang.String
DataServicesThumbnail
type: com.mgmtp.a12.dataser vices.attachment.ThumbnailType	
mimeType: java.lang.String	
content: java.util.function.Supplier<? extends java.io.InputStream>
AttachmentReference
type: com.mgmtp.a12.dataser vices.attachment.AttachmentReferenceType	
reference: T	
parse(
type: com.mgmtp.a12.dataser vices.attachment.AttachmentReferenceType	
ref: java.lang.String	
): com.mgmtp.a12.dataser vices.attachment.AttachmentReference	
fromDocRef(	
docRef: java.lang.String	
): com.mgmtp.a12.dataser vices.attachment.AttachmentReference	
fromDocRef(	
docRef: com.mgmtp.a12.dataservices.document.DocumentReference	
): com.mgmtp.a12.dataser vices.attachment.AttachmentReference
AttachmentReferenceType	
DOCUMENT;	
values(): Array	
valueOf(
name: java.lang.String	
): com.mgmtp.a12.dataser vices.attachment.AttachmentReferenceType
ThumbnailType	
SMALL, BIG;	
values(): Array	
valueOf(
name: java.lang.String	
): com.mgmtp.a12.dataser vices.attachment.ThumbnailType
GenericReference
«use»
«use»
«use»
«use»
«use»	«use»
«use» 	«use»
«use»
«use»	«use» 	«use»
«use»
«use»	«use»
Figure 21. Attachment diagram
Attachment Mime Type Probing Improvement
Data Services introduces properties to improve contents mime type probing.
• mgmtp.a12.dataservices.attachments.mimeType.probeMimeType.enabled for enable/disable probing
mime type for attachments feature. If enabled, Data Services will probe attachments mime type
and send the result to Content Store while persisting attachments, in other case mime type will
be probed by Content Store during attachments persistence.
• mgmtp.a12.dataservices.attachments.mimeType.inMemoryTemp.enabled for enable/disable using in-
memory temporary file while probing attachments mime type. If enabled, we will use in-
memory temporary file for probing attachments mime type, in other case temporary file will be
located on File System, this will require hard disk write permission.
Data Services and Content Store properties compatibility matrix:
Data services
probeMimeType.enabled
Content Store
trustExternalMimeType.enable
d
Description
190

-- 190 of 334 --

false(default) false(default) This is default behavior as
previous versions, Content
Store will probe content mime
type.
false true This setup makes no sense,
since Content Store requires
mimeType but Data Service will
never probe it this will cause
Invalid Input Error.
true false This setup is wasting
performance, Data Services will
handle attachments mime type
probing and send result to
Content Store but Content Store
will still probe mime type again.
true true Data Services will handle
attachments mime type probing
and send result to Content
Store, the result is trusted and
accepted.
Relationships
• Model Graph
• Relationship Model
• Relationship Migration
Abbreviations
RM Relationship Model
DM Document Model
LM Link Document Model
RL Relationship Link
Overview and Definitions
An association between 2 documents is called a Relationship Link (RL). This RL refers to a source
and a target document and can contain additional information about the relationship . Every RL
needs a Relationship Model (RM) which contains, amongst others, the source DM, the target DM, and
the LM. This structure of the RM is defined in the Relationship Meta Model. A RL contains references
(docRefs) of the documents which implement the source and target DM in the RM. Furthermore, it
may contain the id of the document that implements the LM of the RM. In case the RM does not
191

-- 191 of 334 --

provide a LM then the docRef of the LM document has to be blank as well.
Document Model CRUD
• Creation of a new DM does not affect existing relationships in any way.
• Update of a DM might affect relationships in the following ways:
◦ Changed permissions ⇒ The DM is now not visible to some users and therefore relationship
might disappear from the model graph as well.
▪ Since the model graph is not cached the current value would be calculated on the fly. So
no changes are needed on an update of a DM.
◦ Content of the DM may conflict with documents already present in the DB.
▪ We currently have this situation, and it is not a responsibility of Data Services team to
handle it. It is a migration topic.
• Deletion of a DM would have the same effect as a change of the permission and therefore
should be treated in the same way.
The update or deletion of a DM might result in changes of a model graph which could be possibly
already loaded on client startup. This can be considered a minor issue since we do not support
model changes at runtime. We would suggest having a mechanism on client side to load model
graph from the server every x minutes if this is a problem.
Document CRUD
• Creation of a document does not affect relationships in any way.
• Update of a document might affect relationships feature in the following way:
◦ Paged candidates-lists based on some filter might get corrupted by updating a document that
is in that result set in a way that the document would be excluded from the result set (or the
other way round).
▪ This is currently the case for the overview engine, too, and could be solved by the
introduction of cursors.
◦ The same applies for the deletion of a document that should be displayed in a paged result
set.
• Deletion of a document using the DELETE_DOCUMENT operation will remove links associated with
the deleted document first.
Model Graph
The model graph is a representation of the relationships between various DMs stored in the
persistent store and visible to the currently logged-in user. The model graph contains the following
information:
• List of available DMs.
◦ For each DM in the list there is:
192

-- 192 of 334 --

▪ A section for other DMs which are in a relationship with it.
▪ A section for subTypes of the DM.
▪ Information whether the model is abstract. Abstract model means that there is no
possibility to create documents of that model. Abstract models are intended just to be
parents of its subtypes. To create a model as abstract add abstract annotation to it with
no value ("") or value of "true". Other values will be considered NOT abstract.
▪ Each super type needs to specify its closest subtypes (comma separated list) in its
subTypes annotation. These annotations will be recursively introspected during the
model graph generation.

Please note that Data Services is not able to detect cyclic type
definitions. Therefore, the following state will be considered invalid
leading to runtime errors: MODEL1 is a subType of MODEL2 and
MODEL2 is a subtype of MODEL1.
• List of all RMs in JSON representation.
• List of available CDMs (i.e. the CDMs where the current user is allowed to see all included DMs).
• List of all other relevant models (if any) and their references, except the ones supported by Data
Services (no Document models and Relationship models here).
Relationship Model Availability
 Refer to the Relationship Model to see detailed structure of Relationship Model.
The availability of the RM is related to the current user because in order to determine if the RM is
valid we need to check the permissions of all DMs that are used in the RM.
We consider a RM accessible if:
• The entityCharacteristics group has exactly 2 entries (there are no 3-way relationships
allowed).
• The current user has permissions to read RelationshipMetaModel.
• The current user has permissions to read all DMs specified in entityCharacteristics entries.
• The current user has permissions to read the DM assigned to the linkDocumentModel property.
DMs Displayed in a Model Graph
• All DMs which the current user is allowed to see and which are not part of any RM.
• All DMs that are part of a RM if the RM is accessible for the currently logged-in user.
◦ This includes the DMs in the entityCharacteristics, LM and generated models.
Structure
The model graph JSON contains the following main properties:
193

-- 193 of 334 --

• documentModels: Contains all available Document Models (DMs). Each entry provides the model’s
ID (modelId), display labels (displayLabels), relationships to other models (relations), subtypes
(subTypes) and whether the model is abstract (abstractModel).
• composeDocumentModels: Lists all Compose Document Models (CDMs) that the current user can
access. Each entry includes the model’s ID (modelId) and display labels (displayLabels).
• relationshipModels: Describes all Relationship Models (RMs) in detail. Each entry contains
metadata (header) and the structure of the relationship (content), including roles (role), link
constraints (linkConstraints), and associated document models (documentModel).
• genericModels: Lists all other relevant models. Each entry has the model’s ID (modelId), display
labels (displayLabels), model type (modelType) and all references to other models
(modelReferences).
ModelGraph FS Implementation
Data Services provide a tool for generating model graphs from your models. Its basic usage is as
follows:
• java -jar dataservices-modelgraph-fs-impl-39.0.2-fatjar.jar ARGUMENTS or simply
• ./dataservices-modelgraph-fs-impl-39.0.2-fatjar.jar ARGUMENTS
ARGUMENTS should specify the path to the models folder from which you wish to generate the model
graph.
Example: java -jar dataservices-modelgraph-fs-impl-39.0.2-fatjar.jar
file:/Users/a12/resources/models
Note Use --help (alias -h) flag to show all available options.
ModelGraphFactory API
ModelGraphFactory is a standalone Java API for building a ModelGraphRoot from file-system model
files without requiring a running Spring application context. It is part of the dataservices-
modelgraph-fs-impl module and is intended for use in plain Java applications, build tools, or other
non-Spring environments.
Methods
ModelGraphFactory.buildFromContent(List<String> modelContents)
Accepts a list of pre-loaded JSON model content strings and builds a ModelGraphRoot. No
filesystem access is performed; all model data must be provided by the caller.
ModelGraphFactory.buildFromResources(List<URI> resourceUris)
Reads JSON model files from the provided URIs and delegates to buildFromContent. Supports
file: and classpath: URI schemes.
Module
194

-- 194 of 334 --

dependencies {
implementation "com.mgmtp.a12.dataservices:dataservices-modelgraph-fs-impl:
${dataServicesVersion}"
}
Usage Example
import com.mgmtp.a12.dataservices.relationship.ModelGraphRoot;
import com.mgmtp.a12.dataservices.modelgraph.fs.impl.ModelGraphFactory;
import java.net.URI;
import java.util.List;
// Build from file URIs
ModelGraphRoot graph = ModelGraphFactory.buildFromResources(List.of(
URI.create("file:/path/to/models/ContractModel.json"),
URI.create("file:/path/to/models/RelationshipModel.json")
));
// Build from pre-loaded content strings
String modelJson = Files.readString(Path.of("/path/to/models/ContractModel.json"));
ModelGraphRoot graph2 = ModelGraphFactory.buildFromContent(List.of(modelJson));
Relationship Model
The Relationships feature is based on the Relationship models defined in the RelationshipMetaModel,
which can be found in dataservice-models module.
Security for relationship models is provided in the same way as for all other models: The user must
have MODEL_READ access right for the RM, the source and target DMs, and the LM (this might not be
present in the RM) to be able to read the RM.
The following properties of RMs are currently used. All properties (except the linkDocumentModel)
are mandatory and split into header and content:
Relationship model structure
@startjson
{
"header": {
"id": "ContractBusinessPartner",
"modelType": "relationship",
"modelVersion": "4.0.0",
"locales": [
{
"code": "en"
},
{
195

-- 195 of 334 --

"code": "en_US"
}
],
"labels": [
{
"locale": "en",
"text": "ContractBusinessPartner"
}
],
"annotations": [
{
"name": "roles",
"value": "guest,admin,systemAdmin"
}
],
"modelReferences": [
{
"purpose": "Document model",
"modelType": "document",
"alias": "Contract",
"reference": "Contract"
},
{
"purpose": "Document model",
"modelType": "document",
"alias": "BusinessPartnerSuper",
"reference": "BusinessPartnerSuper"
}
]
},
"content": {
"duplicatesAllowed": false,
"labels": [
{
"locale": "en",
"text": "ContractBusinessPartner"
}
],
"entityCharacteristics": [
{
"ordered": false,
"linkConstraints": {
"multiplicity": {
"unbounded": true,
"upperLimit": null
}
},
"labels": [
{
"locale": "en",
"text": "Contract"
196

-- 196 of 334 --

}
],
"role": "Contract",
"documentModel": "Contract"
},
{
"ordered": false,
"linkConstraints": {
"multiplicity": {
"unbounded": false,
"upperLimit": 1
}
},
"labels": [
{
"locale": "en",
"text": "Main Policy Holder"
}
],
"role": "Partner",
"documentModel": "BusinessPartnerSuper"
}
]
}
}
@endjson
Header:
/header/id
Relationship name.
/header/modelType
Model type must be specified as relationship.
/header/annotations
Roles annotation must be provided.
/header/locales
At least one locale must be provided.
/header/modelReferences/modelType
The type of the referenced model must be specified as document.
/header/modelReferences/reference
Reference of model must be provided.
Content:
197

-- 197 of 334 --

/content/labels
Display label has to contain locale and text fields.
/content/linkDocumentModel
Name of the LM (may be missing indicating that no link document can be assigned), where
model must exist and user must have permission to read.
/content/duplicatesAllowed
Switch for LIST_CANDIDATES operation which filters out already assigned links.
/content/entityCharacteristics
Group for definition of characteristics of the relationships.
/content/entityCharacteristics/role
Role identification. All operations are using this property to identify which side of the
relationship is being referred to.
/content/entityCharacteristics/ordered
Boolean switch that determines whether query results traversing this relationship return links
in user-specified rank order (true) or in default link-ID order (false). This flag is directional: it
applies when the annotated role is the source (navigated-from) side of a query. For example, if
role A has ordered: true and role B has ordered: false, querying FROM A to B returns results in
rank order, but querying FROM B to A returns results in link-ID order.
/content/entityCharacteristics/documentModel
Reference to the DM which should be used for the document for the respective side of the
relationship.
/content/entityCharacteristics/linkConstraints/multiplicity
Group for multiplicity constraints of links.
/content/entityCharacteristics/linkConstraints/multiplicity/upperLimit
Maximal number of occurrences of the entity in the relationship. ADD_LINK operation will not
allow creation if after the creation this limit would be breached.
/content/entityCharacteristics/linkConstraints/multiplicity/unbounded
Switches off upperLimit functionality.
Relationship Migration
To migrate Relationship Model files, first install or update the migration tool with:
npm install -g @com.mgmtp.a12.dataservices/dataservices-relationship-model-migration
Then, run the following command to perform the migration:
198

-- 198 of 334 --

relationship-model-migration <path to relationship model file or directory> --backup
Examples:
# file
relationship-model-migration my-relationship-model.json --backup
# folder
reltionship-model-migration . --backup
Note
1. If <path to relationship model file or directory> is a directory, the migration tool will
recursively search for Relationship Model files to migrate.
2. If Relationship Model files are not under version control, use --backup (alias -b) flag to create
backups for model files. This flag is optional.
3. Use --help (alias -h) flag to show all available options.
Compose Documents
Setup Server for CDD
DS natively support CDM feature there is no additional setup needed to use it.

The use of CDMs in conjunction with "unbounded" multiplicity of relationship
models presents a security risk, enabling client to retrieve documents and links for
a CDM bypassing Links.pageLimit and Documents.pageLimit. Consequently, there
exists the room for DoS attack by incorporating a CDM containing a root document
with an extensive array of linked documents. It is recommended to reduce the
multiplicity of the roles in the Relationship Model to a value that aligns with the
project’s contextual requirements.
CDM Handling
A CDM is a document model which contains CDM annotations and is used by Data Services in QUERY
operation and QueryService using cdd and document-graph projections.
199

-- 199 of 334 --

header
content
id 	NaturalPersonCDM	
modelType 	document	
modelVersion 	28.0.2	
locales
labels
annotations	
modelReferences
code 	en
code 	de
locale 	en	
text 	NaturalPerson CDM
name 	cdm.queryRoot	
value 	NaturalPerson-document
name 	roles	
value 	admin,sysadmin
alias 	Address-document.json	
modelType 	document	
purpose 	include	
reference 	Address-document
alias 	NaturalPerson-document.json	
modelType 	document	
purpose 	include	
reference 	NaturalPerson-document
alias 	Contract-document.json	
modelType 	document	
purpose 	include	
reference 	Contract-document
alias 	CoInsurerAdditionalFields.json	
modelType 	document	
purpose 	include	
reference 	CoInsurerAdditionalFields	
modelInfo	
modelConfig	
modelRoot
name 	NewDocumentModel	
immutable 	¬ false
decimalSeparator 	.	
timeZone 	UTC	
conditionLanguage 	code 	en_US
rootGroups
type 	Group	
id 	include_6ab0d	
name 	businessPartner	
Group
repeatability 	1	
modelAlias 	NaturalPerson-document.json
type 	Group	
id 	group_f7a8d	
name 	r_location	
annotations	
Group
name 	cdm.targetDocumentModel	
value 	Address-document
name 	cdm.targetRole	
value 	address
name 	cdm.relationship	
value 	Location
name 	cdm.sourceRole	
value 	businessPartner	
repeatability 	99	
elements	
type 	Group	
id 	include_bfc66	
name 	address	
Group 	repeatability 	1	
modelAlias 	Address-document.json
type 	Group	
id 	group_0548e	
name 	r_postAddress	
annotations	
Group
name 	cdm.targetDocumentModel	
value 	Address-document
name 	cdm.targetRole	
value 	address
name 	cdm.relationship	
value 	PostAddress
name 	cdm.sourceRole	
value 	businessPartner	repeatability 	1	
elements
type 	Group	
id 	include_2e0af	
name 	address	
Group 	repeatability 	1	
modelAlias 	Address-document.json	
type 	Field	
id 	field_8d767	
name 	hasPostalAddress	
Field
fieldType	
label
transient 	¬ tr ue
type 	BooleanType 	locale 	en	
text 	has postal address
locale 	de	
text 	hat Postanschrift
type 	Computation	
id 	computation_108e1	
name 	computeHasPostalAddress	
Computation	
computedFieldRelPath 	../hasPostalAddress	
computationAlternatives	
errorMessage
operation 	True	
precondition 	GroupFilled(RuleGroup) and AllFieldsFilled(address)
operation 	False	
precondition 	GroupFilled(RuleGroup) and NotAllFieldsFilled(address)
locale 	en	
text 	error text for computation of computeHasPostalAddress
locale 	de	
text 	error text for computation of computeHasPostalAddress
type 	Group	
id 	group_86397	
name 	r_CoInsurer	
annotations	
Group
name 	cdm.relationship	
value 	CoInsurer
name 	cdm.targetDocumentModel	
value 	contract-document
name 	cdm.targetRole	
value 	contract
name 	cdm.sourceRole	
value 	businessPartner	
repeatability 	1	
elements
type 	Group	
id 	include_7b4ef	
name 	contract	
Group 	repeatability 	1	
modelAlias 	Contract-document.json	
type 	Group	
id 	group_62bc2	
name 	relationship	
Group 	repeatability 	1	
elements	
type 	Group	
id 	include_8035d	
name 	additionalFields	
Group 	repeatability 	1	
modelAlias 	CoInsurerAdditionalFields.json	
type 	Field	
id 	field_41751	
name 	t_docRef	
Field	
fieldType	
label
transient 	¬ tr ue
type 	StringType 	locale 	en	
text
locale 	de	
text	
type 	Field	
id 	field_7759c	
name 	isCoInsured	
Field	
fieldType	
label
transient 	¬ tr ue
type 	BooleanType	
locale 	en	
text
locale 	de	
text	type 	Computation	
id 	computation_a528f	
name 	computeIsCoInsured	
Computation 	computedFieldRelPath 	../isCoInsured	
computationAlternatives	
errorMessage
operation 	True	
precondition 	FieldFilled(t_docRef)
operation 	False	
precondition 	FieldNotFilled(t_docRef)
locale 	en	
text 	error text for computation of computeIsCoInsured
locale 	de	
text 	error text for computation of computeIsCoInsured
Figure 22. Example of CDM structure
Load CDM
Similarly, as with relationship models, we only allow access to a CDM if the user has permissions to
see all document models and relationship models included in this CDM. The computation of these
permission checks is very costly and for this reason we are caching the result of permission checks.
User is allowed to load CDM if
• (s)he is allowed to load the model itself as well as it’s cdm.queryRoot document model.
• (s)he is allowed to load all relationships in the groups annotated with cdm.relationship.
Authorization check for each RM consists of authorization check of the RM model itself and
authorization checks of source, target, and link document model.
A CDM reference will be included in the modelGraph if the current user is allowed to load the CDM.
This means we should have 3 root nodes in the model graph now: documentModels,
relationshipModels and CDMs.
CDD Handling
Compose documents (CDDs) are the documents of the CDM models. These documents are not
regular documents and therefore they are not persisted together with the regular documents, and
they cannot be retrieved using document or document-graph projection from QUERY operation.
DS provides only one API to load constructed CDDs based on the query specificaiton. To achieve this
please use cdd projection of the QUERY operation or underlying service layer.
document-graph Projection
A CDM defines how links and documents can be organized into a composed data document (CDD).
A document graph contains all documents and links which are needed to construct a CDD.
200

-- 200 of 334 --

When loading a document graph the server will apply the following principles:
• Complete documents are always loaded from the persistent store.
• The access to the links and documents will be secured. The access to the models and documents
will be checked in a fail fast way. So, in order for the operation to succeed the user must have all
permissions as described below :
◦ All models will be loaded for the CDM to check if the user has permissions to see the CDM.
◦ All links and link documents will be loaded using repository access injection.
▪ This means that the missing link or link document authorization violation will create the
same result: Less data in the document graph.
Standard QUERY response is provided for this projection. The root documents are stored in entries
property whereas links and link documents are stored in links property of the Query response.
cdd Projection
For Overview Engine use cases it is necessary to be able to search in the CDDs securely with full
support of pagination, filtering and sorting. To address these use-cases we introduced the cdd
projection of QUERY operation. This projection is a very similar to document without links. There are
however couple of differences in the following aspects:
• There is no heterogeneity allowed for cdd projection. A12 does not support CDM heterogeneity,
only heterogeneity in the DMs used in CDM.
• cdd projection works only for the CDM models. Using regular DMs will cause an error.
• Repository access is not required for the CDM fields but the existing repository access
constraints will be constructed bottom up from existing constraints on included models. I.e.:
ContractCDM using BusinessPartner and Address DM will result in cdd projections where
repository access constraints will be added to the query from Contract, BusinessPartner and
Address DM. See more examples in Security section.
• CDDs are constructed during runtime therefore make sure that only data needed for Overview
or Tree engine are requested from DS. The bigger the CDM, the more data needs to be loaded
and the slower the QUERY response.
• Root constraint (constraint in the root query) of the cdd projection can only work for Root DM of
the CDM. Using linked DM is possible but then the has operator must be used.
◦ Using CDM fields for linked DM will not work. The field names from the linked DM must be
used.
• CDD projection may produce empty objects in the output. This is valid and does not affect the
underlying data; it only represents empty values. For performance reasons we do not prune all
empty objects. Doing so would require a full traversal of the entire object graph, which is
inefficient.
Java API
There are two ways how to use Data Services Java API:
201

-- 201 of 334 --

• call its functionality to get or modify data
• extend the functionality and behavior of Data Services which is a more advanced topic.
Let’s learn about both approaches in the next two chapters.
Calling Data Services Functionality
Data Services exposes several functional areas via its Java API, including model management,
document operations, and supporting utilities. You can fetch, update, and search models and
documents, leverage helper classes, and handle exceptions according to defined rules. This chapter
introduces these capabilities.
Model API
To work with models, use ModelService. This service handles generic models, including Document
Models, Relationship Models, and others. When you load a model with this service, you receive a
GenericModel type.
To obtain a specific model type (document, relationship, or CDM) use IModelLoader with desired
model type, i.e.: IModelLoader<IDocumentModel>.
202

-- 202 of 334 --

dataservices
model
relationship
persistence
persistence
utils
ModelTypeService
findAllSubtypes(
documentModelName: java.lang.String
): java.util.Set
findDirectSubtypes(
documentModelName: java.lang.String
): java.util.Set
findModelNameAndAllSubtypes(
documentModelName: java.lang.String
): java.util.Set
isSubtype(
parentModelName: java.lang.String
testedModelName: java.lang.String
): boolean
ModelService
create(
modelContent: java.lang.String
): com.mgmtp.a12.dataservices.model.GenericModel
update(
modelContent: java.lang.String
): com.mgmtp.a12.dataservices.model.GenericModel
delete(
modelId: java.lang.String
): boolean
load(
modelId: java.lang.String
): com.mgmtp.a12.dataservices.model.GenericModel
load(
modelIds: java.util.Collection
): java.util.Collection
findAllHeadersByType(
type: java.lang.String
): java.util.List
findAllHeaders(): java.util.Collection
exists(
header: com.mgmtp.a12.model.header.Header
): boolean
getSupportingRepository(
header: com.mgmtp.a12.model.header.Header
): com.mgmtp.a12.dataservices.model.persistence.IModelRepository
RelationshipModelLoader
loadAllRelationshipModels(): java.util.Set
IModelLoader
loadModel(
modelId: java.lang.String
): T
IModelRepository
supports(
header: com.mgmtp.a12.model.header.Header
): boolean
save(
header: com.mgmtp.a12.model.header.Header
modelContent: java.lang.String
): com.mgmtp.a12.dataservices.model.GenericModel
update(
newHeader: com.mgmtp.a12.model.header.Header
newModelContent: java.lang.String
): com.mgmtp.a12.dataservices.model.GenericModel
delete(
header: com.mgmtp.a12.model.header.Header
): boolean
exists(
header: com.mgmtp.a12.model.header.Header
): boolean
load(
header: com.mgmtp.a12.model.header.Header
): java.util.Optional
IModelReadRepository
readModel(
modelId: java.lang.String
): T
Figure 23. Model API class diagram
IModelRepository
A model repository enables client projects to implement custom behavior for model CRUD
operations.
ModelService uses the supports method of IModelRepository to find a repository implementation for
a given model. If multiple implementations are found, the first one is loaded. Custom repository
developers must define the order in which repositories are loaded.
See the IModelRepository interface for details.
The save, load, and update methods return a GenericModel, representing any model type and
containing both the header and string content. To obtain another type, such as IDocumentModel, pass
the string content of the GenericModel to a suitable deserializer function.
203

-- 203 of 334 --

Document API
DocumentService
DocumentService is the entrypoint for document persistence and supports multiple document
repositories. The service selects the appropriate repository implementation for each request,
allowing customization by implementing IDocumentRepository. For details, see document repository
customizations.
dataservices
d o c u m e n t
persistence
suppor t	
DocumentService
create(
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
locale: java.util.Locale
): com.mgmtp.a12.dataservices.document.DataServicesDocument
create(
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
locale: java.util.Locale
validationStrategy: com.mgmtp.a12.dataservices.document.persistence.DocumentValidationStrategy
computationStrategy: com.mgmtp.a12.dataservices.document.persistence.DocumentComputationStrategy
): com.mgmtp.a12.dataservices.document.DataServicesDocument
update(
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
newDocument: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
locale: java.util.Locale
): com.mgmtp.a12.dataservices.document.DataServicesDocument
update(
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
newDocument: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
locale: java.util.Locale
validationStrategy: com.mgmtp.a12.dataservices.document.persistence.DocumentValidationStrategy
computationStrategy: com.mgmtp.a12.dataservices.document.persistence.DocumentComputationStrategy
): com.mgmtp.a12.dataservices.document.DataServicesDocument
update(
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
documentPar ts: java.util.List
locale: java.util.Locale
): com.mgmtp.a12.dataservices.document.DataServicesDocument
update(
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
documentPar ts: java.util.List
locale: java.util.Locale
validationStrategy: com.mgmtp.a12.dataservices.document.persistence.DocumentValidationStrategy
computationStrategy: com.mgmtp.a12.dataservices.document.persistence.DocumentComputationStrategy
): com.mgmtp.a12.dataservices.document.DataServicesDocument
load(
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
): java.util.Optional
delete(
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
): void
deleteAll(
documentReferences: java.util.Collection
): void
IDocumentRepository
supports(
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
): boolean
supports(
modelName: java.lang.String
metadata: java.util.Optional
): boolean
findByDocumentReference(
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
): java.util.Optional
findAllDocRefsForModel(
modelId: java.lang.String
): java.util.List
findAllDocRefsForModel(
modelId: java.lang.String
pageable: org.springframework.data.domain.Pageable
): java.util.List
findDocumentsByDocRefs(
docRefs: java.util.List
): java.util.List
create(
dataServicesDocument: com.mgmtp.a12.dataservices.document.DataServicesDocument
): void
update(
dataServicesDocument: com.mgmtp.a12.dataservices.document.DataServicesDocument
): void
delete(
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
): void
deleteAll(
modelName: java.lang.String
documentReferences: java.util.Collection
): void
DocumentSuppor t
convertJSONToDocument(
documentModelName: java.lang.String
jsonDocument: java.io.Reader
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
convertJSONToDocument(
documentModelName: java.lang.String
jsonDocument: java.io.Reader
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
convertDocumentToJSON(
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
writer: java.io.Writer
): void
conver tToDocumentSpec(
dataServicesDocument: com.mgmtp.a12.dataservices.document.DataServicesDocument
): com.mgmtp.a12.dataservices.document.DocumentSpec
resolveLocale(
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
preferredLocale: java.util.Locale
skipNonExisting: boolean
): java.util.Locale
deserialize(
documentModelName: java.lang.String
reader: java.io.Reader
deserializationConfig: com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
serialize(
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
writer: java.io.Writer
serializationConfig: com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig
): void
convertJSONToDocument(
documentModelName: java.lang.String
jsonNode: tools.jackson.databind.JsonNode
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
convertJSONToDocument(
documentModelName: java.lang.String
jsonNode: tools.jackson.databind.JsonNode
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
Figure 24. Document API class diagram
Document Querying
All data retrieval operations are performed by QueryService. For more information, see Query API.
In fact, there are
204

-- 204 of 334 --

• GET_DOCUMENT operation and
• DocumentService#load method
which both internally use QueryService to load the data.
IDocumentRepository
IDocumentRepository enables client projects to implement custom persistence behavior for specific
documents. DocumentService uses the supports method to determine the appropriate repository for a
given document. Multiple implementations are allowed, but only one repository must be selected
per document. The supports method accepts a DocumentV2 and returns a boolean indicating support.
For example, repositories can be separated by model name: one implementation for documentModel1,
another for documentModel2. Each document must reside in a single repository to prevent
duplication.
During persistence, DocumentService iterates through all repository implementations and selects one
using the following logic:
Selecting the appropriate repository
private IDocumentRepository getDocumentRepository(DocumentV2 document) {
return documentRepositories.stream()
.filter(repository -> repository.supports(document))
.findFirst()
.orElseThrow(() -> {
log.error(String.format(REPOSITORY_NOT_FOUND, document
.getDocumentModelId()));
return new NotFoundException(String.format(REPOSITORY_NOT_FOUND,
document.getDocumentModelId()));
});
}
If multiple implementations support a document, only the first is used. Developers must define the
repository loading order. We recommend separating IDocumentRepository implementations by
document model to avoid duplicate DocumentReference values, which is invalid.
The first supporting repository is chosen, so ordering is crucial. Use the
org.springframework.core.annotation.Order annotation to specify precedence. For example, the
default implementation
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentRepository is assigned
the lowest precedence, ensuring custom implementations are checked first.
Repositories can also target specific documents regardless of model. The supports method allows
selection based on any document field. To ensure a specific implementation is always checked first,
use:
Setting repository precedence
@Order(Ordered.HIGHEST_PRECEDENCE)
205

-- 205 of 334 --

 com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentReposit
ory is internal and not intended for extension.
Attachment API
The AttachmentService allows you to:
• create attachments with thumbnails
• retrieve download URLs for attachments and thumbnails
The AttachmentHeaderService can be used to:
• create, load, and delete an AttachmentHeader
• assign and unassign an attachment to or from a GenericReference, typically a DocumentReference
• retrieve a list of stale attachments (attachments unassigned longer than a configurable period)
The AttachmentHeaderRepository provides the repository layer for the AttachmentHeaderService.
dataservices
attachment
persitence
header
AttachmentService
createAttachment(
is: java.io.InputStream
filename: java.lang.String
documentModelName: java.lang.String
pathToField: java.lang.String
annotations: java.util.List
): com.mgmtp.a12.dataservices.attachment.AttachmentHeader
findAttachmentUrl(
attachmentId: java.lang.String
docRef: com.mgmtp.a12.dataservices.document.DocumentReference
): java.util.Optional
findThumbnailUrl(
attachmentId: java.lang.String
type: com.mgmtp.a12.dataservices.attachment.ThumbnailType
): java.util.Optional
findThumbnailUrl(
header: com.mgmtp.a12.dataservices.attachment.AttachmentHeader
type: com.mgmtp.a12.dataservices.attachment.ThumbnailType
): java.util.Optional
AttachmentHeaderRepository
create(
header: com.mgmtp.a12.dataservices.attachment.AttachmentHeader
): void
findById(
attachmentId: java.lang.String
): java.util.Optional
delete(
attachmentId: java.lang.String
): void
addReference(
header: com.mgmtp.a12.dataservices.attachment.AttachmentHeader
reference: com.mgmtp.a12.dataservices.attachment.AttachmentReference
): void
removeReference(
header: com.mgmtp.a12.dataservices.attachment.AttachmentHeader
reference: com.mgmtp.a12.dataservices.attachment.AttachmentReference
): void
findAndRemoveReferencesFor(
documentReferences: java.util.Collection
): java.util.List
referenceExists(
header: com.mgmtp.a12.dataservices.attachment.AttachmentHeader
reference: com.mgmtp.a12.dataservices.attachment.AttachmentReference
): boolean
findUnassignedAttachmentsOlderThan(
threshold: java.time.Instant
): java.util.List
AttachmentHeaderService
create(
header: com.mgmtp.a12.dataservices.attachment.AttachmentHeader
): com.mgmtp.a12.dataservices.attachment.AttachmentHeader
delete(
attachmentId: java.lang.String
): void
load(
attachmentId: java.lang.String
): java.util.Optional
loadUnassignedAttachmentsOlderThan(
tmpAttachmentExpireHours: int
): java.util.List
assignAttachment(
header: com.mgmtp.a12.dataservices.attachment.AttachmentHeader
reference: com.mgmtp.a12.dataservices.attachment.AttachmentReference
): void
unAssignAttachment(
header: com.mgmtp.a12.dataservices.attachment.AttachmentHeader
reference: com.mgmtp.a12.dataservices.attachment.AttachmentReference
): void
unAssignAttachments(
documentReferences: java.util.Collection
): void
Figure 25. Attachment service class diagram
Attachments and thumbnails are stored using the Content Store, which can run in embedded or
standalone mode, depending on the configuration property
mgmtp.a12.dataservices.attachments.extension.embedded.enabled.
When creating an attachment via AttachmentService#createAttachment, the provided
documentModelName is used to:
• check ModelRead permission
• determine if the attachment should be stored as public or private content, based on the
configuration property mgmtp.a12.dataservices.attachments.type.publicType.models
206

-- 206 of 334 --

Relationship API
With the Relationship API, you can establish and maintain relationship links between documents.
Relationships are defined between exactly two documents: the source and the target. Optionally, a
relationship can include additional information in a link document.
dataservices
relationship
spec
persistence
factory
validation
java
io
RelationshipLinkService
create(
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
create(
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor	
linkDocument: java.lang.String	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
create(
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor	
linkDocRef: com.mgmtp.a12.dataservices.document.DocumentReference	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
update(
id: java.lang.String	
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor	
linkDocument: java.lang.String	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
delete(
id: java.lang.String	
): void
deleteAllByIds(	
id: java.util.Set	
): void
load(
id: java.lang.String	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
relink(
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor	
linkId: java.lang.String	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
load(
specification: com.mgmtp.a12.dataservices.relationship.RelationshipLinkSpecification	
pageable: org.springframework.data.domain.Pageable	
): org.springframework.data.domain.Page
deleteByRoleDocRefs(	
documentReferences: java.util.Collection	
): void
RelationshipLink
getId(): java.lang.String
getRelationshipModel(): java.lang.String
getCreatedAt(): java.time.Instant
getLinkDocumentDocRef(): com.mgmtp.a12.dataservices.document.DocumentReference
getRoles(): java.util.Map
setLinkDocumentDocRef(	
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference	
): void
addRole(
relationshipRole: com.mgmtp.a12.dataservices.relationship.RelationshipRole	
): void
RelationshipRole
getName(): java.lang.String
getDocRef(): com.mgmtp.a12.dataservices.document.DocumentReference
getOrder(): java.lang.String
setOrder(
order: java.lang.String	
): void
LinkDescriptor
relationshipModel: java.lang.String
entities: java.util.List<com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec>
linkDocumentDocRef: com.mgmtp.a12.dataservices.document.DocumentReference
predecessorLinkRef: java.lang.String
position: com.mgmtp.a12.dataservices.relationship.spec.LinkPosition
getSourceRole(): com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec
getTargetRole(): com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec
RelationshipLinkSpec
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor
id: java.lang.String
RelationshipRoleSpec
role: java.lang.String
modelName: java.lang.String
docRef: com.mgmtp.a12.dataservices.document.DocumentReference
RelationshipLinkRepository
create(
relationshipLink: com.mgmtp.a12.dataservices.relationship.RelationshipLink	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
update(
id: java.lang.String	
relationshipLink: com.mgmtp.a12.dataservices.relationship.RelationshipLink	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
delete(
id: java.lang.String	
): void
deleteAllByIds(	
ids: java.util.Set	
): void
findById(
id: java.lang.String	
): java.util.Optional
findByRelationshipModelName(	
modelName: java.lang.String	
pageable: org.springframework.data.domain.Pageable	
): org.springframework.data.domain.Page
findByRelationshipModelNameAndSource(	
relationshipModelName: java.lang.String	
sourceRole: java.lang.String	
sourceDocRef: com.mgmtp.a12.dataservices.document.DocumentReference	
pageable: org.springframework.data.domain.Pageable	
): org.springframework.data.domain.Page
findByRelationshipModelNameAndSourceAndTarget(	
relationshipModelName: java.lang.String	
role1: java.lang.String	
docRef1: com.mgmtp.a12.dataservices.document.DocumentReference	
role2: java.lang.String	
docRef2: com.mgmtp.a12.dataservices.document.DocumentReference	
pageable: org.springframework.data.domain.Pageable	
): org.springframework.data.domain.Page
findByLinkDocument(	
docRef: com.mgmtp.a12.dataservices.document.DocumentReference	
pageable: org.springframework.data.domain.Pageable	
): org.springframework.data.domain.Page
findTerminatingNodes(	
relationshipModelName: java.lang.String	
terminatingRoleName: java.lang.String	
pageable: org.springframework.data.domain.Pageable	
): org.springframework.data.domain.Page
countByRoles(	
relationshipModelName: java.lang.String	
sourceRole: com.mgmtp.a12.dataservices.relationship.RelationshipRole	
targetRole: com.mgmtp.a12.dataservices.relationship.RelationshipRole	
): long
countByRole(	
relationshipModelName: java.lang.String	
role: java.lang.String	
docRef: com.mgmtp.a12.dataservices.document.DocumentReference	
): long
countByRelationshipModel(	
relationshipModelName: java.lang.String	
): long
findAllByRoleDocRef(	
documentReferences: java.util.Collection	
pageable: org.springframework.data.domain.Pageable	
): org.springframework.data.domain.Page
countByLinkInDocumentDocRefs(	
documentReferences: java.util.Collection	
): long
RelationshipLinkFactory
createLink(	
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor	
linkDocRef: com.mgmtp.a12.dataservices.document.DocumentReference	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
createLink(	
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor	
linkDocRef: com.mgmtp.a12.dataservices.document.DocumentReference	
computedRank: com.mgmtp.a12.dataservices.relationship.internal.ranks.ComputedRank	
): com.mgmtp.a12.dataservices.relationship.RelationshipLink
RelationshipValidationSupport
validateLink(	
linkDescriptor: com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor	
linkDocRef: com.mgmtp.a12.dataservices.document.DocumentReference	
): void
Serializable
Figure 26. Relationship Link API class diagram
The RelationshipLinkService provides functionality to create, load, update, and delete relationships
between documents, including the associated link document.
A LinkDescriptor specifies the link to be created or updated.
Links are identified by their link ID for operations such as loading or deleting.
There are two methods to change an existing link:
• update:
◦ Creates a new link document and assigns it to the link.
◦ Deletes the old link document.
207

-- 207 of 334 --

◦ Does not change the link itself.
• relink:
◦ Creates a new link using the provided LinkDescriptor.
◦ Deletes the old link after un-assigning its link document.
◦ Does not change the link document but assigns it to the new link.
Extension Interfaces
Data Services exposes several public interfaces that allow integration and extension components to
interact with the relationship subsystem without depending on internal implementation classes.
RelationshipLinkRepository
RelationshipLinkRepository is the public persistence interface for relationship links. It provides a
complete set of CRUD operations and query methods.
External components, such as a relational-persistence integration module, should depend on this
interface rather than on the internal JPA repository. Data Services registers a default
implementation as a Spring bean.
The following example shows how to inject and use the repository in a custom component:
@Component
public class CustomRelationshipComponent {
private final RelationshipLinkRepository repository;
public CustomRelationshipComponent(RelationshipLinkRepository repository) {
this.repository = repository;
}
public long countLinks(String modelName) {
return repository.countByRelationshipModel(modelName);
}
}
RelationshipLinkFactory
RelationshipLinkFactory is the public factory interface for creating RelationshipLink instances. It
encapsulates validation and domain object construction.
Inject RelationshipLinkFactory when you need to create relationship links programmatically
without going through the full RelationshipLinkService workflow.
RelationshipValidationSupport
RelationshipValidationSupport defines the validation contract applied when creating relationship
links. See Overriding Relationship Link Validation in the Extending the Server section for details on
208

-- 208 of 334 --

overriding the default validation.
Utility Classes
209

-- 209 of 334 --

Kernel
dataservices-core
dataservices
document
support
utils
java
lang
io
dataservices-models
dataservices
document
reference
common
exception
mapping
DocumentV2
DocumentSupport
convertJSONToDocument(	
documentModelName: java.lang.String	
jsonDocument: java.io.Reader	
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
convertJSONToDocument(	
documentModelName: java.lang.String	
jsonDocument: java.io.Reader	
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference	
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
convertDocumentToJSON(	
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2	
writer: java.io.Writer	
): void
convertToDocumentSpec(	
dataServicesDocument: com.mgmtp.a12.dataservices.document.DataServicesDocument	
): com.mgmtp.a12.dataservices.document.DocumentSpec
resolveLocale(	
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2	
preferredLocale: java.util.Locale	
skipNonExisting: boolean	
): java.util.Locale
deserialize(	
documentModelName: java.lang.String	
reader: java.io.Reader	
deserializationConfig: com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig	
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
serialize(
document: com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2	
writer: java.io.Writer	
serializationConfig: com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig	
): void
convertJSONToDocument(	
documentModelName: java.lang.String	
jsonNode: tools.jackson.databind.JsonNode	
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
convertJSONToDocument(	
documentModelName: java.lang.String	
jsonNode: tools.jackson.databind.JsonNode	
documentReference: com.mgmtp.a12.dataservices.document.DocumentReference	
): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
ConfigurationParsingUtils
isSingleAsterisk(	
inputList: java.util.Collection	
): boolean
matchOrAll(	
value: java.lang.String	
inputList: java.util.Collection	
): boolean
CharSequence
Comparable 	T
Serializable
DataServicesDocument
getKernelDocument(): com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2
getMetadata(): com.mgmtp.a12.dataservices.document.DataServicesDocumentMetadata
DocumentReference
documentModelName: java.lang.String
documentId: java.lang.String
isValid(): boolean
toString(): java.lang.String
length(): int
charAt(
index: int	
): char
subSequence(	
start: int	
end: int	
): java.lang.CharSequence
compareTo(	
o: com.mgmtp.a12.dataservices.document.DocumentReference	
): int
setIfValid(
docRef: java.lang.String	
): void
setIfValid(
documentModelName: java.lang.String	
documentId: java.lang.String	
): void
DocumentSpec
docRef: com.mgmtp.a12.dataservices.document.DocumentReference
documentModelName: java.lang.String
document: java.lang.String
GenericReference
GenericThrowableMapper
shouldAddExceptionToHeader: boolean
getHttpStatus(	
exception: E	
): org.springframework.http.HttpStatus
getExceptionKey(	
exception: E	
): java.lang.String
getEntity(
exception: E	
): java.lang.Object
shouldLogStackTrace(	
exception: E	
): boolean
log(
exception: E	
): void
getErrorLevel(	
exception: E	
): com.mgmtp.a12.dataservices.common.exception.ErrorLevel
getSecureMessage(	
exception: E	
): java.lang.String
constructSafeExceptionHeaderMessage(	
message: java.lang.String	
): java.lang.String
getErrorCode(	
ex: E	
): java.lang.String
getExceptionMessage(	
exception: E	
): java.lang.String
Figure 27. Document Utility class diagram
210

-- 210 of 334 --

Document Utility
DocumentSupport provides utility methods to convert between JSON representation and DocumentV2 as
well as for serializing and deserializing documents.
Configuration Parsing Utilities
ConfigurationParsingUtils provides helper methods for evaluating configuration values that
support a single-asterisk wildcard.
These utilities are intended for use in Data Services configuration code and extension components
that need to evaluate allow-list configurations.
Wildcard Matching
Many Data Services configuration properties accept either an explicit list of values or the special
wildcard value *, which matches all values. ConfigurationParsingUtils encapsulates this pattern.
isSingleAsterisk(Collection<String>)
Returns true if the collection contains exactly one entry equal to "*". Use this to detect a match-
all configuration before iterating over allowed values.
matchOrAll(String, Collection<String>)
Returns true if the value is present in the list, or if the list is a single-asterisk wildcard. Returns
false if the list is empty or null.
 If "*" appears alongside other entries in the list, it is treated as a literal value
and a warning is logged. A mixed list such as ["*", "someValue"] does not
behave as a wildcard.
The following example shows typical usage in a configuration evaluation:
List<String> allowedModels = configuration.getAllowedModels();
if (ConfigurationParsingUtils.matchOrAll(modelName, allowedModels)) {
// model is allowed
}
Exceptions and its Mapping
Data Services defines a set of core exceptions (see Figure 28) to ensure proper logging and accurate
HTTP status code mapping when exceptions propagate to the HTTP layer.
All major exception types are centrally handled by
com.mgmtp.a12.dataservices.server.rest.exception.mapping.DataServicesExceptionsHandler, which
maps them to appropriate responses.
211

-- 211 of 334 --

java
lang	io
dataservices
common
exception
mapping
CharSequence	RuntimeException	Serializable
LocalizedEntry
key: java.lang.String
defaultMessage: java.lang.String
AnonymityException
getAnonymityMessage(): java.lang.String
BaseException
getLevel(): com.mgmtp.a12.dataservices.common.exception.ErrorLevel
getShortMessage(): com.mgmtp.a12.dataservices.common.exception.BaseException.LocalizedMessageWithPriority
setShortMessage(	
newMessage: com.mgmtp.a12.dataservices.common.exception.BaseException.LocalizedMessageWithPriority	
): void
setShortMessage(	
newMessage: com.mgmtp.a12.dataservices.common.LocalizedEntry	
priority: com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority	
): void
setShortMessage(	
shortMessage: com.mgmtp.a12.dataservices.common.LocalizedEntry	
): void
getLongMessage(): com.mgmtp.a12.dataservices.common.exception.BaseException.LocalizedMessageWithPriority
setLongMessage(	
newMessage: com.mgmtp.a12.dataservices.common.exception.BaseException.LocalizedMessageWithPriority	
): void
setLongMessage(	
newMessage: com.mgmtp.a12.dataservices.common.LocalizedEntry	
priority: com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority	
): void
setLongMessage(	
longMessage: com.mgmtp.a12.dataservices.common.LocalizedEntry	
): void
withShortMessage(	
key: java.lang.String	
defaultMessage: java.lang.String	
): com.mgmtp.a12.dataservices.common.exception.BaseException
withShortMessage(	
key: java.lang.String	
defaultMessage: java.lang.String	
priority: com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority	
): com.mgmtp.a12.dataservices.common.exception.BaseException
withLongMessage(	
key: java.lang.String	
defaultMessage: java.lang.String	
): com.mgmtp.a12.dataservices.common.exception.BaseException
withLocalizedMessage(	
key: java.lang.String	
defaultMessage: java.lang.String	
): com.mgmtp.a12.dataservices.common.exception.BaseException
withAnonymityMessage(	
anonymityMessage: java.lang.String	
): com.mgmtp.a12.dataservices.common.exception.BaseException
updateShortMessage(	
key: java.lang.String	
message: java.lang.String	
priority: com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority	
): com.mgmtp.a12.dataservices.common.exception.BaseException
updateShortMessage(	
key: java.lang.String	
message: java.lang.String	
): com.mgmtp.a12.dataservices.common.exception.BaseException
updateLongMessage(	
message: java.lang.String	
priority: com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority	
): com.mgmtp.a12.dataservices.common.exception.BaseException
withLongMessage(	
key: java.lang.String	
mess: java.lang.String	
priority: com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority	
): com.mgmtp.a12.dataservices.common.exception.BaseException
getMessage(): java.lang.String
BaseError
getLevel(): com.mgmtp.a12.dataservices.common.exception.ErrorLevel
getShortMessage(): com.mgmtp.a12.dataservices.common.LocalizedEntry
getLongMessage(): com.mgmtp.a12.dataservices.common.LocalizedEntry
getErrorDetail(): com.mgmtp.a12.dataservices.common.exception.ErrorDetail
LocalizedMessageWithPriority
priority: com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority
MessagePriority
LOW, MEDIUM, HIGH;
values(): Array
valueOf(
name: java.lang.String	
): com.mgmtp.a12.dataservices.common.exception.BaseException.MessagePriority
GenericThrowableMapper
shouldAddExceptionToHeader: boolean
getHttpStatus(	
exception: E	
): org.springframework.http.HttpStatus
getExceptionKey(	
exception: E	
): java.lang.String
getEntity(
exception: E	
): java.lang.Object
shouldLogStackTrace(	
exception: E	
): boolean
log(
exception: E	
): void
getErrorLevel(	
exception: E	
): com.mgmtp.a12.dataservices.common.exception.ErrorLevel
getSecureMessage(	
exception: E	
): java.lang.String
constructSafeExceptionHeaderMessage(	
message: java.lang.String	
): java.lang.String
getErrorCode(	
ex: E	
): java.lang.String
getExceptionMessage(	
exception: E	
): java.lang.String
Figure 28. Exceptions class diagram
Extending the Server
This section contains guidelines how to extend the Data Services server.
Transaction Boundaries
This section provides critical guidelines for extension developers regarding transaction
management in Data Services. Improper handling of transactions in custom extensions can lead to
212

-- 212 of 334 --

data inconsistencies between the document database and search index.
Understanding Transaction Boundaries
Data Services ensures atomicity between document persistence and index updates by executing
both operations within the same database transaction. This design guarantees that:
• If document creation succeeds, the corresponding index entry is also created.
• If document update succeeds, the corresponding index entry is also updated.
• If document deletion succeeds, the corresponding index entry is also deleted.
• If any operation fails, both the document change and the index change are rolled back together.

Golden Rule: Never break the transaction boundary between document
operations and index updates.
If your custom extension creates a separate transaction or executes operations
asynchronously, index consistency can no longer be guaranteed.
READ_COMMITTED Isolation Level Implications
Data Services uses PostgreSQL’s READ_COMMITTED isolation level. This has important
implications for code that runs outside the main transaction:
• Uncommitted changes are not visible: Any data modified within the current transaction is not
visible to queries running in a separate transaction (including async operations) until the
original transaction commits
• Non-repeatable reads: If you read the same data twice from a separate transaction, you may
get different results if another transaction committed changes in between
• Stale data risk: Asynchronous operations that query data may see an older state of the
database, not reflecting changes made in the originating transaction

When using asynchronous operations or separate transactions, be aware that:
• Queries executed asynchronously will not see uncommitted changes from the
calling transaction
• Data read before an async operation starts may differ from what the async
operation reads
• Post-commit listeners see committed data, but concurrent transactions may
have modified it since
For detailed information on transaction isolation, see Transaction Management
and Concurrency.
Incorrect Patterns to Avoid
The following code patterns break transaction boundaries and must not be used in extensions that
interact with document operations.
213

-- 213 of 334 --

Using REQUIRES_NEW Propagation
Do not use REQUIRES_NEW for document-related operations. When the calling transaction fails and
rolls back, the nested transaction created by REQUIRES_NEW has already committed. This leaves the
document in an inconsistent state with its index.
Manual Transaction Management
Do not create manual transactions for document operations. Manual transaction management
bypasses Spring’s transaction propagation, creating isolated transaction boundaries that do not
participate in the calling transaction.
Asynchronous Processing During Document Operations
Do not use @Async during document operations. Asynchronous methods run in a separate thread
with their own transaction context. If the original transaction rolls back, the asynchronous update
may have already committed or may commit later, causing inconsistency.

Due to READ_COMMITTED isolation, async methods cannot see uncommitted
changes from the calling transaction. Any data you pass to the async method or
that the async method queries will reflect the database state before uncommitted
changes.
Using CompletableFuture for Document Operations
Do not use CompletableFuture for document operations. CompletableFuture.runAsync() executes the
lambda in a different thread pool, completely outside the transaction boundary of the calling
method.
 Code running in a CompletableFuture operates in a separate transaction context.
Due to READ_COMMITTED isolation, it will not see any uncommitted changes from
the originating transaction and may read stale data.
Correct Patterns to Follow
Using Default REQUIRED Propagation
Use default propagation or explicit REQUIRED. The REQUIRED propagation (default) joins the existing
transaction if one exists, or creates a new one if not. This ensures all operations participate in the
same transaction boundary.
Using Synchronous Event Listeners
Synchronous event listeners participate in the same transaction. Synchronous event listeners
execute within the same thread and transaction as the event publisher. Using
TransactionPhase.BEFORE_COMMIT ensures the listener participates in the same transaction.
Using Asynchronous Processing Only for Read Operations
Async is acceptable for read-only operations. Read-only operations do not modify data, so they
cannot cause index inconsistency. Asynchronous processing is acceptable for queries and lookups.
214

-- 214 of 334 --


Be aware that async read operations run in their own transaction context. Due to
READ_COMMITTED isolation, they will not see uncommitted changes from the
calling transaction. If your async read depends on data being modified in the
current transaction, wait for the transaction to commit first or use a post-commit
event listener.
Using Post-Commit Asynchronous Processing
Async after transaction commits for non-critical processing. Using TransactionPhase.AFTER_COMMIT
ensures the listener only runs after the transaction successfully commits. At this point, both
document and index changes are persisted, so asynchronous processing is safe.

While post-commit listeners can see the committed changes from the originating
transaction, be aware that other concurrent transactions may have already
modified the data. Due to READ_COMMITTED isolation, subsequent queries in the
post-commit handler may return data that includes changes from other
transactions that committed after your original transaction.
Testing Custom Extensions
When testing custom extensions that interact with documents, verify transaction behavior by
writing integration tests that verify rollback behavior includes both document and index changes.
Custom JPA Entities and Repositories
Data Services registers its JPA infrastructure as named Spring beans (dsEntityManagerFactory,
dsTransactionManager). Extension modules that add custom JPA entities and repositories must
reference these beans explicitly — either to share DS’s datasource or to wire a separate one.
Custom Entities on DS’s Datasource
Use this approach when your custom entities live in the same database as Data Services.
dsEntityManagerFactory automatically picks up packages registered via @EntityScan at startup, so no
separate entity manager factory is needed.
Custom entity configuration sharing DS’s datasource
@Configuration
@EntityScan(basePackages = { "com.example.extra" })
@EnableJpaRepositories(
basePackages = "com.example.extra",
entityManagerFactoryRef = "dsEntityManagerFactory",
transactionManagerRef = "dsTransactionManager"
)
public class ExtraEntityConfiguration {
}
 Both entityManagerFactoryRef and transactionManagerRef must be specified
215

-- 215 of 334 --

explicitly. @EnableJpaRepositories resolves both attributes by name, not by
@Primary. Omitting transactionManagerRef causes the following error when the
repository is first used transactionally:
No bean named 'transactionManager' available: No matching
TransactionManager bean found
for qualifier 'transactionManager' - neither qualifier match nor bean
name match!
This misconfiguration can be silent: if the only code that writes through the
repository is gated by @ConditionalOnProperty and that property is disabled by
default, the server starts without error. The failure only surfaces when the feature
is enabled and the repository is actually called in a transaction.
Repositories wired to dsTransactionManager participate in the same transaction as DS document
operations. See Transaction Boundaries for the applicable rules.
 A working example is available in the examples-extending-server module:
com.mgmtp.a12.examples.extra.ExampleAdditionalEntityConfiguration.
Custom Entities with a Separate Datasource
Use this approach when your custom entities live in a different database. Define your own JPA
beans and reference them from @EnableJpaRepositories.
Custom entity configuration with a separate datasource
// imports omitted for brevity
@Configuration
@EnableJpaRepositories(
basePackages = "com.example.extra",
entityManagerFactoryRef = "extraEntityManagerFactory",
transactionManagerRef = "extraTransactionManager"
)
public class ExtraEntityConfiguration {
@Bean
public DataSource extraDataSource() {
...
}
@Bean
public LocalContainerEntityManagerFactoryBean extraEntityManagerFactory(
@Qualifier("extraDataSource") DataSource dataSource) {
LocalContainerEntityManagerFactoryBean factory =
new LocalContainerEntityManagerFactoryBean();
factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
factory.setDataSource(dataSource);
factory.setPackagesToScan("com.example.extra");
216

-- 216 of 334 --

return factory;
}
@Bean
public PlatformTransactionManager extraTransactionManager(
@Qualifier("extraEntityManagerFactory") EntityManagerFactory factory) {
return new JpaTransactionManager(factory);
}
}

• Do not use the ds prefix for bean names — that namespace is reserved for DS
infrastructure.
• Do not use @EntityScan — those packages are picked up by
dsEntityManagerFactory and would incorrectly include your entity classes in
DS’s persistence unit.
• Do not mark your beans @Primary — DS beans are already @Primary and a
duplicate causes a startup conflict.
 Repositories wired to a separate transaction manager do not share DS’s
transaction. Custom repository operations and DS document operations cannot be
rolled back atomically together.
Data Services BOMs
In your external project, you should apply Data Services BOM to align dependencies with the Data
Services, by specifying:
Gradle example
api enforcedPlatform("com.mgmtp.a12.dataservices:dataservices-
parent:${DS_VERSION}")
Then you can omit version for Data Services artifacts like:
Gradle example alternative
implementation 'com.mgmtp.a12.dataservices:dataservices-server-app'
api 'com.mgmtp.a12.dataservices.common:dataservices-common-lib'
api 'com.mgmtp.a12.dataservices.common:dataservices-common-api'
Events
Data Services are publishing events which can be used by client projects to hook in custom code
before or after operations, or during the server initialization. All those events are annotated with
the @EventDocumentation annotation. Additionally, we offer the
com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener annotation which may
217

-- 217 of 334 --

be used instead of org.springframework.context.event.EventListener, enabling to exclusively track
events published within the same context.
There are several examples in examples-extending-server:
• com.mgmtp.a12.dataservices.examples.relationship.RelationshipLinkListener
• com.mgmtp.a12.dataservices.examples.document.model.migration.DocumentModelMigration
• com.mgmtp.a12.dataservices.examples.document.extensions.ContactModelValidationExtension
• com.mgmtp.a12.dataservices.examples.document.encryption.EncryptionListeners
• com.mgmtp.a12.dataservices.examples.attachment.encryption.AttachmentEncryptionSyncListeners
• com.mgmtp.a12.dataservices.examples.attachment.encryption.AttachmentEncryptionAsyncListener
s
• com.mgmtp.a12.examples.attachment.thumbnails.CustomThumbnailListener
• com.mgmtp.a12.examples.attachment.mime.CustomZipTypeListener
Data Services Events
CddsAfterLoadByQueryEvent
Triggered from the LIST_CDDS operation once the CDDs are completely loaded.
Modification of the payload will only be displayed to the user. No changes will be persisted.
event com.mgmtp.a12.dataservices.document.events.CddsAfterLoadByQueryEvent
QueryAfterPostProcessPhaseEvent
The QueryAfterPostProcessPhaseEvent is published after the query has been executed and post-
processed. Results are available in the QueryPage and can be modified by the event listeners. This
event should be used if the query results need to be modified in all system not just in the JSON-RPC
operation like QueryAfterOperationEvent which is only used in the JSON-RPC operation.
event com.mgmtp.a12.dataservices.query.events.QueryAfterPostProcessPhaseEvent
QueryBeforeExecutionPhaseEvent
The com.mgmtp.a12.dataservices.query.QueryService publishes this event to allow bypassing a call
to the com.mgmtp.a12.dataservices.query.QueryRepository. This enables listeners to provide
alternative means to resolve the query.
Event listeners must provide results in their implementation because
com.mgmtp.a12.dataservices.query.QueryRepository is never called.
event com.mgmtp.a12.dataservices.query.events.QueryBeforeExecutionPhaseEvent
QueryAfterOperationEvent
Published after a query is evaluated and before the results are returned to the caller. This event is
218

-- 218 of 334 --

sent from the RPC layer; it is not published when com.mgmtp.a12.dataservices.query.QueryService
or com.mgmtp.a12.dataservices.query.QueryRepository are called directly.
event com.mgmtp.a12.dataservices.query.operation.events.QueryAfterOperationEvent
QueryBeforeOperationEvent
Allows changes to the query before it is processed by QueryService. This event is triggered with
every QUERY operation. It is not triggered if QueryService or QueryRepository are called directly.
event com.mgmtp.a12.dataservices.query.operation.events.QueryBeforeOperationEvent
AttachmentBeforeCreateEvent
Triggered before the attachment is created.
event com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeCreateEvent
AttachmentThumbnailBeforeSaveEvent
Triggered before the attachment thumbnail is saved.
event com.mgmtp.a12.dataservices.attachment.events.AttachmentThumbnailBeforeSaveEvent
AttachmentAfterCreateEvent
Triggered after the attachment is created.
event com.mgmtp.a12.dataservices.attachment.events.AttachmentAfterCreateEvent
AttachmentBeforeDeleteEvent
Published before an attachment is deleted. Mutating the AttachmentHeader is not supported at this
stage.
event com.mgmtp.a12.dataservices.attachment.events.AttachmentBeforeDeleteEvent
AttachmentAfterDeleteEvent
Triggered after the attachment is deleted.
event com.mgmtp.a12.dataservices.attachment.events.AttachmentAfterDeleteEvent
AttachmentThumbnailAfterSaveEvent
Triggered after the attachment thumbnail is saved.
event com.mgmtp.a12.dataservices.attachment.events.AttachmentThumbnailAfterSaveEvent
ContentTypeDetectedEvent
Event published after a content type has been successfully detected.
219

-- 219 of 334 --

event com.mgmtp.a12.dataservices.common.events.ContentTypeDetectedEvent
DocumentBeforeIndexEvent
The event is published before the document is indexed.
event com.mgmtp.a12.dataservices.document.events.DocumentBeforeIndexEvent
DocumentBeforeUpdateEvent
 Any data modifications are persisted with the updated document.To update
updatedDocument and persistedDocument, you must reassign the updated
document because DocumentV2 is immutable.
The event is published before computation and validation of the updated document.
event com.mgmtp.a12.dataservices.document.events.DocumentBeforeUpdateEvent
triggers com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#update,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#update,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#update,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#update
DocumentAfterDeleteEvent
The event is published after document deletion but before the transaction is committed.
event com.mgmtp.a12.dataservices.document.events.DocumentAfterDeleteEvent
triggers com.mgmtp.a12.dataservices.document.operation.internal.DeleteDocumentOperation#
rpc,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#delete
DocumentAfterControllerLoadEvent
The event is published after the document is loaded over the REST API.
event com.mgmtp.a12.dataservices.document.events.DocumentAfterControllerLoadEvent
DocumentBeforeRepositorySaveEvent
The event is published before the document is stored into the repository.
event com.mgmtp.a12.dataservices.document.events.DocumentBeforeRepositorySaveEvent
220

-- 220 of 334 --

triggers com.mgmtp.a12.dataservices.document.operation.internal.ModifyDocumentOperation#
rpc,
com.mgmtp.a12.dataservices.document.operation.internal.PartialModifyDocumentOpe
ration#rpc,
com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation#rpc
,
com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation#rp
c,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentReposit
ory#create,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentReposit
ory#update
DocumentAfterUpdateEvent
 No data modifications are persisted with the updated document.
The event is published after a document is updated, but before the transaction is committed.
event com.mgmtp.a12.dataservices.document.events.DocumentAfterUpdateEvent
triggers com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#update,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#update,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#update,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#update
DocumentBeforeDeleteEvent
 To update persistedDocument, you must reassign the updated document because
DocumentV2 is immutable.
The event is published before the document is deleted.
event com.mgmtp.a12.dataservices.document.events.DocumentBeforeDeleteEvent
triggers com.mgmtp.a12.dataservices.document.operation.internal.DeleteDocumentOperation
#rpc,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentServic
e#delete
listeners com.mgmtp.a12.dataservices.relationship.internal.DocumentDeletionListener#onDe
leteDocument order: 2147483647
DocumentAfterRepositoryLoadEvent
Triggered after the document is loaded from repository.
event com.mgmtp.a12.dataservices.document.events.DocumentAfterRepositoryLoadEvent
221

-- 221 of 334 --

triggers com.mgmtp.a12.dataservices.document.operation.internal.ModifyDocumentOperation#
rpc,
com.mgmtp.a12.dataservices.document.operation.internal.PartialModifyDocumentOpe
ration#rpc,
com.mgmtp.a12.dataservices.document.operation.internal.DeleteDocumentOperation#
rpc,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentReposit
ory#findByDocumentReference,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentReposit
ory#findDocumentsByDocRefs
DocumentBeforeCreateEvent
 Any data modifications are persisted with the document.To update
createdDocument, you must reassign the updated document because DocumentV2
is immutable.
The event is published before validation and computation of a newly created document.
event com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent
triggers com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation#rpc
,
com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation#rp
c,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#create,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#create,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#create,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#copy
DocumentAfterLoadEvent
 No data modifications are persisted.To update the document, you must reassign
the updated instance because DocumentV2 is immutable.
The event is published after the document is loaded.
event com.mgmtp.a12.dataservices.document.events.DocumentAfterLoadEvent
triggers com.mgmtp.a12.dataservices.document.operation.internal.GetDocumentOperation#rpc
,
com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation#rp
c,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#copy,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#load
222

-- 222 of 334 --

DocumentAfterCreateEvent
 No data modifications are persisted with the document.
The event is published after a document is created, but before the transaction is committed.
event com.mgmtp.a12.dataservices.document.events.DocumentAfterCreateEvent
triggers com.mgmtp.a12.dataservices.document.operation.internal.AddDocumentOperation#rpc
,
com.mgmtp.a12.dataservices.document.operation.internal.CopyDocumentOperation#rp
c,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#create,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#create,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#create,
com.mgmtp.a12.dataservices.document.persistence.internal.DefaultDocumentService
#copy
DataServicesCustomInitializationEvent
The event is published after all models are imported. It can be used to run reindexing just before
the JSON-RPC initialization is called.
event com.mgmtp.a12.dataservices.initialization.events.DataServicesCustomInitializati
onEvent
triggers com.mgmtp.a12.dataservices.initialization.internal.DataServicesInitializationSe
rvice#runInitialization
DataServicesInitializationFinishedEvent
The event is published when the initialization is completed, and when all models and documents
are loaded and indexed. It also indicates that the Data Services server is ready to be used.
event com.mgmtp.a12.dataservices.initialization.events.DataServicesInitializationFin
ishedEvent
listeners com.mgmtp.a12.dataservices.utils.internal.CachePreloader#onApplicationEvent,
com.mgmtp.a12.dataservices.server.util.internal.KernelCachesPreloader#listenOn
ServicesInitializationFinished,
com.mgmtp.a12.dataservices.server.actuator.internal.InitializationFinishedHeal
thIndicator#onApplicationEvent order: 2147483647
DataServicesDocumentModelCachesPreloadedEvent
Event indicating that document model caches have been preloaded. Consumers may use this to
warm dependent caches or start operations that rely on preloaded model metadata.
event com.mgmtp.a12.dataservices.initialization.events.DataServicesDocumentModelCache
sPreloadedEvent
223

-- 223 of 334 --

triggers com.mgmtp.a12.dataservices.server.util.internal.KernelCachesPreloader#listenOnS
ervicesInitializationFinished
GetDocumentAfterEvent
The event is published after the GET_DOCUMENT operation is executed.
event com.mgmtp.a12.dataservices.document.operation.events.GetDocumentAfterEvent
triggers com.mgmtp.a12.dataservices.document.operation.internal.GetDocumentOperation#rpc
GetDocumentBeforeEvent
The event is published before the GET_DOCUMENT operation is executed.
event com.mgmtp.a12.dataservices.document.operation.events.GetDocumentBeforeEvent
triggers com.mgmtp.a12.dataservices.document.operation.internal.GetDocumentOperation#rpc
ModelAfterRepositoryLoadEvent
The event is published after the model is loaded from the repository.
event com.mgmtp.a12.dataservices.model.events.ModelAfterRepositoryLoadEvent
ModelBeforeDeleteEvent
The event is published before the model is deleted.
event com.mgmtp.a12.dataservices.model.events.ModelBeforeDeleteEvent
ModelsAfterLoadEvent
The event is published after the models are loaded.
event com.mgmtp.a12.dataservices.model.events.ModelsAfterLoadEvent
ModelAfterUpdateEvent
The event is published after the model is updated.
event com.mgmtp.a12.dataservices.model.events.ModelAfterUpdateEvent
ModelBeforeRepositorySaveEvent
The event is published before the model is saved to the repository.
event com.mgmtp.a12.dataservices.model.events.ModelBeforeRepositorySaveEvent
ModelBeforeUpdateEvent
The event is published before the model is updated. It is composed of the persisted header, the
persisted model content, and the updated model content.
224

-- 224 of 334 --

event com.mgmtp.a12.dataservices.model.events.ModelBeforeUpdateEvent
ModelAfterCreateEvent
The event is published after the model is created.
event com.mgmtp.a12.dataservices.model.events.ModelAfterCreateEvent
ModelAfterDeleteEvent
The event is published after the model is deleted.
event com.mgmtp.a12.dataservices.model.events.ModelAfterDeleteEvent
ModelAfterLoadEvent
The event is published after the model is loaded.
event com.mgmtp.a12.dataservices.model.events.ModelAfterLoadEvent
triggers com.mgmtp.a12.dataservices.model.persistence.internal.AbstractModelLoader#loadM
odel
ModelsAfterImportEvent
The event is published after model import, so that custom code can be executed.
event com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent
ModelBeforeCreateEvent
The event is published before the model is created.
event com.mgmtp.a12.dataservices.model.events.ModelBeforeCreateEvent
RelationshipLinkAfterUpdateEvent
The event is published after the link changes are persisted.
RelationshipLinkAfterUpdateEvent represents the update of the link document. Ome
RelationshipLinkAfterUpdateEvent instance could be shared between multiple instances of event
consumers, therefore it must not be mutable.
event com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterUpdateEvent
RelationshipLinkAfterDeleteEvent
The event is published after the link is successfully deleted.
The RelationshipLinkAfterDeleteEvent represents the deletion of the link for a relationship model
between the entities described in the linkDescriptor of the RelationshipLinkSpec. One
RelationshipLinkAfterDeleteEvent instance could be shared between multiple instances of event
consumers, therefore it must not be mutable.
225

-- 225 of 334 --

event com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterDeleteEven
t
listeners com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkValidationLis
tener#linkDeletedEventListener order: 100
RelationshipLinkAfterCreateEvent
The event is published after the link is successfully created.
RelationshipLinkAfterCreateEvent represents the creation of the new link for Relationship model
between the entities described in the linkDescriptor of the RelationshipLinkSpec. One
RelationshipLinkAfterCreateEvent instance could be shared between multiple instances of event
consumers, therefore it must not be mutable.
event com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterCreateEven
t
listeners com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkValidationLis
tener#linkAddedEventListener order: 100
Listeners for Other than Data Services Events
ContextRefreshedEvent
event org.springframework.context.event.ContextRefreshedEvent
• com.mgmtp.a12.dataservices.initialization.DataServicesInitializationListener#onApplicationI
nitialization
order 100
description Handles application initialization on ContextRefreshedEvent. Ensures this
listener runs before UAA re-enables security bypass (ordered with Order
100).
• com.mgmtp.a12.dataservices.rpc.internal.JsonRpcOperationDispatcher#handleContextRefresh
order -100
description Order -100 was chosen to be executed before the initialization listener
(DataServicesCoreInitializationListener), which has 100 as its Order value.
To execute your listener between this and the initialization, choose a value
from -99 to 99.
• com.mgmtp.a12.contentstore.initialization.ContentStoreInitializationListener#onApplicationI
nitialization
226

-- 226 of 334 --

order 101
description UAA also listens to the ContextRefreshedEvent and disables security bypass
in the listener with order HIGHEST_PRECEDENCE. Therefore, we need to
make sure that our listeners will be executed before UAA disables security
bypass → @Order(101) And if someone wants their ContextRefreshedEvent
listener to be executed before this method, for example, they need to set an
Order lower than 100. Be aware that the listener
DataServicesInitializationListener#onApplicationInitialization, which
listens also to the ContextRefreshedEvent, is executed with @Order(100).
Application Custom Metadata
If you want to add your custom metadata to the document, you must extend the document
metadata model. It’s bundled in the application and is located at
classpath:com/mgmtp/a12/rmc/metadata/document-meta-data.json (provided by the rmc conversion
artifact). To provide your own, configure the property
mgmtp.a12.dataservices.models.metadata.document.path to point to your custom resource. All
existing fields from the existing one must be present also in the new one.
As soon as the metadata model is extended and expect new fields, you can add metadata to the
document in the DocumentBeforeUpdateEvent and DocumentBeforeCreateEvent. Document in this event
already contains the code metadata like Document Reference, Model Name, timestamps of creation
or update and related users. You can add extra fields described by your model to this document.
Custom Operations
There is also the possibility to extend operations or create custom ones. For more details please see
the chapter on JSON-RPC operations.
 Refer to the DS examples section for an example of a custom operation.
Custom Exceptions and its Mapping
If you want to implement your custom exception mapping, follow the common processing in Spring
MVC.
 Refer to the DS examples section for an example of custom exceptions.
In the Figure 28 you can see exception inheritance.
Custom Types and Conditions
There is also the possibility to add custom types and conditions for validating documents.
To use a custom condition, just implement the
com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory interface and provide this
227

-- 227 of 334 --

implementation as a Spring bean. The name of the condition must be returned by the
#getSupportedConditionNames() method. Then you can refer to this condition in validations by that
name.
To use a custom type, just implement the
com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory interface and provide this
implementation as a Spring bean. The name of the type must be returned by the
#getSupportedTypeNames() method. Then you can use a type of that name in your models.
For more details, please refer to custom type section and custom conditon section in A12 Kernel
documentation.
 Refer to the DS examples section for an example of custom types and conditions.
Attachment Clean-up
The CleanUpDirtyAttachmentsJob can be extended by adding a bean of type
com.mgmtp.a12.dataservices.attachment.IDirtyAttachmentCleanupCondition which could prevent the
deletion of the attachment depending on your condition.
If the bean is not present, the attachment is always deleted if it applies to the rules for deletion.
 Refer to the DS examples section for an example of a custom cleanup condition.
If you add this bean, the attachment is deleted only if it applies to the rules for the deletion and the
IDirtyAttachmentCleanupCondition.canBeDeleted method implementation returns true for the
attachment header.
If IDirtyAttachmentCleanupCondition.canBeDeleted returns false for an attachment header, it means
you decide to keep the corresponding attachment.
 In this case Data Services will not track this attachment anymore, which means
that it is your responsibility to take care of the remaining life-cycle of this
attachment.
Job Scheduling
Data Services uses Quartz scheduler to trigger scheduled jobs.
By default, it is configured to support clustering and to use JDBC store.
spring.quartz.job-store-type=jdbc
spring.quartz.properties.org.quartz.jobStore.isClustered=true
spring.quartz.properties.org.quartz.scheduler.instanceId=AUTO
Provided Jobs
The following jobs are preconfigured in the Data Services vanilla application:
228

-- 228 of 334 --

1. Scheduler ID: cleanUpRequestIdJob:
◦ Schedule: ${mgmtp.a12.dataservices.jobs.requests.cleanUpRequestId.schedule}
◦ Description: Job to clean up table REQUEST_ID by deleting entries which are older than the
time configured in mgmtp.a12.dataservices.jobs.requests.cleanupRequestId.expireHours. See
CleanUpRequestIdJob.
2. Scheduler ID: defragmentRanksJob:
◦ Schedule: ${mgmtp.a12.dataservices.link.rankRecalculateScheduler.defragmentSchedule}
◦ Description: Job to defragment ranks for relationship links.
3. Scheduler ID: cleanUpDirtyAttachmentsJob:
◦ Schedule: ${mgmtp.a12.dataservices.jobs.attachments.cleanUpDirtyAttachments.schedule}
◦ Description: Job to clean up files in the attachment storage location which are not
referenced by any GenericReference. See CleanUpDirtyAttachmentsJob.
4. Scheduler ID: cleanUpStaleAttachmentsJob:
◦ Schedule: ${mgmtp.a12.dataservices.jobs.attachments.cleanUpStaleAttachments.schedule}
◦ Description: Job to clean up files in the attachment storage location which were never
referenced by any GenericReference and which are older than the time configured in
mgmtp.a12.dataservices.jobs.attachments.temporary.expireHours. See
CleanUpStaleAttachmentsJob.
Custom Jobs
You can schedule your own job by creating a bean of type org.quartz.Trigger and providing a
org.quartz.Job implementation which will be executed, for example:
Example Job implementation and configuration
public class MyJob implements Job {
@Override
public void execute(JobExecutionContext context) throws JobExecutionException {
JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
LOGGER.info("My job triggered with params: {}, {}", mergedJobDataMap.
getString("myCustomStringParameter"), mergedJobDataMap.getInt("myCustomIntParameter")
);
doSomething(mergedJobDataMap.getString("myCustomStringParameter"),
mergedJobDataMap.getInt("myCustomIntParameter"));
}
public void doSomething(String myCustomStringParameter, int myCustomIntParameter)
{
...
}
}
@Configuraion
229

-- 229 of 334 --

public class JobConfig {
@Bean
public JobDetail myJobDetail() {
return JobBuilder.newJob()
.ofType(MyJob.class)
.withIdentity("myJob", "myJobGroup")
.withDescription("My example job.")
.storeDurably()
.build();
}
@Bean
public Trigger myTrigger(JobDetail myJobDetail){
return TriggerBuilder.newTrigger()
.forJob(myJobDetail)
.withIdentity("myTriggerId", "myJobGroup")
.withDescription("My trigger description.")
.withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * * ?"))
.usingJobData("myCustomStringParameter", "Param value")
.usingJobData("myCustomIntParameter", 6)
.startNow()
.build();
}
}
ExternalEnumerationLoader
To create your own external enumeration loader you have to implement the
ExternalEnumerationLoader interface. You have to annotate the class as a @Component and implement
the isModelSupported(String) and loadEnumeration(Document) methods. The API to load all external
enumerations for a model can be found here.
 Refer to the DS examples section for an example of a custom external enumeration
loader.
Document Serialization and Deserialization in DataServices
DataServices leverages Kernel library for document serialization and deserialization. The process is
highly configurable though extension points, allowing you to tailor the serialization and
deserialization behavior to meet specific requirements.
• DocumentSerializationConfig: This extension point allows you to customize how documents are
serialized.
• DocumentDeserializationConfig: This extension point enables customization of the document
deserialization process.
By creating new Spring beans for these two classes, you can ensure that DataServices handles
document serialization and deserialization in a manner that aligns with your application’s
230

-- 230 of 334 --

requirements, providing both flexibility and control over your data processing workflows.
 Refer to the DS examples section for an example of configuring Kernel document
serialization and deserialization.
Jackson ObjectMapper Configuration
DataServices configures the shared Spring Boot ObjectMapper with serializers and settings required
for its internal operation. To add custom serializers, deserializers, or subtype mappings, expose a
tools.jackson.databind.JacksonModule bean in your Spring configuration.
@Configuration
public class ExampleJacksonConfiguration {
@Bean("exampleJacksonModule")
public JacksonModule exampleJacksonModule() {
SimpleModule module = new SimpleModule("ExampleJacksonModule");
module.addSerializer(ExampleTaxId.class, new ExampleTaxIdSerializer());
module.addDeserializer(ExampleTaxId.class, new ExampleTaxIdDeserializer());
return module;
}
}

Do not use JsonMapperBuilderCustomizer to extend DataServices Jackson
configuration. It provides access to global mapper settings such as visibility rules
and mapper features that, if changed, can silently interfere with DataServices
internals.

Refer to the DS examples section for a working example of extending the Jackson
ObjectMapper configuration (ExampleJacksonConfiguration in examples-extending-
server). Activate it by adding the dataservices-example-extension_jackson Spring
profile to your application.
Overriding Relationship Link Validation
Data Services applies a default validation strategy when creating relationship links. The
RelationshipValidationSupport interface defines this validation contract.
The auto-configuration registers the default implementation as a Spring bean using
@ConditionalOnMissingBean. This means any project-level bean of type
RelationshipValidationSupport takes precedence over the Data Services default.
When to Override Validation
You should consider providing a custom RelationshipValidationSupport bean in the following
situations:
• Bulk import: During large data migrations, the default validation may reject links that are
231

-- 231 of 334 --

temporarily inconsistent. Replacing validation with a no-operation implementation allows
import to complete before applying consistency checks.
• Custom business rules: When the default structural validation must be extended with domain-
specific checks.
• Performance-critical paths: When validation overhead is unacceptable and correctness is
guaranteed by the calling code.
 Disabling or replacing validation can allow structurally invalid relationship links
to be created. Ensure that any custom implementation maintains the invariants
required by your application.
No-Operation Implementation
The following example registers a no-operation bean that skips all validation:
@Configuration
public class BulkImportConfiguration {
@Bean
public RelationshipValidationSupport noopRelationshipValidation() {
return (linkDescriptor, linkDocRef) -> {};
}
}
Because the auto-configuration uses @ConditionalOnMissingBean, this bean is used whenever it is
present in the application context, replacing the Data Services default.
Custom Validation Implementation
The following example adds a domain-specific check on top of the existing validation. To extend
rather than replace the default validation, inject the default bean and delegate to it:
@Bean
public RelationshipValidationSupport customRelationshipValidation(
RelationshipValidationSupport defaultValidation) {
return (linkDescriptor, linkDocRef) -> {
defaultValidation.validateLink(linkDescriptor, linkDocRef);
// additional domain-specific checks
};
}
 When providing a custom RelationshipValidationSupport, be aware that the
default bean will no longer be created. If you need the default validation behavior,
inject it explicitly as shown above or replicate its logic.
232

-- 232 of 334 --

Data Services Security
Data Services Authentication
Authentication is completely handled by UAA, refer to their documentation for
more information.
Data Services Authorization

Data Services provide authorizationDefinition file via
mgmtp.a12.uaa.authorization.authorizationDefinition property. Do not replace it,
otherwise you disable DS authorization completely, meaning that there is no
guarantee that your code will work as expected. Instead, use
mgmtp.a12.uaa.authorization.childAuthorizationDefinitions to introduce your own
authorization rules on top of the already existing ones.
 Refer to the DS examples section for an example of custom authorization.
Authorization is handled by UAA. For general UAA concepts and configuration, refer to their
documentation. In this chapter we will focus on Data Services related UAA usage.
Access to models is governed by roles assigned to each model. To read a model like Product, a user
must possess at least one role with the MODEL_READ permission, and that role must be listed among
the model’s allowed roles. This mechanism ensures model access is restricted by a single
permission.
Document access is derived from model permissions. For example, to create a document for the
Product model, a user must:
1. Have at least one role with the MODEL_READ permission, and that role must be listed in the Product
model’s roles.
2. Possess the DOCUMENT_CREATE permission in any of their roles.
Authorization rules reside in uaa/authorizationDefinition.json, using policies defined with SPEL
expressions. Permissions reference these policies to validate access to specific scopes.
You may define additional permissions and policies as needed, organizing them according to your
requirements. Permissions can be defined for the following scopes:
Model Create Check that the user has access right to create models.
com.mgmtp.a12.model.header.Header is referenced as
#resource.
Model Update Check that the user has access right to update provided model.
com.mgmtp.a12.model.header.Header is referenced as
#resource.
233

-- 233 of 334 --

Model Read Check that the user has access right to read provided model.
com.mgmtp.a12.model.header.Header is referenced as
#resource.
Model Delete Check that the user has access right to delete provided model.
com.mgmtp.a12.model.header.Header is referenced as
#resource.
Document Create Check that the user has access right to create documents.
com.mgmtp.a12.kernel.md.document.apiV2.immutable.Docume
ntV2 is referenced as #resource.
Document Update Check that the user has access right to update documents.
com.mgmtp.a12.dataservices.authorization.DocumentUpdateRes
ource is referenced as #resource.
Document Partial Update Check that the user has access right to partially update
documents.
com.mgmtp.a12.dataservices.authorization.DocumentUpdateRes
ource is referenced as #resource.
Document Delete Check that the user has access right to delete documents.
com.mgmtp.a12.dataservices.document.DataServicesDocument
is referenced as #resource.
Document Multi Delete Check that user has access right to multi delete documents.
Manage Caches Check that the user has a role which has access right to manage
caches.
Endpoint You can control endpoint security inside this scope. Each
endpoint is defined by its class name/method name. Returns
always true.
RelativePath Controls access to actuator endpoints. Requires the
ACCESS_ACTUATOR access right.
Attachment Upload You can define permission rules for uploading a file.
com.mgmtp.a12.dataservices.attachment.AttachmentHeader is
referenced as #resource.
Export List CDD You can define permission rules for export document.
Query You can define permission rules for executing queries.
Authorization Scopes Used in the Code
234

-- 234 of 334 --

Method name and arguments Scope name Description
ModelsV2ControllerImpl.loadMod
el
modelId
java.lang.String
code
Model Read
Check that the user has access
right to read provided model.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
ModelsV2ControllerImpl.updateM
odel
modelContent
java.lang.String
code
Model Update
Check that the user has access
right to update provided model.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
ModelsV2ControllerImpl.createM
odel
modelContent
java.io.@lombok.NonNull
Reader
code
Model Create
Check that the user has access
right to create models.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
ModelsV2ControllerImpl.deleteM
odel
modelId
java.lang.String
code
Model Delete
Check that the user has access
right to delete provided model.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
ModelsV2ControllerImpl.generat
eValidationCode
modelName
java.lang.String
code
Model Read
Check that the user has access
right to read provided model.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
ModelsV2ControllerImpl.importM
odelBulk
modelBulk
java.io.@lombok.NonNull
InputStream
code
Model Update
Check that the user has access
right to update provided model.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
code
Model Create
Check that the user has access
right to create models.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
235

-- 235 of 334 --

Method name and arguments Scope name Description
AttachmentV2ControllerImpl.upl
oad
content
org.springframework.core
.io.InputStreamResource
filename
java.lang.String
documentModelName
java.lang.String
pathToField
java.lang.String
annotations
java.lang.String[]
code
Model Read
Check that the user has access
right to read provided model.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
code
Attachment Upload
You can define permission rules
for uploading a file.
com.mgmtp.a12.dataservices.att
achment.AttachmentHeader is
referenced as #resource.
RelationshipControllerImpl.get
ModelGraph
code
Model Read
Check that the user has access
right to read provided model.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
ExternalEnumerationControllerI
mpl.loadExternalEnumerationFor
Model
modelName
java.lang.String
code
Model Read
Check that the user has access
right to read provided model.
com.mgmtp.a12.model.header.H
eader is referenced as
#resource.
code
Query
You can define permission rules
for executing queries.
Model Authorization
The Model Read scope is enforced for all Data Services APIs. Whenever a model is loaded for
operations such as deserialization or validation, access is securely checked against this scope.
The QUERY operation supports heterogeneous models, but user permissions may restrict visibility of
certain subtypes.
Access to subtypes is also governed by the Model Read scope. The GET /modelgraph endpoint returns
all document and relationship models visible to the current user, ensuring that subsequent
operations respect these visibility constraints.
Due to these requirements, not all security logic can be delegated to other scopes.
236

-- 236 of 334 --

Role Based vs. Role-less Authorization
Data Services introduces the configuration key
mgmtp.a12.dataservices.authorization.roleBased.enabled, which defaults to true. This setting
controls authorization for the operations: MODEL_CREATE, MODEL_UPDATE, MODEL_READ, and MODEL_DELETE.
• If set to true, model header roles are required and compared against the user’s roles for access.
• If set to false, authorization for these operations is disabled.
Clients can extend model authorization by specifying custom rules using the property:
mgmtp.a12.uaa.authorization.child-authorization-
definitions=classpath:additionalAuthorizationDefinition.json.
Support Only HTTP/1 Protocol
By default, DS is supporting both HTTP/2 and HTTP/1 protocols, therefore if project team want to
enable supporting for HTTP1 only please use DS new profile dataservices-http1_only and
contentstore-http1_only for supporting application server HTTP/1 protocol only. In the examples DS
introduce dataservices-example-http1_env and contentstore-example-http1_env group profile to start
up server with HTTP/1 protocol supporting only.
Log Injection
We discovered that users could control single lines of log by crafting special requests with Unicode
characters for line breaks. For example, attackers could inject a new line of log by sending a request
like this:
{
"jsonrpc": "2.0",
"method": "ADD_DOCUMENT",
"id": "AddComputedDocument",
"params": {
"document": {
"BusinessPartnerRoot": {
"PersonalData[] - please check\n\r2026-06-26 06:33:33,666
[#{7*7}][%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36}][This is a injected log entry
#{7*7}] - user controlled log entry #{7*7} T(java.lang.System).getenv()[0]\n\r2026-06-
26 06:33:33,666 [foobar][WARN ][error when parsing RPC request] - error in ": {
"FirstName": "firstname",
"LastName": "lastname",
"Email": "mail@mail.com"
}
}
},
"documentModelName": "BusinessPartner",
"locale": "en"
}
}
237

-- 237 of 334 --

The result would be in the log:
2026-06-26 06:33:33,666 [foobar][WARN ][error when parsing RPC request] - error in
[1]', the corresponding entity was not found in the corresponding document model.
[ERROR,L0,s0,e0],
For the entity instance '/BusinessPartnerRoot[1]/PersonalData[] - please check
2026-06-26 06:33:33,666 [#{7*7}][%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36}][This
is a injected log entry #{7*7}] - user controlled log entry #{7*7}
T(java.lang.System).getenv()[0]
This exposes a potential risk when subsequent monitoring systems use automatic rules based on
log entries, such as blocking an IP address or a user. For manual log reviews, this would likely only
cause confusion.
To prevent this attach, configure your logging implementation to escape special characters before
they are included in log entries. This includes characters such as newlines, carriage returns, and
delimiters.
Data Services Artifacts
Data Services deliver several artifacts to support different use cases.
Runnable Artifacts
The DS vanilla Spring Boot applications can be found in the artifactory under the following IDs:
Jar Application
 Artifact does not contain any additional dependencies or features.
Group ID com.mgmtp.a12.dataservices
Artifact ID dataservices-server-app
The artifact is also available with sources and javadoc classifiers.
The other three artifacts that Data Services deliver are maintenance tools.
Relationship Migration Tool in Typescript
The relationship model migration tool is a TS tool that should be used to migrate relationship
models to the newest version.
Group ID com.mgmtp.a12.dataservices
Artifact ID dataservices-relationship-model-migration
 Migration is supported only from the last major version to the current major
238

-- 238 of 334 --

version. If you need to migrate older versions please migrate step by step.
Data Services Client CLI
The Data Services command line tool is a jar file which uses a Java client to communicate with a
running Data Services instance. Please use the --help option to learn more about the jar
application.
Group ID com.mgmtp.a12.dataservices
Artifact ID dataservices-client-cli
The artifact is also available with sources and javadoc classifiers.
DataServices Initialization App
Data Services provide an initialization jar application which can execute the initialization sequence
of the Data Services server without starting any HTTP servlets.
This application does not take any parameters, only the application.properties which are identical
to the application.properties of the server.
The application was created to support a clustered deployment where the initialization sequence
(rebuilding index, migration of database, migration of data, upload of models) should happen only
once and not during the actual server star-up.
This tool should make the maintenance of the Data Services deployments easier by splitting
migration/configuration from the server initialization.
Group ID com.mgmtp.a12.dataservices
Artifact ID dataservices-server-init-app
The artifact is also available with sources and javadoc classifiers.
Data Services BOM
There is a way to build your own artifact. For this, we offer a BOM artifact.
When you build your custom artifact you can easily change the versions of main A12 libraries:
• a12.validation.version
• a12.dataservices.version
If you need a custom artifact, you need to extend from main Data Services BOM artifact:
Group ID com.mgmtp.a12.dataservices
Artifact ID dataservices-parent
239

-- 239 of 334 --

then, you have to define the artifact packaging.
This is the recommended way how to extend DS because it allows the most flexibility while not
enforcing any target extension (war, jar, fatjar,…).
ModelGraphGenerator
For customer projects that cannot use the Data Services server but need to generate the model
graph, we provide a standalone jar artifact. This artifact can be used to generate the model graph
from the command line. It does not have any dependencies to Spring Boot or other server
components.
The ModelGraphGenerator can be found in the artifactory under the following ID:
Group ID com.mgmtp.a12.dataservices
Artifact ID dataservices-modelgraph-fs-impl
For more information please see ModelGraphGenerator javadoc.
Data Services Helm Charts
Data Services provide Helm charts to deploy Data Services on Kubernetes clusters.
The Helm charts can be found in the artifactory under the following ID:
Group ID com.mgmtp.a12.dataservices
Chart ID a12-data-services
Please note that helm version is different from Data Services version, current chart version is
39.0.2.
HTTP API Documentation
The HTTP API of the Data Services server is reachable via the following endpoints:
 Parameters marked with  are mandatory and should not be omitted nor null.
Other parameters (marked with ) can be bypassed or set to null.
Attachments
Documentati
on
URL Description
Attachments
REST API V2
#{@dataServicesCoreProperties.serv
er.contextPath}/v2/attachment
This API provides the ability to create
attachments V2 (potentially with
thumbnails).
240

-- 240 of 334 --

Documents
Documentation URL Description
External
Enumeration REST
API
#{@dataServicesCoreProperties.server.c
ontextPath}/enum/ext
API to retrieve external
enumerations for a model.
Models
Documentatio
n
URL Description
Models REST
API V2
#{@dataServicesCoreProperties.server.context
Path}/v2/models
API to create, read, update and
delete models.
Monitoring
Documentation URL Description
Monitor Configuration
Properties REST API
#{@dataServicesCoreProperties.se
rver.contextPath}
API to retrieve monitored
configuration properties.
Query API
Documentation URL Description
Query Aggregations
REST API
#{@dataServicesCoreProperties.server.contextPat
h}/aggregation
Endpoint to load
aggregations.
Relationships
Documentation URL Description
Relationship REST
API
#{@dataServicesCoreProperties.server.contex
tPath}/
Endpoint to receive a model
graph.
Workspace (SME API): For testing and demo purposes
only. Not intended for production use.
Documentation URL Description
Internal SME
Workspace Import
REST API
#{@dataServicesCoreProperties.
server.contextPath}/sme/worksp
ace
This API provides the ability to import
workspace data.
241

-- 241 of 334 --

Documentation URL Description
Internal SME
Workspace Export
REST API
#{@dataServicesCoreProperties.
server.contextPath}/sme/worksp
ace
This API provides the ability to export
workspace data.
Internal Database
Clear REST API
#{@dataServicesCoreProperties.
server.contextPath}/sme/worksp
ace
This API provides the ability to delete
models, documents, relationship links,
attachments.
All URLs in the above documentation are relative. You need to prepend the context to the URL
where your application is deployed.
Common Request Headers
Commonly used headers in most requests:
Key Value
Authorization UAABearer {token}
Common Error Responses
Commonly appearing errors in most requests:
Response Status Description
401 Unauthorized Authorization exception, e.g. user not signed in.
403 Forbidden User does not have proper permissions for the operation.
404 Not Found Resource not found in database or Endpoint does not exist.
405 Method Not Allowed Using wrong method (e.g., POST instead of GET when calling
/modelgraph endpoint).
406 Not Acceptable Accept header might not be set properly (e.g., application/xml).
415 Unsupported Media
Type
Content-Type header might not be set properly (e.g.,
application/xml).
500 Internal Server Error Unknown server problem, probably bug.
Attachments REST API V2
This API provides the ability to create attachments V2 (potentially with thumbnails).
List of Contents
• Upload Attachment as Stream V2
Upload Attachment as Stream V2
Name Upload Attachment as Stream V2
242

-- 242 of 334 --

Description Endpoint allowing the upload of attachments. Big and small thumbnails are
generated if the attachment is of type: JPEG, PNG, BMP, WBMP, GIF or SVG. An
attachment can also be of text type (e.g. JSON, XML, TXT). NOTE: SVG is only
supported for Thumbnailator. Enabling
mgmtp.a12.dataservices.attachments.thumbnail.optimization.url.enabled would
return an empty url for SVG.
Method POST
URL #{@dataServicesCoreProperties.server.contextPath}/v2/attachment
Headers Accept
application/json
Parameters content 
Attachment content.
filename 
Desired filename of attachment.
documentModelName 
Document model name of uploaded document.
pathToField 
Not yet implemented, but it is mandatory parameter. Empty string could be
used.
annotations 
List of attachment annotations.
Authorization
Scopes
• Model Read
• Attachment Upload
Success
response
200 OK
Attachment has been uploaded.
External Enumeration REST API
API to retrieve external enumerations for a model. Java definition of /enum/ext which provides
mappings between business specified document names and document ids. There can be only one
mapping defined per document model.
List of Contents
• Get External Enumeration for Document Model
Get External Enumeration for Document Model
Name Get External Enumeration for Document Model
Description Loads external enumeration per document model. The external enumeration has
to be implemented as for document model via extension point.
243

-- 243 of 334 --

Method GET
URL #{@dataServicesCoreProperties.server.contextPath}/enum/ext/{document-model-
name}
Headers Accept
application/json
Parameters modelName 
Queried Document Model to retrieve External Enumeration.
Authorization
Scopes
• Model Read
• Query
Success
response
200 OK
Loaded External Enumeration.
Error
response
412 Precondition Failed
Validation code could not be generated.
Notes • HTTP response will have cache-related headers modified. For the cache
information see NoCache its usage.
Models REST API V2
API to create, read, update and delete models. All below-mentioned endpoints work only with JSON,
and they share context path /v2/models. All models that adhere to the metadata definition can be
persisted and served via following REST endpoints. No other models will be accepted.
All below-mentioned CRUD operations are extensible via IModelRepository concept.
List of Contents
• Load Model
• Update Model
• Create Model
• Delete Model
• Generate Validation Code
• Import Models
Load Model
Name Load Model
Method GET
URL #{@dataServicesCoreProperties.server.contextPath}/v2/models/{model-id}
Parameters modelId 
Required Model to load.
244

-- 244 of 334 --

Name Load Model
Authorization
Scopes
• Model Read
Success
response
200 OK
The response contains a body with the persisted model.
Update Model
Name Update Model
Method PUT
URL #{@dataServicesCoreProperties.server.contextPath}/v2/models,
#{@dataServicesCoreProperties.server.contextPath}/v2/models/
Parameters modelContent 
Content of the model to update.
Authorization
Scopes
• Model Update
Success
response
200 OK
The response contains the persisted model. Please note that this model might
be different to the model that was sent because of custom extensions which
are able to change the model before saving.
Error
response
400 Bad Request
Model validation failed. Model is not acceptable → The payload of the request
is not a valid A12 model.
Notes • The roles annotation of the model is mandatory. Without a role definition the
server will not be able to persist the model.
Create Model
Name Create Model
Method POST
URL #{@dataServicesCoreProperties.server.contextPath}/v2/models,
#{@dataServicesCoreProperties.server.contextPath}/v2/models/
Parameters modelContent 
Content of the model to create.
Authorization
Scopes
• Model Create
Success
response
200 OK
The response contains the persisted model. Please note that this model might
be different to the model that was send because of custom extensions which
are able to change the model before saving.
245

-- 245 of 334 --

Name Create Model
Error
response
400 Bad Request
Model validation failed. Model is not acceptable → The payload of the request
is not a valid A12 model.
409 Conflict
Model creating failure → Model might be already created.
Notes • The roles annotation of the model is mandatory. Without a role definition the
server will not be able to persist the model.
Delete Model
Name Delete Model
Method DELETE
URL #{@dataServicesCoreProperties.server.contextPath}/v2/models/{model-id}
Parameters modelId 
Required Model to delete.
Authorization
Scopes
• Model Delete
Success
response
200 OK
If model was deleted or if model with model-id does not exist anymore.
Example Product
Generate Validation Code
Name Generate Validation Code
Method GET
URL #{@dataServicesCoreProperties.server.contextPath}/v2/models/{model-
id}/validationCode
Parameters modelName 
The model to be validated.
Authorization
Scopes
• Model Read
Import Models
Name Import Models
Description Endpoint allowing a bulk import of models to database.
This method is intentionally not @Transactional. Each model is imported in its
own transaction at the service layer, allowing partial success when some models
fail and are retried due to dependency ordering.
246

-- 246 of 334 --

Method PUT
URL #{@dataServicesCoreProperties.server.contextPath}/v2/models,
#{@dataServicesCoreProperties.server.contextPath}/v2/models/
Headers Content-type
application/octet-stream
Parameters modelBulk 
Stream of zip of models.
Authorization
Scopes
• Model Update
• Model Create
Success
response
200 OK
The response contains list of the names of all created models.
Error
response
400 Bad Request::Model validation failed. Model is not acceptable → The payload
of the request is not a valid A12 model.
Monitor Configuration Properties REST API
API to retrieve monitored configuration properties.
List of Contents
• Get Monitored Configuration Properties
Get Monitored Configuration Properties
Name Get Monitored Configuration Properties
Description Retrieves monitored configuration properties and their current values. This
endpoint exposes specific configuration properties that clients may need to know,
returning the currently effective value for each property (either from
configuration sources or the default value if not overridden). Please note this
endpoint is public and does not require authentication.
Method GET
URL #{@dataServicesCoreProperties.server.contextPath}/monitored-properties
Parameters
Success
response
200 OK
The response contains a body with the map of monitoring properties.
Query Aggregations REST API
Endpoint to load aggregations.
List of Contents
• Load Aggregations
247

-- 247 of 334 --

Load Aggregations
Name Load Aggregations
Description Returns aggregated values as a 2-dim object array with the values of the group by
columns first, and the aggregated values behind them. The number of returned
rows is controlled by configuration.
Example:
[
["Household", 1, 50000.0],
["Liability", 1, 1000000.0],
["Travel", 3, 1350000.0]
]
Method POST
URL #{@dataServicesCoreProperties.server.contextPath}/aggregation
Parameters queryRoot 
A query that contains aggregations. No links and no paging are allowed.
Relationship REST API
Endpoint to receive a model graph.
List of Contents
• Get Model Graph
Get Model Graph
Name Get Model Graph
Description Get a model graph containing document models, CDMs, and relationship models.
Method GET
URL #{@dataServicesCoreProperties.server.contextPath}/modelgraph
Parameters
Authorization
Scopes
• Model Read
Success
response
200 OK
The response contains the ModelGraphRoot object.
Internal SME Workspace Import REST API
This API provides the ability to import workspace data.
248

-- 248 of 334 --

List of Contents
• Import Workspace as Stream
Import Workspace as Stream
Name Import Workspace as Stream
Description Endpoint allowing import SME Workspace, encapsulated within tar compressed
with gzip archive Crucially, the directories and their contents within the
archive must strictly adhere to a predefined processing order to ensure
correct data dependencies and integrity during import. Folder example:
• /data/meta/workspacedata_items.json
• /data/models/Contract.json
• /data/models/BusinessPartner.json
• /data/models/ContractBusinessPartner.json
• /data/attachments/e4csdw43-6a00-418a-a7d6-d9f5b7f82df2.jpg
• /data/attachments/e4cbe2wa-2efd-418a-12ed-d9f5b7fd3wf0.cert
• /data/documents/Contract/8ed0be43-bd0c-438c-a0de-c8e8712c83b1.json
• /data/documents/BusinessPartner/8ed0b2eds-2csa-2wsa-asf2-
c8e8712c83b1.json
• /data/links/ContractBusinessPartner/8fd0b2eds-2csa-2wsa-asf2-
c8e8712c83b2.json
• /data/links/ContractBusinessPartner/8ad0b2eds-2csa-2wsa-asf2-
c8e8712c83b3.json
• /data/user/users.yaml
Importing SME Workspace is specifically designed for empty databases.
Attempting to import into a non-empty database may result in unexpected errors
or data corruption.
Method POST
URL #{@dataServicesCoreProperties.server.contextPath}/sme/workspace/import
Parameters content 
the compressed archive of the SME Workspace
Success
response
204 No Content
Import workspace data successfully.
Internal SME Workspace Export REST API
This API provides the ability to export workspace data.
List of Contents
• Export Workspace Content
249

-- 249 of 334 --

Export Workspace Content
Name Export Workspace Content
Description Endpoint allows downloading the compressed archive of the SME Workspace,
exported folder example:
• /data/meta/workspacedata_items.json
• /data/models/Contract.json
• /data/models/BusinessPartner.json
• /data/models/ContractBusinessPartner.json
• /data/attachments/e4csdw43-6a00-418a-a7d6-d9f5b7f82df2.jpg
• /data/attachments/e4cbe2wa-2efd-418a-12ed-d9f5b7fd3wf0.cert
• /data/documents/Contract/8ed0be43-bd0c-438c-a0de-c8e8712c83b1.json
• /data/documents/BusinessPartner/8ed0b2eds-2csa-2wsa-asf2-
c8e8712c83b1.json
• /data/links/ContractBusinessPartner/8fd0b2eds-2csa-2wsa-asf2-
c8e8712c83b2.json
• /data/links/ContractBusinessPartner/8ad0b2eds-2csa-2wsa-asf2-
c8e8712c83b3.json
• /data/user/users.yaml
Method GET
URL #{@dataServicesCoreProperties.server.contextPath}/sme/workspace/export
Parameters includeModels 
true if the exported data should include model definitions; false otherwise.
Success
response
200 OK
File is ready to be downloaded.
Internal Database Clear REST API
This API provides the ability to delete models, documents, relationship links, attachments.
List of Contents
• Clear Database
Clear Database
Name Clear Database
Description Endpoint allowing clear all data from the database.
Method DELETE
URL #{@dataServicesCoreProperties.server.contextPath}/sme/workspace/clearDatabas
e
250

-- 250 of 334 --

Headers Accept
application/json
Parameters
Success
response
204 No Content
Clear database successfully.
Data Services Clients
Java Client
To connect any Java application to the server, use our Java API — no manual HTTP handling
required. The client manages both communication and authentication for you. Configuration is
flexible to suit different environments. While the client is Spring-based, it also supports usage in
non-Spring contexts.
Main Initialization
The main entry point for connecting your Java application is the ClientFactory.
For Spring applications, configure the property mgmtp.a12.dataservices.client.configuration.base-
url with your server URL, then inject the factory into your class. You can also use the
autoconfigured project as a dependency to inject client interfaces directly.
For non-Spring applications, instantiate ClientFactory using its default constructor.
Prefer a single factory instance per application to minimize authentication overhead. Multiple
factories are only needed for scenarios like using separate technical users.
In Spring, one factory is created automatically via classpath scanning; additional instances must be
created by your infrastructure. The factory supports both single-threaded and multithreaded
environments.
To add custom request interceptors (e.g., for modifying headers), create a ClientFactory instance as
a Spring bean or via your DI framework. Use the builder to supply a list of custom
ClientHttpRequestInterceptor.
Manual Configuration
Manual configuration is suitable when you need full control over authentication and server
settings, or want to customize request handling.
To do this, instantiate ClientConfiguration and UaaRestClientConfiguration with your desired
properties. Use ClientFactory#builder to obtain a ClientFactoryBuilder, customize as needed, and
call build() to create the factory.
Once initialized, you can retrieve any client interface from the factory, such as
ClientFactory#getRestModelsClient, and begin interacting with the server.
251

-- 251 of 334 --

Autoconfiguration
For autoconfiguration, you can depend on the autoconfigure library:
com.mgmtp.a12.dataservices:dataservices-client-spring-boot-autoconfigure
By doing this, you can use the beans of the client interfaces by injecting them to your code. For
example:
Example usage of ModelsClient
class YourClass {
@Inject
private ModelsClient modelsClient;
public void run(String model) {
[...]
modelsClient.createModel(new StringReader(model));
[...]
}
}
 The server must be configured properly, otherwise it will not work. (see
configuration properties with prefix mgmtp.a12.dataservices.client)
For all properties see the Configuration details.
Exception Handling
The client API provides three main runtime exceptions for type-safe error handling. When a
specific exception cannot be thrown, a generic exception is used. All exceptions are unchecked and
do not need to be declared in API signatures.
A12ClientException Generic exception used for any kind of error where we can’t
use a specific exception. Parent of all other client exceptions.
MissingAccessRightException This exception will be thrown when the user has no access
right to a document or model.
MissingDataException This exception will be thrown when the requested document
or model is not found.
You can use the getErrorDetail() method on each exception to get details:
Example usage of getErrorDetail()
public ErrorDetail getErrorDetail() {
return errorDetail;
}
ErrorDetail is just a marker interface. See the example from the Java client implementation.
252

-- 252 of 334 --

Example implementation of ErrorDetail
public class RestErrorDetail {
private int responseCode;
private String response;
}
Usage Scenarios
The client can be used in different scenarios. This section describes the most common ones.
Fat Client AKA Tools
In this case you just need a single user which is configured in the application settings. The usage is
single thread. Authentication is handled by the client itself and credentials are stored at runtime by
the client.
Client With Multiple (Technical) Users
This case is similar to the previous one with the only difference that you need to have multiple
factories. Credentials are bound to each factory.
Client Interfaces
After all configuration is done, you can use the client interfaces to interact with the rest endpoints
in your Java application. These are the classes:
• com.mgmtp.a12.dataservices.client.attachment.RestAttachmentV2Client
• com.mgmtp.a12.dataservices.client.rpc.RestRpcOperationsClient
• com.mgmtp.a12.dataservices.client.enumeration.rest.RestEnumerationClient
• com.mgmtp.a12.dataservices.client.model.rest.RestModelsClient
• com.mgmtp.a12.dataservices.client.relationship.rest.RestRelationshipClient
Detailed explanations how to call these interface methods can be found in the respective Javadoc.
Data Services Command Line Interface (CLI)
The CLI is a tool used to interact with the running Data Services server from scripts or command
line, so make sure your Data Services server is up and running.
Basic usage is:
• java -jar dataservices-client-cli-39.0.2-fatjar.jar COMMAND OPTIONS ARGUMENTS or just
• ./dataservices-client-cli-39.0.2-fatjar.jar COMMAND OPTIONS ARGUMENTS If you are using POSIX
shell and have set the executable flag to fatjar and have at least Java 11 version configured.
 If you call an executable jar, the current directory is set to the location of the jar,
253

-- 253 of 334 --

so relative paths are resolved relatively to the jar location.
The application returns exit code 0 if all has been executed without problems. In case of an error it
returns exit code 1. In case of help requested it returns exit code 2.
When you run it just with -h argument, help is displayed (also listing all available commands and
their syntax).
For the tool to work properly, you have to configure it.
A path can be provided by using OS specific absolute or relative path, or by a valid URI. Files should
not have spaces in the name. Currently, we support only directories with spaces but not files.
Examples for acceptable paths:
• c:\data\mymodel.json
• /home/user/mydata/mymodel.json
• file:/c:/data/mymodel.json
• classpath:/mymodel.json
• data\mymodel.json
• mydata/mymodel.json
Configuration
The tool is configured using the Java properties read from command line or from the file
application.properties, which should be placed in the same directory as the fatjar, or in a
subdirectory called config, as it is common in Spring.
Data Services - Content Store
Introduction
Attachments and thumbnails are managed by the Data Services module Content Store (CS). Data
Services interacts with Content Store to upload these files, while only the content itself is stored in
Content Store; attachment metadata resides in the attachment_header table of Data Services. All
uploaded content sharing the same Persistent Type is handled uniformly (see Persistent Type
section).
Content Store operates in two modes:
• Embedded Mode::
◦ The default mode, running within the Data Services component.
◦ Only the Download Content API is exposed; other APIs are restricted.
◦ No separate Content Store instance is required for client projects.
• Standalone Mode:
254

-- 254 of 334 --

◦ Runs as an independent service, exposing APIs for uploading, deleting, and generating
downloadable URLs via ContentStoreTicketController and ContentStorePrivateController.
◦ Data Services communicates with Content Store using the Content-store-client module.
◦ Attachment and thumbnail processing is decoupled from Data Services performance.
◦ Standalone Content Store can also operate independently of Data Services.
Content Store supports both file system and database storage, configurable with file system as the
default.
Content Store uses its own Datasource. This requires configuring Datasource properties for Content
Store in any mode, enabling seamless switching between standalone and embedded operation.
The following diagram illustrates the relationship between Data Services and Content Store classes,
useful for extension.
j ava
lang 	io
dataservices-core	
dataservices	
a t t a c h m e n t
h e a d e r
persitence
v 2	
persistence
content-store	
contentstore	
service
c o n t e n t
dataservices-models	
dataservices	
a t t a c h m e n t
r e fe r e n c e
CharSequence 	Serializable
AttachmentService
createAttachment(	
is: java.io.InputStream	
filename: java.lang.String	
documentModelName: java.lang.String	
pathToField: java.lang.String	
annotations: java.util.List	
): com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
findAttachmentUrl(	
attachmentId: java.lang.String	
docRef: com.mgmtp.a12.dataservices.document.DocumentReference	
): java.util.Optional	
findThumbnailUrl(	
attachmentId: java.lang.String	
type: com.mgmtp.a12.dataser vices.attachment.ThumbnailType	
): java.util.Optional	
findThumbnailUrl(	
header : com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
type: com.mgmtp.a12.dataser vices.attachment.ThumbnailType	
): java.util.Optional
AttachmentHeaderService
create(
header : com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
): com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
delete(
attachmentId: java.lang.String	
): void
load(
attachmentId: java.lang.String	
): java.util.Optional	
loadUnassignedAttachmentsOlderThan(	
tmpAttachmentExpireHours: int	
): java.util.List	
assignAttachment(	
header : com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
reference: com.mgmtp.a12.dataser vices.attachment.AttachmentReference	
): void
unAssignAttachment(	
header : com.mgmtp.a12.dataser vices.attachment.AttachmentHeader	
reference: com.mgmtp.a12.dataser vices.attachment.AttachmentReference	
): void
unAssignAttachments(	
documentReferences: java.util.Collection	
): void
IAttachmentRepository
create(
id: java.lang.String	
is: java.io.InputStream	
filename: java.lang.String	
type: com.mgmtp.a12.dataser vices.attachment.TypeOfTheContent	
mimeType: java.lang.String	
): com.mgmtp.a12.dataservices.attachment.persitence.AttachmentPersistenceResult	
findUrl(
id: java.lang.String	
filename: java.lang.String	
type: com.mgmtp.a12.dataser vices.attachment.TypeOfTheContent	
): java.util.Optional	
delete(
id: java.lang.String	
): void
AttachmentService 	IAttachmentRepository
ContentPersistenceResult
contentId: java.lang.String	
size: long	
url: java.util.Optional<java.lang.String>	
contentType: java.lang.String
ContentPersistenceResultBuilder
ContentStoreService
requestContentUrl(	
contentId: java.lang.String	
duration: long	
): java.lang.String	
findPublicContentUrl(	
contentId: java.lang.String	
): java.util.Optional	
getContent(
id: java.lang.String	
): com.mgmtp.a12.contentstore.content.ContentStream	
exists(
id: java.lang.String	
persistentType: java.lang.String	
): boolean	
saveContent(	
contentId: java.lang.String	
persistentType: java.lang.String	
inputStream: java.io.InputStream	
filename: java.lang.String	
): com.mgmtp.a12.contentstore.ContentPersistenceResult	
saveContent(	
contentId: java.lang.String	
persistentType: java.lang.String	
inputStream: java.io.InputStream	
filename: java.lang.String	
mimeType: java.lang.String	
): com.mgmtp.a12.contentstore.ContentPersistenceResult	
deleteById(
contentId: java.lang.String	
): void
ContentStream
contentSupplier: java.util.function.Supplier<java.io.InputStream>	
contentType: java.lang.String	
ready: boolean	
isPublic: boolean	
readyLock: java.util.concurrent.locks.Lock	
isReadyCondition: java.util.concurrent.locks.Condition	
setReady(): void	
awaitReady(	
timeoutMs: long	
): boolean
ContentStreamBuilder	ContentPersistenceResult
AttachmentAnnotation
name: java.lang.String	
value: java.lang.String
AttachmentHeader
attachmentId: java.lang.String	
thumbnailBigId: java.lang.String	
thumbnailSmallId: java.lang.String	
filename: java.lang.String	
references: java.util.List<com.mgmtp.a12.dataser vices.attachment.AttachmentReference<com.mgmtp.a12.dataser vices.reference.GenericReference>>	
createdAt: java.time.Instant	
createdBy: java.lang.String	
modifiedAt: java.time.Instant	
modifiedBy: java.lang.String	
mimeType: java.lang.String	
size: java.lang.Long	
typeOfTheContent: com.mgmtp.a12.dataservices.attachment.TypeOfTheContent	
annotations: java.util.List<com.mgmtp.a12.dataser vices.attachment.AttachmentAnnotation>
AttachmentUrl
location: java.lang.String
DataServicesThumbnail
type: com.mgmtp.a12.dataser vices.attachment.ThumbnailType	
mimeType: java.lang.String	
content: java.util.function.Supplier<? extends java.io.InputStream>
AttachmentReference
type: com.mgmtp.a12.dataser vices.attachment.AttachmentReferenceType	
reference: T	
parse(
type: com.mgmtp.a12.dataser vices.attachment.AttachmentReferenceType	
ref: java.lang.String	
): com.mgmtp.a12.dataser vices.attachment.AttachmentReference	
fromDocRef(	
docRef: java.lang.String	
): com.mgmtp.a12.dataser vices.attachment.AttachmentReference	
fromDocRef(	
docRef: com.mgmtp.a12.dataservices.document.DocumentReference	
): com.mgmtp.a12.dataser vices.attachment.AttachmentReference
AttachmentReferenceType	
DOCUMENT;	
values(): Array	
valueOf(
name: java.lang.String	
): com.mgmtp.a12.dataser vices.attachment.AttachmentReferenceType
ThumbnailType	
SMALL, BIG;	
values(): Array	
valueOf(
name: java.lang.String	
): com.mgmtp.a12.dataser vices.attachment.ThumbnailType
GenericReference
«use»
«use»
«use»
«use»
«use»	«use»
«use» 	«use»
«use»
«use»	«use» 	«use»
«use»
«use»	«use»
Figure 29. Attachment diagram
Content Store Persistent Type
We introduce the Persistent Type concept. Currently, we have two types: private and public, each
type has a different process to download content.
255

-- 255 of 334 --

• private:
◦ Content of this type cannot be downloaded directly from Content Store. Instead, one has to
register a download ticket for the desired contentId. The response contains a download URL
which doesn’t include the contentId so that contentId isn’t exposed.
◦ By default, private content can only be downloaded once.
• public:
◦ Content of this type has a static download URL, which remains the same for every request to
get a download URL for this public content.
Content Store Sub Modules
Content Store module includes 6 sub modules:
1. dataservices-content-store-client: The Java clients used for communicating with standalone-
mode Content Store, reference: Content Store client module.
2. dataservices-content-store-core: This module is the Java core of Content Store.
3. dataservices-content-store-core-spring-boot-autoconfigure: This module helps to initialize the
core module with Spring application automatically.
4. dataservices-content-store-server: This module defines the standalone Content Store API.
5. dataservices-content-store-server-app: This module helps to run dataservices-content-store-
server as Spring application.
6. dataservices-content-store-server-spring-boot-autoconfigure: This module helps to initialize
the server as Spring application automatically.
Content Store Configuration
Content Store uses Spring for configuration management. All configuration keys are prefixed with
mgmtp.a12.dataservices.contentstore. In embedded mode, configuration from higher-level modules
can override settings from lower-level modules to ensure flexible integration.
Configuration Options
Permanent Configuration
The following configuration keys should not be changed because Content Store rely on a certain
state of database and configuration of repositories:
spring.datasources.contentstore.liquibase.enabled = true
For enabling liquibase.
spring.datasources.contentstore.liquibase.change-log =
classpath:/contentstore_db/project_model.xml
Liquibase change log configuration.
256

-- 256 of 334 --

spring.jta.enabled = true
JTA is enabled by default.
spring.datasources.contentstore.jpa.hibernate.ddl-auto = validate
Enables the validation of DDL statements.
Changeable Configuration
The following example values use the embedded PostgreSQL configuration suitable for
development and testing. For production usage, configure a persistent external PostgreSQL
database with the appropriate connection URL, credentials, and driver.
spring.datasources.contentstore.embedded-postgres.enabled = true
Enables the embedded PostgreSQL instance for Content Store.
spring.datasources.contentstore.embedded-postgres.port = 5435
Port on which the embedded PostgreSQL instance listens.
spring.datasources.contentstore.jpa.database = postgresql
Spring target database dialect.
You can define your own Liquibase migration datasource by providing a bean with
LiquibaseDatasource annotation like:
Custom Liquibase Migration DataSource
@LiquibaseDataSource @Bean public DataSource contentstoreMigrationDataSource(Object...
params) {
// your implementation
}
If custom liquibase migration datasource is not provided, this block of configuration options
focuses on liquibase datasource for database migration:
spring.datasources.contentstore.liquibase.url
The database connection string to apply liquibase migration, if this property is provided, the
following user and password below are required, if not this connection will be ignored and
Content Store datasource will be applied instead.
spring.datasources.contentstore.liquibase.user
The username which liquibase will use on behalf, while performing migration, if this property is
provided but above liquibase url is missing then Content Store url will be used instead.
spring.datasources.contentstore.liquibase.password
The password of liquibase user
spring.datasources.contentstore.liquibase.driver-class-name
Database driver
257

-- 257 of 334 --

Cache Configuration
By default, the Content Store has second-level caching disabled.
spring.datasources.contentstore.jpa.properties.hibernate.cache.useSecondLevelCache = false
Set to true to enable the usage of second level cache.
spring.datasources.contentstore.jpa.properties.hibernate.cache.useQueryCache = false
Database queries are not cached currently.
Content Store performance relies on avoiding of DB queries rather than caching them.
spring.datasources.contentstore.jpa.properties.hibernate.cache.region.factoryClass =
com.hazelcast.hibernate.HazelcastCacheRegionFactory
Content Store prefers using Hazelcast by default. Please note that no other Factory class has been
tested.
mgmtp.a12.dataservices.contentstore.cache.timeout = 3600: int
Cache timeout for default value of request public url of content parameter. Acceptable
configuration unit is: Second.
Example: 3600 means the public url of content will be expired after 1 hour.
Extension Configuration
mgmtp.a12.dataservices.contentstore.extensions.tika.inMemoryTemp.enabled = false: boolean
If enabled, enforces Tika to use in-memory JimFs as temporary storage during Mime-Type
detection.
Server Configuration
mgmtp.a12.dataservices.contentstore.server.api.enabled = false: boolean
Enable/disable exposing of API controllers. By default it’s enabled, but you could disable it if you
use Content Store in embedded mode.
mgmtp.a12.dataservices.contentstore.server.api.mimeType.trustExternalMimeType.enabled = false:
boolean
Enable/disable mandatory request parameter mimeType in content uploading API: "POST
/api/content". By default, it’s disabled, this means Content Store will probe mime type from the
uploading content, the request parameter is ignored. If this property is enabled, the request
parameter is mandatory and Content Store will take it as content mime type.
mgmtp.a12.dataservices.contentstore.server.contextPath = /cs: java.lang.String
Mappings in Content Store have the following structure:
SPRING_CONTEXT_PATH/CONTENT_STORE_CONTEXT_PATH/… This property should be used to
set CONTENT_STORE_CONTEXT_PATH. Its purpose is to give an ability to differentiate with
DATA_SERVICES_CONTEXT_PATH by introducing your own prefix variable.
NOTES: 1. Don’t put leading '/' if SPRING_CONTEXT_PATH has trailing '/'. It will result in '//' prefix
in the mappings. 2. There is a configuration called mgmtp.a12.uaa.authentication.context-path. It
should be equal to this property for the application to function properly.
258

-- 258 of 334 --

mgmtp.a12.dataservices.contentstore.server.pub.enabled = true: boolean
Enable/disable exposing of public controllers. By default it’s enabled, but you could disable it if
you use Content Store as library of your application, and you handle in your way.
Storage Configuration
mgmtp.a12.dataservices.contentstore.storage.contentStorage = 'FS': enum
Default implementation of content storage. Can be one of FS for filesystem storage, DB for
database storage or OTHER - in this case none of the bundled content storages is used, and you
must provide your own implementation.
mgmtp.a12.dataservices.contentstore.storage.fs.location =
${user.home}/a12/dataservices/contentstore/contents: java.io.File
Content location on file system (Prefix file: is mandatory).
Example: file:/var/lib/a12/dataservices/contentstore/contents
Other Properties
spring.datasources.contentstore.jpa.show-sql = false
By default, queries from Content Store to the database are not logged.
mgmtp.a12.dataservices.contentstore.baseUrl = : java.lang.String
This is the base URL of Content Store which is used for public access. Downloadable URLs
requested by a client (Web Browser) will point to this host. Therefore, the host here should be
public-domain that is accessible from the internet. If Content Store is deployed on a cluster
behind a load balancer, please make sure that this host is pointing to your Content Store public
domain name. This base-url is required, for setting up relative path please use "/". This property
is mandatory for starting up Content Store application:
1. For embedded mode by default this base-url property is set to localhost:8080 which works
well for development mode, please be aware of setting this property properly in your
production. Because this property is used to construct downloadable URLs for content, this
means end users will never be able to download the content by using the default property
localhost:8080. Please set this property by using your public domain which points to Data
Services server and can be accessed from the internet.
2. For the standalone mode please consider the same situation. Set base-url property by using
your public domain which points to Content Store server and can be accessed from the
internet.
mgmtp.a12.dataservices.contentstore.contentWaitReadyTimeout = 10_000: long
This is the timeout for waiting until content stream is ready for downloading.
mgmtp.a12.dataservices.contentstore.enableDefaultDownloadListener = true: boolean
This is for enabling download ready field by default, it’ll set contentStream.ready = true. In case
you turn off default listener here, you must set ready = true to enable downloading.
mgmtp.a12.dataservices.contentstore.limitSize = 10 MiB: java.lang.String
Limit size of content (value is case-insensitive). Acceptable configuration unit is: Kb(Kilobytes),
259

-- 259 of 334 --

Mb(Megabytes), Gb(Gigabytes).
Example: 10 MiB limited content size 10 Megabytes.
mgmtp.a12.dataservices.contentstore.ticketDuration = 5 min: java.lang.String
Expired time for available ticket (value is case-insensitive). Acceptable configuration unit is:
H(hour), M(Minute), S(second).
Example: 5 min ticket will be considered as unavailable after 5 minutes.
mgmtp.a12.dataservices.contentstore.ticketMultiDownload.enabled = false: boolean
Property to allow client downloading content from ticket multiple times until it’s expired. This is
disabled by default.
Content Store Client Properties
These properties should be applied when Data Services is configured to communicate with
standalone-mode Content Store.
mgmtp.a12.dataservices.contentstore.client.configuration.remoteUrl = "": String
This is the Content Store remote URL that Data Services will use to communicate with the
Content Store HTTP APIs. Please note that if Content Store is running on a cluster, then the host
should be the Load Balancer domain name or service name, which Data Services can access
within the intranet.
mgmtp.a12.dataservices.contentstore.client.content.basePrefix = (empty): java.lang.String
String prefix to prepend to relative content download URLs returned by standalone-mode
Content Store. The value is concatenated as-is to the relative URL path without any URL
validation. Any scheme is supported (for example HTTP). When this property is empty, the
relative URL is returned unchanged.
E.g:
• basePrefix = "http://localhost:8080"
• relativeUrl = "/cs/api/content/93ebef0f-b034-4547-afb9-2ab51ab314ba"
• expected downloadUrl = "http://localhost:8080/cs/api/content/93ebef0f-b034-4547-afb9-
2ab51ab314ba"
mgmtp.a12.uaa.authentication.client.rest.authentication-configuration.relative-login.url =
user/local/login
Content Store Client uses the UAA Rest Client Connector. This property is for re-authenticating
the currently logged-in session when making a HTTP call from Content Store Client to
standalone-mode Content Store.
How to Start the Content Store Module
Content Store can be started in two modes: embedded mode and standalone mode.
260

-- 260 of 334 --

Embedded Mode
By default, Data Services operates with Content Store in embedded mode, where the private API for
uploading and requesting downloadable URLs is disabled. Only the public API for downloading
content is exposed. For details on public and private API capabilities, see here.
Standalone Mode
For using Data Services with standalone-mode Content Store please follow these steps:
• Start your Content Store as a standalone application by running this gradle task:
./gradlew :content-store:dataservices-content-store-server-app:bootRun
• This Gradle task starts the Content Store Spring application with built-in uaa profile. If you want
to change UAA configuration please refer to the UAA documentation.
• Data Services uses the client from dataservices-content-store-client to communicate with
Content Store. You should set up UAA configuration for this client to retrieve an authentication
token (please use the same UAA configuration for both Data Services and Content Store).
Currently, we provide authentication for standalone-mode Content Store. This means only
authenticated users can send request to Content Store for manipulating content.
Content Store is designed as an internal service. In any case you want to publish the Content
Store APIs, please extend the APIs and apply suitable UAA-Authorization for your content
protection.
• You may create your own uaa profiles to adapt with your project needs.
• Please take a look at the configuration properties for Content Store. For any JPA custom
properties configuration please override the default:
Default JPA configuration for Content Store
spring.datasources.contentstore.jpa.show-sql=false
spring.datasources.contentstore.jpa.properties.hibernate.cache.use_second_level_cache=
false
spring.datasources.contentstore.jpa.properties.hibernate.cache.use_query_cache=false
spring.datasources.contentstore.jpa.properties.hibernate.cache.region.factory_class=co
m.hazelcast.hibernate.HazelcastCacheRegionFactory
spring.datasources.contentstore.jpa.properties.hibernate.cache.hazelcast.instance_name
=A12S
spring.datasources.contentstore.jpa.properties.hibernate.physical_naming_strategy=org.
hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
spring.datasources.contentstore.jpa.properties.hibernate.implicit_naming_strategy=org.
springframework.boot.hibernate.SpringImplicitNamingStrategy
• In this mode please be sure to have enabled Content Store private API because Data Services
need to communicate with Content Store via this API. By default, Content Store private API is
enabled for the standalone mode.
261

-- 261 of 334 --

Content Store HTTP API
When running in standalone-mode, Content Store publishes an API to manipulate content using 3
controllers:
 Parameters marked with  are mandatory and should not be omitted nor null.
Other parameters (marked with ) can be bypassed or set to null.
Content Store Private HTTP API
Documentation URL Description
Content Store Ticket
Controller REST API
#{@contentStoreProperties.s
erver.contextPath}/api/ticket
This API provides the ability to create a ticket
for downloading private content.
Content Store
Private Controller
REST API
#{@contentStoreProperties.s
erver.contextPath}/api/conte
nt
This API provides the ability to upload, delete
content, or request download URL for public
content.
Content Store Public HTTP API
Documentation URL Description
Content Store Public
Controller REST API
#{@contentStoreProperties.server.co
ntextPath}/download
This API provides the ability to
download content.
ContentStoreTicketController and ContentStorePrivateController are secured; only authenticated
users can access these controllers.
 By default, there is no authorization applying to them. This means, for the current
architecture Data Services should be the only one who can communicate directly
with these controllers for data security.
Content Store Ticket Controller REST API
This API provides the ability to create a ticket for downloading private content.
List of Contents
• Request Ticket to Download Private Content
Request Ticket to Download Private Content
Name Request Ticket to Download Private Content
Description Endpoint that allows generating a download URL of private content.
Method GET
URL #{@contentStoreProperties.server.contextPath}/api/ticket/{contentId}
262

-- 262 of 334 --

Parameters contentId 
contentId to get download url.
duration 
Input string for transferring to seconds in long number, input case is
insensitive.
Success
response
200 OK
Return downloadable URL with ticket id. The client can use this URL to
download the content and can add parameter "filename" to set the file name
for the downloaded file.
Content Store Private Controller REST API
This API provides the ability to upload, delete content, or request download URL for public content.
List of Contents
• Upload Content
• Delete Content
• Get Download URL
Upload Content
Name Upload Content
Description Endpoint that allows uploading of content file.
Method POST
URL #{@contentStoreProperties.server.contextPath}/api/content
Parameters content 
Content upload data.
contentId 
The contentId to save.
persistentType 
Persistent Type for Content, public or private.
filename 
The name of content.
mimeType 
The mime type of uploading content, this mime type will only be accepted if
mgmtp.a12.dataservices.contentstore.server.api.mimeType.trustExternalMimeTy
pe.enabled is true.
Success
response
200 OK
Content has been uploaded to file system.
263

-- 263 of 334 --

Delete Content
Name Delete Content
Description Endpoint that allows deleting content by id.
Method DELETE
URL #{@contentStoreProperties.server.contextPath}/api/content/{id}
Parameters id 
Path variable for deleting content by id.
Success
response
204 NO_CONTENT
Content has been deleted.
Get Download URL
Name Get Download URL
Description Endpoint for requesting downloadable URL for public content by id.
Method GET
URL #{@contentStoreProperties.server.contextPath}/api/content/{id}
Parameters id 
The public content id for requesting url.
Success
response
200 OK
Return downloadable URL from content id. The client can add the parameter
"filename" to set the file name for the downloaded file.
Content Store Public Controller REST API
This API provides the ability to download content.
List of Contents
• Get Content
Get Content
Name Get Content
Description Endpoint allows downloading a content file.
Method GET
URL #{@contentStoreProperties.server.contextPath}/download/{id}
264

-- 264 of 334 --

Parameters id 
contentId of public content or ticketId of private content.
filename 
The content file name of the response, if it’s empty the id would be used.
cacheDuration 
The duration of cache config in seconds and 0 is disabled cache, negative
number is maximum cache duration - 2147483647 seconds. Default value can
be configure through key mgmtp.a12.dataservices.contentstore.cache.timeout.
Success
response
200 OK
File is ready to be downloaded.
Content Store Client Module
This is a Java client library used for communication with Content Store.
There are 3 Content Store clients within this module related to the corresponding 3 Content Store
controllers (see here):
• ContentStorePrivateClient
• ContentStorePublicClient
• ContentStoreTicketClient.
They are provided as Spring beans. Data Services will automatically scan them if it is configured to
communicate with standalone-mode Content Store.
• Please be aware of the Content Store Client configuration mentioned in the section about
Content Store Client Properties
Sequence Diagram
These diagrams below will show how Content requests and responses are handled:
265

-- 265 of 334 --

Data Services 	Content store
client
client
Data-services Core
Data-services Core
Content Store Service
Content Store Service
Content Store Repository
Content Store Repository
Storage
Storage
Event Listener
Event Listener
Save content request
Save Content
ContentBeforeCreateEvent
Size validation
Persist Content
Persist Content
ContentAfterCreateEvent
Save content result
Save content response
Figure 30. Content save diagram
Content Store publishes events
• before content is saved
• after content is saved.
Please take a look at the Content Store Events section for further information.
Data Services 	Content store
client
client
Data-services Core
Data-services Core
Content Store Service
Content Store Service
Ticket Service
Ticket Service
Content Store Repository
Content Store Repository
Content storage
Content storage
Event Listener
Event Listener
Request the content URL
Request ContentUrl
get Content
get Content by contentId
Content
Content
Register ticketId
Registered ticketId
ContentAfterRequestEvent
Temporary valid URL of content with ticketId
Temporary valid URL of content with ticketId
GetContent
Get TicketInfo by TicketID
Validate TicketInfo
a l t 	[Valid Ticket]
ContentBeforeLoadEvent
a l t 	[ContentBeforeLoadEvent has no content supplier]
get Content
get Content by contentId
Content
Content
ContentBeforeDownloadEvent
Response as download stream from content supplier
ContentAfterDownloadEvent
[Invalid Ticket]
404: Not Found
Figure 31. Content download diagram
266

-- 266 of 334 --

Content Store publishes events when
• the content download URL is created
• the content is loaded from storage system
• the content is ready for downloading
• the content is downloaded.
Please take a look at the Content Store Events section for further information.
 This sequence diagram applies for downloading private content. When
downloading public content the flow is easier: we don’t have to register download
ticket to Content Store but all events are still published.
Content Store Events
ContentBeforeDownloadEvent
Triggered before the content is going to be downloaded.
event com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent
listeners com.mgmtp.a12.contentstore.autoconfigure.internal.listener.DefaultContentDownl
oadEventListener#listenOnContentBeforeDownload order: 2147483647
ContentAfterRequestEvent
Triggered after the request for downloading the content is created.
event com.mgmtp.a12.contentstore.events.ContentAfterRequestEvent
ContentBeforeCreateEvent
Triggered before persisting the content to the storage system.
event com.mgmtp.a12.contentstore.events.ContentBeforeCreateEvent
ContentAfterCreateEvent
Triggered after the content is created.
event com.mgmtp.a12.contentstore.events.ContentAfterCreateEvent
ContentAfterDownloadEvent
Triggered after the private content stream is downloaded successfully. This event is not published
when downloading public content.
event com.mgmtp.a12.contentstore.events.ContentAfterDownloadEvent
267

-- 267 of 334 --

ContentStoreInitializationFinishedEvent
Published when Content Store initialization completes and all models and documents are loaded
and indexed. It also indicates that the Content Store server is ready to be used.
event com.mgmtp.a12.contentstore.initialization.events.ContentStoreInitializationFin
ishedEvent
listeners com.mgmtp.a12.contentstore.server.actuator.ContentStoreInitializationFinishedH
ealthIndicator#onApplicationEvent order: 2147483647
Content Store Artifacts
Jar Application
Group ID com.mgmtp.a12.dataservices.contentstore
Artifact ID dataservices-content-store-server-app
Content Store BOM
There is a way to build your own artifact. For this, we offer a BOM artifact.
If you need a custom artifact, you need to extend from main Data Services BOM artifact:
Group ID com.mgmtp.a12.dataservices.contentstore
Artifact ID content-store
then, you have to define the artifact packaging.
This is the recommended way how to extend Content Store because it allows the most flexibility
while not enforcing any target extension (war, jar, fatjar,…).
Content Store Helm Charts
We provide Helm charts to deploy the Content Store application on Kubernetes clusters. The Helm
charts can be found in the artifactory under the following ID:
Group ID com.mgmtp.a12.dataservices
Chart ID a12-content-store
Please note that helm version is different from Data Services version, current chart version is
39.0.2.
Content Store Probing Content Mime Type
268

-- 268 of 334 --

Content Mime Type Probing Mechanism
Mime-type probing for content is implemented in the modules dataservices-common/dataservices-
common-lib and dataservices-common/dataservices-common-api. The dataservices-
common/dataservices-common-api module provides the ContentTypeDetector interface, with its default
implementation TikaContentTypeDetector located in dataservices-common/dataservices-common-lib
and leveraging the Tika library for detection. When a mime type is successfully detected, a
ContentTypeDetectedEvent is published.
Client projects can listen for the ContentTypeDetectedEvent to customize mime-type detection. The
listeners JsonContentTypeListener and MsWordContentTypeListener offer custom handling for
application/json and application/msword mime types in text content.
In Content Store, the DefaultContentStoreService uses the default TikaContentTypeDetector for
probing. In Data Services, both EmbeddedContentStoreAttachmentRepository and
StandaloneContentStoreAttachmentRepository rely on the same default detector.
Custom implementations of ContentTypeDetector are discouraged unless strictly necessary. If
customization is required, it should be handled in the client project or by publishing the
ContentTypeDetectedEvent to utilize the default listeners.
dataservices-common/dataservices-common-api
dataservices
common
content
events
dataservices-common/dataservices-common-lib
ContentTypeDetector
probeContentType(
inputStream: java.io.InputStream
filename: java.lang.String
): java.lang.String
getContentLength(
bytes: Array
): long
ContentTypeDetectedEvent
TikaContentTypeDetector 	MsWordContentTypeListener 	JsonContentTypeListener
Content Store 	Data Services
«trigger» 	«listen» 	«listen»
«use» 	«use»
«use» 	«use»
Figure 32. Content probing mime type class diagram
269

-- 269 of 334 --

Examples
Data Services offer three artifacts that serve as examples of customized DS implementations.
Extending Server
For the examples listed below, refer to the code found in the
com.mgmtp.a12.dataservices:examples:examples-extending-server artifact.
1. Authorization examples
a. Custom authorization of methods
b. Additional ABAC rules for Query
c. Black box authorization for Query
2. Kernel extension examples
a. Custom kernel types and conditions
b. Static validation code configuration
c. Custom serialization of documents
3. Misc
a. Custom exceptions
b. Custom event listeners (e.g attachments encryption and decryption)
c. Custom attachment cleanup condition
d. Custom in memory document read repository
e. Custom external enumeration
f. Custom document model migration
g. Custom operation
h. DS configuration
i. Custom Jackson ObjectMapper configuration
j. Adding custom configuration properties to actuator via @ExposePropertiesToActuator
annotation
k. Custom configuration for probing attachments in Data Services and sending mime-type to
Content Store when creating content. The Content Store will trust the mime-type or not (also
depends on Content Store configuration, please refer to Content Store example profiles).
l. Implementation of com.mgmtp.a12.dataservices.migration.IDocumentMetadataMigrator Custom
definition of
com.mgmtp.a12.dataservices.migration.DefaultDocumentMetadataMigrationStepConfiguration.
m. Custom Query projection
n. public attachments for certain document models
o. Custom document metadata by using
270

-- 270 of 334 --

mgmtp.a12.dataservices.models.metadata.document.path.
Initialization Application
For examples listed below, refer to the code found in the
com.mgmtp.a12.dataservices:examples:examples-dataservices-init-app artifact.
1. Usage of init application
2. Configuration of init application
3. Custom migration in init application
4. Custom annotation composing @MigrationStep annotation
Content Store Server Example
For examples listed below, refer to the code found in the
com.mgmtp.a12.dataservices:examples:dataservices-content-store-server-example artifact.
1. Custom UAA authentication
2. Custom configuration to accept external mime type for content in uploading content API.
Troubleshooting Common Problems
Javadoc is not properly rendered in IDE
DS uses Asciidoctor syntax for Javadoc, which is not natively supported by all IDEs. For proper
rendering in IntelliJ IDEA, install the Asciidoclet plugin or use a suitable alternative for your IDE.
Without the plugin, Javadoc will display in IntelliJ IDEA, but indentation and JSON formatting may
be incorrect.
Eclipse supports rendering Javadoc in Asciidoc format with available plugins, but Visual Studio
Code currently lacks native support for this feature.
Error org.quartz.SchedulerException: Job instantiation
failed
This is typically caused by the existing job definitions stored in the database at table
qrtz_job_details column job_class_name. Spring Quartz fails to initialize the job beans because of
the changes of class path or class name.
This is just a warning log, it won’t crash the application from bootstrapping and requires migration
effort to adapt the classpath changes.
Quartz job may run when disabling
By default, we configure that Quartz stores all job information in the database, and we set the
271

-- 271 of 334 --

property spring.quartz.jdbc.initialize-schema=never, it prevents automatic table creation on
startup. This can lead to unexpected behavior when disabling jobs.
Here’s Why:
• If you set mgmtp.a12.dataservices.jobs.enabled=false to disable jobs after they have already
been run, the job information will still be present in the database.
• Since the initialize-schema property is set to never, the Quartz job manager won’t automatically
clean up old job data upon disabling them.
• Therefore, the job may still run even though you disabled it.
To completely turn off jobs and remove their data, you need to combine both properties:
mgmtp.a12.dataservices.jobs.enabled=false
spring.quartz.jdbc.initialize-schema=always
Explanation:
• Setting mgmtp.a12.dataservices.jobs.enabled=false ensures jobs are disabled.
• Setting spring.quartz.jdbc.initialize-schema=always forces the job manager to create tables on
startup, which will also clean up any existing data for disabled jobs. This combined approach
guarantees that jobs are truly disabled, and their information is removed from the database.
Dynamic Gradle Versions
Please note that our application currently does not support dynamic Gradle versioning. Attempting
to utilize dynamic versioning may lead to unexpected errors or incompatibilities. For stable
performance, we recommend using static Gradle versions and explicit dependency definitions in
your projects.
Hazelcast Warnings In Logs
[main ][WARN ][com.hazelcast.cp.CPSubsystem ][u:] -
[10.54.100.19]:64622 [a12s_it_17320995465710.5125543610423723] [5.4.0] CP Subsystem is
not enabled. CP data structures will operate in UNSAFE mode! Please note that UNSAFE
mode will not provide strong consistency guarantees.
[main ][WARN ][com.hazelcast.instance.impl.Node ][u:] -
[10.54.100.19]:64622 [a12s_it_17320995465710.5125543610423723] [5.4.0] No join method
is enabled! Starting standalone.
These logs are related to standalone mode. They will disappear when you run Hazelcast in cluster
mode and configure the CP Subsystem.
[main ][WARN ][org.hibernate.orm.deprecation ][u:] -
272

-- 272 of 334 --

HHH90000025: PostgreSQLDialect does not need to be specified explicitly using
'hibernate.dialect' (remove the property setting and it will be selected by default)
This log is related to the configuration of the Postgres database, as Data Services explicitly set the
Postgres driver and dialect.
Spring Boot Issues When Upgrading From 3.3.x to 3.4.x
During Spring Boot upgrade to version 3.4.x we observed several problems with resource
resolution in fatjars. It happens just in several projects. If you’re one of them, downgrade Spring
Boot to version 3.3.x by something like: implementation
enforcedPlatform('org.springframework.boot:spring-boot-dependencies:3.3.7'). Unfortunately, we
didn’t find the cause and have a better solution yet.
Configuration Change Does Not Apply In Spring Boot
Integration Tests For Data Services
We use Spring Boot conditions to create specific beans based on configuration. To achieve this,
values from the ConfigurationProperties annotated class
(com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties) are propagated to
com.mgmtp.a12.dataservices.configuration.condition.internal.AbstractDataServicesCondition.
However, as noted in the Spring Boot issue tracker, there is no direct support for this. Consequently,
we bind these properties from the application context to a static map in the
AbstractDataServicesCondition class. This approach introduces a limitation: since static fields
persist for the duration of a JVM execution, configuration changes will not take effect when
multiple tests with different configurations are executed in the same JVM unless the map is
refreshed. The solution involves resetting the static dependency manually, as shown below:
Resetting static dependency in AbstractDataServicesCondition
/**
* Since we have the "boundProperties" in {@link AbstractDataServicesCondition} as
static dependency, which makes
* running test with multiple instances fail because the static dependency won't reset
its default value.
*/
private void reboundDataServicesCoreProperties() {
ReflectionTestUtils.setField(AbstractDataServicesCondition.class, "
boundProperties", Optional.empty());
}
Consider the following test case that illustrates the issue with static bound properties:
Example test case demonstrating static bound properties issue
@SpringBootTest
273

-- 273 of 334 --

class StaticBoundPropertiesIssueExplorationTest {
@Nested
@TestPropertySource(properties =
"mgmtp.a12.dataservices.query.reindexing.mode=DISABLED")
class GivenReindexingModeDisabled {
@Autowired private DataServicesCoreProperties coreProperties;
@Test void shouldBindProperty() {
// then
assertThat(coreProperties.getQuery().getReIndexing().getMode()).isEqualTo
(DataServicesCoreProperties.Query.Reindexing.Mode.DISABLED);
}
}
@Nested
@TestPropertySource(properties =
"mgmtp.a12.dataservices.query.reindexing.mode=REBUILD_INDEX")
class GivenQueryReindexingModeRebuildIndex {
@Autowired private DataServicesCoreProperties coreProperties;
@Test void shouldBindProperty() {
// then
assertThat(coreProperties.getQuery().getReIndexing().getMode()).isEqualTo
(DataServicesCoreProperties.Query.Reindexing.Mode.REBUILD_INDEX);
}
}
}
In this example, the GivenQueryReindexingModeRebuildIndex test would fail because the static
dependency in AbstractDataServicesCondition retains stale values from the
GivenReindexingModeDisabled test. To ensure the configuration change is applied, you must invoke
the reboundDataServicesCoreProperties method before running
GivenQueryReindexingModeRebuildIndex.
Server Fails to Start with Actuator Misconfiguration
When both properties management.endpoints.enabled-by-default and
management.endpoints.access.default are present in the configuration the server will fail to start
with an error like
Example error log
***************************
APPLICATION FAILED TO START
***************************
274

-- 274 of 334 --

Description:
The following configuration properties are mutually exclusive:
management.endpoints.access.default
management.endpoints.enabled-by-default
This may happen when configuration of the actuators by helm chart is used because Helm does not
support the property management.endpoints.access.default of Spring Boot 3.4.
We recommend to use the DataServices actuator profile until Helm is updates to the new Spring
Boot version.
OOMKilled Errors for Kubernetes Deployments
By default, when an application runs inside a container, the JVM is unaware of the container’s
memory limit and may attempt to use more memory than allocated. If this occurs, Kubernetes will
terminate the container with an OOMKilled error. To prevent this, add the -XX:MaxRAMPercentage=80.
This tells the JVM: "Use a maximum of 80% of the container’s assigned memory for the Java heap.
Using
com.mgmtp.a12.uaa.authentication.backend.BackendAuthent
icationService with Asynchronous Tasks
Data Services currently uses the MODE_INHERITABLETHREADLOCAL strategy for SecurityContextHolder.
This means changes made by a child thread affect the main thread.
BackendAuthenticationService.executeWithBackendAuthentication attempts to preserve the original
authentication instance in the security context and restore it after execution. This behavior is
incompatible with asynchronous execution. To use backend authentication in asynchronous
contexts, we recommend using DelegatingSecurityContextTaskExecutor.
Example of using BackendAuthenticationService with asynchronous tasks
@Async
@CommonDataServicesEventListener public void listenOnServicesInitializationFinished
(DataServicesInitializationFinishedEvent dataServicesInitializationFinishedEvent) {
DelegatingSecurityContextTaskExecutor executor =
new DelegatingSecurityContextTaskExecutor(new SyncTaskExecutor(),
SecurityContextHolder.createEmptyContext());
Runnable originalRunnable = () -> backendAuthenticationService
.executeWithBackendAuthentication(
"superAdmin",
() -> {
// run your codes
return null;
});
executor.execute(originalRunnable);
275

-- 275 of 334 --

}
Explanation: In an asynchronous event listener, DelegatingSecurityContextTaskExecutor uses a new
security context, which backendAuthenticationService modifies. This prevents changes from
affecting the main security context.
Missing Locale en-US.UTF8
Some Linux distributions do not have the en-US.UTF8 locale installed by default. This may lead to
issues when running Data Services, as it expects this locale to be available.
Symptoms
When trying to start the PAC or the Project Template start fails with the following error message:
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating
bean with name 'dsDataSource' defined in class path resource
[com/mgmtp/a12/dataservices/autoconfigure/DSEmbeddedPostgresDatasourceConfiguration.cl
ass]: Failed to instantiate [javax.sql.DataSource]: Factory method 'dsDataSource'
threw exception with message: Process [/tmp/embedded-pg/PG-
eeb889eb8aa39ea3cb783f5a8b3fbe01/bin/initdb, -A, trust, -U, postgres, -D,
../postgres/ds-embedded-postgres, -E, UTF-8, --lc-ctype=en_US.UTF-8] failed
Workaround
Install the en_US.UTF8 locale.
For Debian-based distributions (like Ubuntu), you can do this by running:
sudo locale-gen en_US.UTF-8
sudo update-locale
For Red Hat-based distributions (like CentOS), you can do this by running:
localedef -i en_US -f UTF-8 en_US.UTF-8
After installing the locale, restart your terminal or system to apply the changes.
Embedded PostgreSQL Fails to Initialize on Windows
(Missing Visual C++ Redistributable)
When running the Server with embedded PostgreSQL on Windows, the initialization may fail if the
required Visual C++ Redistributable is not installed or if an incorrect version is installed.
276

-- 276 of 334 --

Symptoms
The application fails to start with an error indicating that the initdb.exe process failed:
Caused by: java.lang.IllegalStateException: Process
[C:\Users\...\AppData\Local\Temp\embedded-pg\PG-...\bin\initdb.exe, -A, trust, -U,
postgres, -D, ..., -E, UTF-8, --lc-ctype=en_US.UTF-8] failed
This generic error message can have multiple root causes:
• Missing UTF-8 locale (en_US.UTF-8) - see the previous section
• Missing Visual C++ Redistributable (covered in this section)
• Wrong version of Visual C++ Redistributable installed
Root Cause
The embedded PostgreSQL binaries for Windows depend on specific Microsoft Visual C++ Runtime
DLLs. When these DLLs are not present on the system, initdb.exe cannot load and fails silently with
the generic error shown above.
The specific DLL often missing is msvcr120.dll, which is part of the Visual C 2013 Redistributable
(v12). However, installing the latest Visual C Redistributable (v14) is recommended as it provides
backward compatibility and better long-term support.
Solution
Install the latest Microsoft Visual C++ Redistributable from the official Microsoft download page:
https://learn.microsoft.com/en-us/cpp/windows/latest-supported-vc-redist
Download and install both the x86 and x64 versions if you are on a 64-bit Windows system to
ensure compatibility with all application components.
After installation, restart your terminal or IDE and retry starting the application.
Diagnostic Steps
To confirm that this is indeed a Visual C++ Redistributable issue, you can manually run initdb.exe
to see the specific error:
1. Locate the embedded PostgreSQL directory in your temp folder:
C:\Users\<YourUsername>\AppData\Local\Temp\embedded-pg\PG-<hash>\bin\
2. Open a command prompt or PowerShell in that directory and run:
277

-- 277 of 334 --

.\initdb.exe --version
3. If the Visual C++ Redistributable is missing, you will see an error like:
error while loading shared libraries: msvcr120.dll: cannot open shared object file:
no such file or directory
This confirms that the Visual C++ Redistributable installation is required.
Shared Memory Limit Reached on macOS (shmget
failed)
When running multiple instances of Data Services or embedded PostgreSQL on macOS, the system
may fail to initialize the database due to restrictive kernel limits on shared memory.
Symptoms
The application fails to start with an error log indicating a failure in the initdb process:
Process [.../initdb, -A, trust, -U, postgres, -D, ..., -E, UTF-8, --lc-ctype=en_US.UTF
-8] failed:
FATAL: could not create shared memory segment: No space left on device
DETAIL: Failed system call was shmget(key=19597448, size=56, 03600).
HINT: This error does *not* mean that you have run out of disk space. It occurs
either if all available shared memory IDs have been taken... or because the system's
overall limit for shared memory has been reached.
Root Cause
macOS has low default limits for System V shared memory segments (SHMMNI) and total shared
memory (SHMALL). Each PostgreSQL instance requires its own segment; once the system limit is
reached, initdb cannot allocate the memory required for verification.
Workaround
You must increase the kernel’s shared memory limits via sysctl.
1. Create or edit the configuration file:
sudo nano /etc/sysctl.conf
2. Add the following parameters:
# Increase maximum number of shared memory segments (e.g., to 128)
278

-- 278 of 334 --

kern.sysv.shmmni=128
# Increase maximum shared memory segment size (e.g., to 1GB - 1073741824 bytes)
kern.sysv.shmmax=1073741824
# Increase total shared memory system-wide (e.g., to 4GB - 4194304 kbytes)
kern.sysv.shmall=4194304
3. Apply the changes:
Since macOS sysctl does not support the -p flag, you should restart your system to ensure the
changes persist.
Liquibase Checksum Invalid Errors in Version 4.33.0
Symptoms
Some deployments using Liquibase version 4.33.0 may fail with a checksum mismatch (e.g.,
"checksum invalid") even though the changeSets have not been modified. This behavior is due to
a known issue in the checksum calculation logic that can surface across environments or after
minor formatting changes.
Previous Workaround
Historically, the issue was bypassed by using the validCheckSum attribute to explicitly accept the
previously calculated checksum:
<changeSet id="my-change" author="ds">
<validCheckSum>7:abcdef1234567890</validCheckSum>
</changeSet>
While effective, this approach is fragile, obscures real database drift, and introduces
unnecessary maintenance overhead.
Resolution / Recommended Action
Upgrade Liquibase to version 5.0.1 (or newer). This version contains fixes that resolve the
underlying checksum calculation issue.
References
JavaDoc
• aggregated Javadoc for all Data Services and Content Store artifacts
279

-- 279 of 334 --

TypeDoc
• dataservices-access
Infrastructure Dependencies
In the table below, the infrastructure dependencies required by Data Services are listed with their
purpose, supported versions, resource recommendations, and configuration links.
Depende
ncy
Purpose Supporte
d
Versions
Configuration
Reference
Minimum Resource
Recommendation
Notes
Postgres Stores persistent
data (all DS managed
data)
16,17,18 Please use the link
for connection
configuration and
use the following
link for Postgres
Setup.
2 CPUs, 8 GB RAM,
100 GB storage
Infinispa
n
(embedde
d)
Distributed in-
memory cache for
clustered
deployments
16.1.x See Caching for
configuration and
Kubernetes
deployment
instructions.
No separate process
required — runs
embedded in the
Data Services JVM.
Required
only for
clustered
(multi-
pod)
deployme
nts.
Single-
node
deployme
nts
require
no
additiona
l
infrastruc
ture.
 Recommendation based on performance and load tests with defined document
counts, link complexity, and model complexity.
Only PostgreSQL is supported as the database for Data Services.
• All DS functional tests are executed against PostgreSQL versions 16, 17, and 18.
• Performance tests are executed only on PostgreSQL version 16.
280

-- 280 of 334 --

PostgreSQL Test Coverage Matrix
The following table describes which test types are executed against each supported PostgreSQL
version:
PostgreSQL Version Test Coverage
16 Regression tests (nightly pipeline)
17 Regression tests (planned, see A12S-6628)
18 Unit tests, integration tests, regression tests (PR builds and nightly
pipeline)
 PostgreSQL 18 is the primary version used for all test types in PR builds and
nightly pipelines. PostgreSQL 16 regression tests provide backward compatibility
verification.
Data Services uses Infinispan as an embedded cache provider for clustered deployments. No
separate Infinispan server is required — Infinispan runs embedded within the Data Services JVM.
• If DS is run as a single node, no JGroups transport is configured and no additional infrastructure
is needed.
See PostgreSQL Setup for recommended PostgreSQL configuration.
Migration Instructions
 Please have a look at Migration to latest A12 chapter for an explanation of general
steps on how to upgrade before starting with the component migration to 2026.06.
2026.06
Deprecations
MISC
Deprecated element Since Replacement Notes
contentstore-embedded_h2 39.0.0 None Retained for Content
Store testing only. Do
not use in application
configurations. It will
be removed in a future
release.
Breaking Changes
281

-- 281 of 334 --

Java API
Removed element Migration
com.mgmtp.a12.dataservices.document.D
ocumentReferenceToStringConverter
Review all call sites for
com.mgmtp.a12.dataservices.document.DocumentReferenceToS
tringConverter.convert(DocumentReference value) and
ensure no null values are passed. Passing null will now
throw a NullPointerException at runtime.
com.mgmtp.a12.dataservices.relationsh
ip.spec.RelationshipRoleSpec
Review all call sites for
com.mgmtp.a12.dataservices.relationship.spec.Relationshi
pRoleSpec(String role, DocumentReference
documentReference) and ensure neither parameter is null.
Both parameters are now mandatory.
com.mgmtp.a12.dataservices.relationsh
ip.RelationshipLinkDocumentSerializat
ionException
Review all call sites for
com.mgmtp.a12.dataservices.relationship.RelationshipLink
DocumentSerializationException(String, String,
IProblemReporter) and ensure the problems parameter is
not null.
com.mgmtp.a12.dataservices.initializa
tion.BusinessModelInitializer
If you reference these classes directly (which is
discouraged as they are internal), update the import
statements to use the new initialization.internal
package.
com.mgmtp.a12.dataservices.model.grap
h.ModelGraphGenerator
Update any code using
com.mgmtp.a12.dataservices.model.graph.ModelGraphGenerat
or to use the new 2-parameter constructor signature.
com.mgmtp.a12.dataservices.utils.Mode
lUtils.validateHeterogeneity(java.uti
l.List)
If you called validateHeterogeneity, move the validation to
your own code or remove the call — inheritance loop
detection is now handled internally.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_000
3
com.mgmtp.a12.dataservices.utils.Mode
lUtils.validateHeader(com.mgmtp.a12.d
ataservices.model.Header, boolean)
Perform the equivalent header checks directly in your
code, or rely on the exception thrown during model
creation.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_000
4
com.mgmtp.a12.dataservices.utils.Mode
lUtils.getObjectRoles(com.mgmtp.a12.d
ataservices.model.Header)
Extract the roles from the header annotations directly
using header.getAnnotations().
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_000
5
com.mgmtp.a12.dataservices.utils.Mode
lUtils.getMatchingRoles(com.mgmtp.a12
.dataservices.model.Header)
Extract the roles from the header annotations directly
using header.getAnnotations().
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_000
6
282

-- 282 of 334 --

Removed element Migration
com.mgmtp.a12.dataservices.rpc.RpcExc
eptionSupport.createException()
Use one of the remaining overloads of createException.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_001
6
com.mgmtp.a12.dataservices.attachment
.ThumbnailType.getNameSuffix()
No replacement — method was not used by any public
API.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_001
7
com.mgmtp.a12.dataservices.attachment
.AttachmentHeaderSpec
Use other constructors of
com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSp
ec.
com.mgmtp.a12.dataservices.attachment
.persistence.IAttachmentRepository.cr
eateAttachment()
Use
com.mgmtp.a12.dataservices.attachment.persistence.IAttac
hmentRepository.create(…) instead.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_001
9
com.mgmtp.a12.dataservices.attachment
.persistence.IAttachmentRepository.cr
eateThumbnail()
Use
com.mgmtp.a12.dataservices.attachment.persistence.IAttac
hmentRepository.create(…) instead.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_002
0
com.mgmtp.a12.dataservices.attachment
.persistence.IAttachmentRepository.fi
ndAttachmentUrl()
Use
com.mgmtp.a12.dataservices.attachment.persistence.IAttac
hmentRepository.findUrl(…) instead.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_002
1
com.mgmtp.a12.dataservices.attachment
.persistence.IAttachmentRepository.fi
ndThumbnailUrl()
Use
com.mgmtp.a12.dataservices.attachment.persistence.IAttac
hmentRepository.findUrl(…) instead.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_002
2
com.mgmtp.a12.dataservices.attachment
.persistence.IAttachmentRepository.de
leteThumbnail()
Use
com.mgmtp.a12.dataservices.attachment.persistence.IAttac
hmentRepository.delete(…) instead.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_002
3
com.mgmtp.a12.dataservices.server.Thr
eadCleanupEvent
Remove any references to
com.mgmtp.a12.dataservices.server.ThreadCleanupEvent. No
replacement — class was not intended for public usage.
283

-- 283 of 334 --

Removed element Migration
com.mgmtp.a12.dataservices.server.Thr
eadCleanupFilter
Remove any references to
com.mgmtp.a12.dataservices.server.ThreadCleanupFilter.
No replacement — class was not intended for public usage.
com.mgmtp.a12.dataservices.document.D
ocumentGraphService
Use QueryService (or the QUERY operation) with the
documentGraph projection instead. No replacement for the
removed graph API classes.
com.mgmtp.a12.dataservices.relationsh
ip.RelationshipMigration
Use the dataservices-relationship-model-migration npm
package for relationship model migration instead.
com.mgmtp.a12.dataservices.attachment
.persistence.IAttachmentRepository.cr
eate(java.lang.String,
java.io.InputStream,
java.lang.String,
com.mgmtp.a12.dataservices.attachment
.TypeOfTheContent, java.lang.String)
Review custom implementations of
IAttachmentRepository.create(…) and add a null check for
typeOfTheContent.
com.mgmtp.a12.dataservices.server.uti
l.KernelCachesPreloader
Remove all direct references to
com.mgmtp.a12.dataservices.server.util.KernelCachesPrelo
ader. This class is not part of the public API.
com.mgmtp.a12.dataservices.model.clie
nt.rest.RestModelsClient.importModelB
ulk(java.io.InputStream)
Replace any usage of
RestModelsClient.importModelBulk(InputStream) with
RestModelsClient.importRuntimeModels(InputStream).
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_001
4
com.mgmtp.a12.dataservices.model.bulk
load.BulkImporterConfiguration
Replace
com.mgmtp.a12.dataservices.model.bulkload.BulkImporterCo
nfiguration with
com.mgmtp.a12.dataservices.initialization.ModelImportCon
figuration.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_006
2
com.mgmtp.a12.dataservices.document.D
ocumentDetail
Use com.mgmtp.a12.dataservices.document.DocumentSpec to
access document content instead of DocumentDetail.
com.mgmtp.a12.dataservices.model.bulk
load.ModelBulkImportExceptionMapper
Remove all references to
com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImpor
tExceptionMapper. Internal class removed together with the
bulk import subsystem.
com.mgmtp.a12.dataservices.model.bulk
load.CollapsingDocumentModelReference
Resolver
Remove all references to
com.mgmtp.a12.dataservices.model.bulkload.CollapsingDocu
mentModelReferenceResolver. Internal class removed
together with the bulk import subsystem.
284

-- 284 of 334 --

Removed element Migration
com.mgmtp.a12.dataservices.model.bulk
load.BulkImportProblemReporter
Remove all references to
com.mgmtp.a12.dataservices.model.bulkload.BulkImportProb
lemReporter. Removed together with ModelBulkImporter. No
direct replacement; the concept is no longer needed.
Automated:
com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_SNAPSHOT
_0002
com.mgmtp.a12.dataservices.model.bulk
load.ModelBulkImportException
Remove all catch blocks and references to
com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImpor
tException. Removed together with ModelBulkImporter. No
direct replacement.
@NonNull annotations added — NullPointerException on null parameters
Impact
Several methods and constructors now have @NonNull annotations on parameters that previously
had undefined behavior when null was passed. Passing null to these parameters will now throw a
NullPointerException at runtime.
Affected APIs:
• com.mgmtp.a12.dataservices.anonymization.AsterixAnonymizer.apply(String s) — parameter s
• com.mgmtp.a12.dataservices.document.DocumentReferenceToStringConverter.convert(DocumentRefe
rence value) — parameter value
• com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec(String role,
DocumentReference documentReference) — parameters role, documentReference
• com.mgmtp.a12.dataservices.relationship.RelationshipLinkDocumentSerializationException(Stri
ng, String, IProblemReporter) — parameter problems
Migration steps
• Review all call sites for these constructors and methods and ensure no null values are passed.
• Update any code using com.mgmtp.a12.dataservices.model.graph.ModelGraphGenerator to use the
new constructor signature.
@Transactional removed from ModelService, IModelRepository, and RelationshipModelLoader interfaces
Impact
The @Transactional annotations have been removed from the method declarations of the following
interfaces:
• com.mgmtp.a12.dataservices.model.ModelService
• com.mgmtp.a12.dataservices.model.persistence.IModelRepository
• com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader
Code that directly implemented any of these interfaces and relied on the interface-level
285

-- 285 of 334 --

@Transactional annotation to provide transaction semantics will no longer inherit that annotation.
Migration steps
• If your code implements ModelService, IModelRepository, or RelationshipModelLoader directly and
requires transactional behavior, annotate the relevant methods in your implementation class
with @Transactional.
• If your code only consumes these interfaces (does not implement them), no changes are
required.
// Before (relied on @Transactional from interface — no longer present)
public class MyModelService implements ModelService {
@Override
public GenericModel create(String modelContent) { ... }
}
// After — add @Transactional explicitly where needed
public class MyModelService implements ModelService {
@Override
@Transactional
public GenericModel create(String modelContent) { ... }
}
ModelService.findAllHeaders() changed from default to abstract
Impact
Any class that implements com.mgmtp.a12.dataservices.model.ModelService without overriding
findAllHeaders() will fail to compile.
Migration steps
• Add an implementation of findAllHeaders() to every custom ModelService implementation.
// Before (compiled because default implementation existed)
public class MyModelService implements ModelService {
// findAllHeaders() not overridden — silently threw UnsupportedOperationException
at runtime
}
// After — must provide an implementation
public class MyModelService implements ModelService {
@Override
public Collection<Header> findAllHeaders() {
// provide your implementation here
}
}
286

-- 286 of 334 --

Relationship Model Version 4.0.0 — Unused Properties Removed
Impact
The Relationship Model has been updated to version 4.0.0. The following Java types have been
removed:
• com.mgmtp.a12.dataservices.relationship.model.CandidateConstraints — class deleted
• com.mgmtp.a12.dataservices.relationship.model.PopulationParameters — class deleted
The following fields have been removed: RelationshipModelContent#associationType,
RelationshipModelContent#storage, RelationshipModelContent#embeddedGroupPath,
EntityCharacteristics#navigable, EntityCharacteristics#candidateConstraints,
Multiplicity#lowerLimit.
Migration steps
• Use the TypeScript dataservices-relationship-model-migration tool to migrate
TypeScript/frontend model files.
• Models stored in the server-side database do not require migration — the server ignores
removed properties during deserialization.
ModelTypeService implementors must implement three new abstract methods
Impact
Any class that implements com.mgmtp.a12.dataservices.model.ModelTypeService directly will fail to
compile until all three new methods are implemented: findRootModelName,
findModelNameAndAllSubtypes, isSubtype.
Migration steps
• Add implementations for the three new methods to every custom ModelTypeService
implementation: findRootModelName(String), findModelNameAndAllSubtypes(String),
isSubtype(String, String).
public class MyModelTypeService implements ModelTypeService {
@Override
public String findRootModelName(String documentModelName) {
// traverse the supertype chain upward; return documentModelName if already
the root
}
@Override
public Set<String> findModelNameAndAllSubtypes(String documentModelName) {
Set<String> result = new HashSet<>(findAllSubtypes(documentModelName));
result.add(documentModelName);
return result;
}
@Override
public boolean isSubtype(String parentModelName, String testedModelName) {
287

-- 287 of 334 --

return parentModelName.equals(testedModelName)
|| findAllSubtypes(parentModelName).contains(testedModelName);
}
}
Order record constructor extended from 4 to 5 parameters
Impact
Code using the 4-parameter com.mgmtp.a12.dataservices.query.Order constructor will not compile.
Migration steps
• Use one of the convenience constructors (e.g. new Order("/Contract/Name", Direction.ASC)) or
the new 5-parameter constructor.
// Before (BREAKS)
new Order(Direction.ASC, "/Contract/Name", false, NullHandling.NULLS_LAST)
// After - Option 1: Use convenience constructors (recommended)
new Order("/Contract/Name", Direction.ASC)
// After - Option 2: Use 5-parameter constructor
new Order(Direction.ASC, "/Contract/Name", false, NullHandling.NULLS_LAST, null)
DocumentValidationError merged into DocumentValidationResult
Impact
Code referencing com.mgmtp.a12.dataservices.document.operation.validate.DocumentValidationError
or the referencedFields field will not compile. The field validationErrors in ValidateDocumentResult
is now of type List<DocumentValidationResult> instead of List<DocumentValidationError>.
Migration steps
• Replace all usages of
com.mgmtp.a12.dataservices.document.operation.validate.DocumentValidationError with
com.mgmtp.a12.dataservices.document.DocumentValidationResult.
• Rename field access referencedFields to referencedFieldsPointers.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_0012.
ModelsClient.importModelBulk renamed to importRuntimeModels
Impact
Code calling com.mgmtp.a12.dataservices.model.client.ModelsClient.importModelBulk(InputStream)
on ModelsClient or RestModelsClient will not compile. References to removed internal classes or
BulkImporterConfiguration will also not compile.
Migration steps
• Replace any usage of ModelsClient.importModelBulk(InputStream) with
288

-- 288 of 334 --

ModelsClient.importRuntimeModels(InputStream). The API behavior is unchanged.
• Replace any usage of BulkImporterConfiguration with ModelImportConfiguration.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_0013.
ExpandIncludesListener removed — runtime model expansion no longer supported
Impact
Data Services no longer performs runtime expansion of document model includes or injection of
__meta metadata during model creation and update. All document models must now be provided as
runtime models, fully processed by the WCF/RMC build-time pipeline before being imported into
Data Services.
Migration steps
• Process all document models through the WCF/RMC pipeline before importing them into Data
Services.
• Run: java -jar dataservices-wcf-cli-fatjar.jar <input-dir> <output-dir> -c rmc-conversion-
fatjar.jar
Solr-based Query API removed — FilterSpec, PageSpec, SortSpec, ResultSet
Impact
The following classes forming the legacy Solr-based query API have been removed:
• com.mgmtp.a12.dataservices.rpc.query.FilterSpec
• com.mgmtp.a12.dataservices.rpc.query.PageSpec
• com.mgmtp.a12.dataservices.rpc.query.SortSpec
• com.mgmtp.a12.dataservices.rpc.query.ResultSet<T>
Any code referencing these classes will fail to compile.
Migration steps
• Use com.mgmtp.a12.dataservices.query.topology.QueryRoot with the Query API instead of
FilterSpec.
• Use the paging mechanisms provided by the Query API (com.mgmtp.a12.dataservices.query.Page)
instead of PageSpec.
• Use com.mgmtp.a12.dataservices.query.Order instead of SortSpec.
• Use the Query API result types returned by QueryService instead of ResultSet.
Document and DocumentDetail domain classes removed
Impact
The following unused document domain classes have been removed:
• com.mgmtp.a12.dataservices.document.Document
289

-- 289 of 334 --

• com.mgmtp.a12.dataservices.document.DocumentDetail
Any code referencing these classes will fail to compile.
Migration steps
• Use com.mgmtp.a12.dataservices.document.DocumentSpec instead of Document.
• No direct replacement for DocumentDetail — document content is available via DocumentSpec.
Solr facet API removed — com.mgmtp.a12.dataservices.document.search.facets
Impact
The entire Solr-based facet API has been removed. All request and response classes in the
com.mgmtp.a12.dataservices.document.search.facets package have been removed. Any code
referencing these classes will fail to compile.
Migration steps
• Use the Query API aggregation operators (term_aggregation, range_aggregation, etc.) to perform
equivalent aggregations.
• See the Query API documentation for details.
Relationship exception classes removed
Impact
The following deprecated exception classes have been removed:
• com.mgmtp.a12.dataservices.relationship.exception.RelationshipInvalidDocumentModelException
• com.mgmtp.a12.dataservices.relationship.exception.RelationshipInvalidIdException
• com.mgmtp.a12.dataservices.relationship.exception.RelationshipLinkDocumentModelMissingExcep
tion
• com.mgmtp.a12.dataservices.relationship.exception.RelationshipLinkDocumentSerializationExce
ption
Any code referencing these classes will fail to compile.
Migration steps
• Remove any catch blocks or references to these exception types.
• The relationship service now throws
com.mgmtp.a12.dataservices.common.exception.NotFoundException or
com.mgmtp.a12.dataservices.common.exception.InvalidInputException instead.
Various internal classes removed (LinkResultEntry, RoleDocDescriptor, SearchUtils, etc.)
Impact
The following classes have been removed:
• com.mgmtp.a12.dataservices.relationship.spec.LinkResultEntry
• com.mgmtp.a12.dataservices.relationship.spec.RoleDocDescriptor
290

-- 290 of 334 --

• com.mgmtp.a12.dataservices.search.SearchUtils
• com.mgmtp.a12.dataservices.search.exception.InvalidFacetException
• com.mgmtp.a12.dataservices.attachment.UploadedAttachmentDetail
• com.mgmtp.a12.dataservices.relationship.migrator.model.IRelationshipModelMigrator
• com.mgmtp.a12.dataservices.relationship.migrator.model.v1.RelationshipModelMigratorV1ToV3
• com.mgmtp.a12.dataservices.relationship.migrator.model.v1.RelationshipModelV1
• com.mgmtp.a12.dataservices.relationship.migrator.model.v1.RelationshipModelWrapperV1
• com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelMigratorV2ToV3
• com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2
• com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3
• com.mgmtp.a12.dataservices.relationship.migrator.RelationshipMigration
• com.mgmtp.a12.dataservices.export.DocumentExportService
• com.mgmtp.a12.dataservices.document.operation.events.ListDocumentsBeforeEvent
Any code referencing these classes will fail to compile.
Migration steps
• Use com.mgmtp.a12.dataservices.query.operation.events.QueryAfterOperationEvent instead of
ListDocumentsBeforeEvent.
• Remove references to all other removed classes — they were not used by any public API.
DefaultRelationshipModelSerializer no longer a Spring component
Impact
Projects that relied on Spring component scanning to discover
com.mgmtp.a12.dataservices.relationship.serialization.DefaultRelationshipModelSerializer
automatically (without using the Data Services auto-configuration) will fail to start with a missing
bean error for RelationshipModelSerializer.
Migration steps
• If your application uses the Data Services Spring Boot auto-configuration
(DataServicesCoreAutoconfiguration), no action is required.
• If your application does not use the auto-configuration, register the bean explicitly or provide
your own implementation of RelationshipModelSerializer as a Spring bean.
@Bean
public RelationshipModelSerializer relationshipModelSerializer(ObjectMapper
objectMapper) {
return new DefaultRelationshipModelSerializer(objectMapper);
}
291

-- 291 of 334 --

CoreTooling* classes renamed in dataservices-modelgraph-fs-impl
Impact
The following internal classes have been renamed in the dataservices-modelgraph-fs-impl module:
• CoreToolingModelService → FileBasedModelService
• CoreToolingRelationshipModelLoader → FileBasedRelationshipModelLoader
• CoreToolingDocumentModelLoader → FileBasedDocumentModelLoader
• CoreToolingModelPermissionEvaluator → NoOpModelPermissionEvaluator
Additionally, NoOpModelPermissionEvaluator grants all permissions unconditionally, while the old
CoreToolingModelPermissionEvaluator threw NotImplementedException.
Migration steps
• Update import statements from CoreTooling* classes to their new File-based counterparts.
• If your code expected NotImplementedException for permission checks, update accordingly —
NoOpModelPermissionEvaluator grants all permissions.
ContentStoreClientProperties.Content.getBaseUrl() renamed to getBasePrefix()
Impact
Any code calling
com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties.Content.getBaseUrl
() will fail to compile. Field renamed from baseUrl to basePrefix to communicate that any string
prefix is valid, not only an HTTP or HTTPS URL.
Migration steps
• Replace all calls to getContent().getBaseUrl() with getContent().getBasePrefix().
// Before
properties.getContent().getBaseUrl()
// After
properties.getContent().getBasePrefix()
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_0066.
ModelBulkImporter class removed
Impact
Any code that directly instantiates or injects
com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImporter will fail to compile. Bulk import of
models from a ZIP/JAR/classpath path via this class is no longer available.
Migration steps
• Remove all direct usages of ModelBulkImporter.doImport.
292

-- 292 of 334 --

• Use RuntimeModelImporter or configure initialization.import.models.path instead.
// Before
modelBulkImporter.doImport(bulkLocation, configuration);
// After
// Use RuntimeModelImporter or configure initialization.import.models.path
DataServicesCoreProperties.Filesystem nested class removed
Impact
Any code accessing mgmtp.a12.dataservices.filesystem.write.enabled or referencing
com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties.Filesystem will fail to
compile. Property was unused since the introduction of content store.
Migration steps
• Remove any reference to DataServicesCoreProperties.Filesystem.
• Remove mgmtp.a12.dataservices.filesystem.write.enabled from application configuration.
// Before
properties.getFilesystem().getWrite().isEnabled()
// After
// Remove — filesystem write switch no longer exists.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_SNAPSHOT_0008.
DataServicesCoreProperties.Search nested class removed
Impact
Any code accessing
com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties.getSearch() or its nested
types will fail to compile. Class was not used anywhere.
Migration steps
• Remove any reference to DataServicesCoreProperties.Search and its sub-classes.
// Before
properties.getSearch().getPaging().getLinks().getOffsetLimit()
// After
// Remove — Search.Paging.LinkPaging no longer exists.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_SNAPSHOT_0009.
293

-- 293 of 334 --

Bulk import exception keys removed from ExceptionKeys and ExceptionCodes
Impact
The following constants have been removed and any code referencing them will fail to compile:
• com.mgmtp.a12.dataservices.exception.ExceptionKeys.MODEL_BULK_IMPORT_GENERIC_ERROR_KEY
• com.mgmtp.a12.dataservices.exception.ExceptionKeys.MODEL_BULK_IMPORT_HEADER_PARSING_ERROR_K
EY
• com.mgmtp.a12.dataservices.exception.ExceptionCodes.MODEL_BULK_IMPORT
Migration steps
• Remove all references to ExceptionKeys.MODEL_BULK_IMPORT_GENERIC_ERROR_KEY,
ExceptionKeys.MODEL_BULK_IMPORT_HEADER_PARSING_ERROR_KEY, and
ExceptionCodes.MODEL_BULK_IMPORT.
• The model import no longer uses bulk-specific error codes; a standard InvalidInputException is
thrown on failure.
dataservices-core-metadata module removed — metadata classes provided by RMC
Impact
The dataservices-core-metadata module has been removed. Its functionality is now provided by the
com.mgmtp.a12.rmc:conversion (RMC) library, which was already a transitive dependency.
• DocumentModelMetadataInjectorFactory is now provided by
com.mgmtp.a12.rmc.metadata.DocumentModelMetadataInjectorFactory. The constructor has been
simplified from 3 parameters to 2 (the IDocumentModelService parameter was removed).
• DocumentModelMetadataInjector is now provided by
com.mgmtp.a12.rmc.metadata.DocumentModelMetadataInjector. The getDocumentModelWithMetadata
method now takes 1 parameter instead of 2 (the attachment model parameter was removed).
• MetadataConstants is now provided by com.mgmtp.a12.rmc.metadata.MetadataConstants. Data
Services-specific field path constants are in the new interface
com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.
Migration steps
• Replace import
com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjectorFactory with
com.mgmtp.a12.rmc.metadata.DocumentModelMetadataInjectorFactory.
• Replace import com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjector with
com.mgmtp.a12.rmc.metadata.DocumentModelMetadataInjector.
• Update factory constructor calls from 3 parameters to 2 (remove IDocumentModelService).
• Update getDocumentModelWithMetadata calls from 2 parameters to 1 (remove the attachment
model parameter).
• Replace com.mgmtp.a12.dataservices.model.metadata.MetadataConstants with
com.mgmtp.a12.rmc.metadata.MetadataConstants for the shared constants
DOCUMENT_META_DATA_MODEL_NAME, CDM_QUERY_ROOT_ANNOTATION, and CDM_QUERY_ROOT_ANNOTATION_NAME.
294

-- 294 of 334 --

BulkImporterConfiguration removed from the public API
Impact
com.mgmtp.a12.dataservices.model.bulkload.BulkImporterConfiguration has been removed from the
public API. Any code referencing it will fail to compile. Model import configuration is now handled
internally by the framework.
Migration steps
• Replace any usage of
com.mgmtp.a12.dataservices.model.client.ModelsClient.importModelBulk(InputStream) with
com.mgmtp.a12.dataservices.model.client.ModelsClient.importRuntimeModels(InputStream). The
API behavior is unchanged.
• Remove any references to
com.mgmtp.a12.dataservices.model.bulkload.BulkImporterConfiguration from your code, as it is
no longer available.
TypeScript API
Removed element Migration
@com.mgmtp.a12.dataservices/dataservi
ces-access#CandidateConstraints
Remove all references to CandidateConstraints and
PopulationParameters. Interface removed along with the
candidateConstraints field from EntityCharacteristics.
@com.mgmtp.a12.dataservices/dataservi
ces-access#EntityCharacteristics
Remove any code accessing
EntityCharacteristics.navigable or
EntityCharacteristics.candidateConstraints. Fields were
not used by DataServices and removed as part of
relationship model simplification.
@com.mgmtp.a12.dataservices/dataservi
ces-access#DocumentSpec.isInstance
Update call sites as needed. DocumentSpec, ErrorPayload,
ErrorResponse, ModelsResult, LinkWithDocument, and
Candidate type guards are now defined as
isInstance(value: unknown). In most cases no change is
required since unknown is a wider type.
@com.mgmtp.a12.dataservices/dataservi
ces-access
Replace all deep import paths with root-level imports from
@com.mgmtp.a12.dataservices/dataservices-access. Deep
import paths from
@com.mgmtp.a12.dataservices/dataservices-access and
@com.mgmtp.a12.dataservices/dataservices-relationship-
model-migration have been removed.
Query.Order interface renamed to Query.DirectFieldOrder; Order is now a union type
Impact
TypeScript code importing or using Query.Order as a concrete interface will break. The fields
ignoreCase and nullHandling became optional. Order is now the union type DirectFieldOrder |
RelationshipOrder.
Migration steps
295

-- 295 of 334 --

• Replace Query.Order with Query.DirectFieldOrder for direct field sort specifications.
• Update any code that relies on ignoreCase or nullHandling being required fields.
// Before
const order: Query.Order = { direction: Query.Direction.ASC, field: "name",
ignoreCase: true, nullHandling: Query.NullHandling.NATIVE };
// After
const order: Query.DirectFieldOrder = { direction: Query.Direction.ASC, field: "name",
ignoreCase: true, nullHandling: Query.NullHandling.NATIVE };
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_SNAPSHOT_0012.
Query.ExactMatchOperator.lang property removed
Impact
TypeScript code setting Query.ExactMatchOperator.lang will receive a compile error (unknown
property). Property was deprecated since 38.1.0 and had no effect on query execution.
Migration steps
• Remove any usages of lang from ExactMatchOperator objects.
// Before
{ field: "name", value: "Foo", lang: "en" }
// After
{ field: "name", value: "Foo" }
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_SNAPSHOT_0013.
RelationshipModel.Content: associationType, storage, embeddedGroupPath fields removed
Impact
TypeScript code accessing content.associationType, content.storage, or content.embeddedGroupPath
will receive compile errors. The AssociationType and Storage enums are also removed. Fields were
unused in the DataServices relationship model and removed as part of model cleanup.
Migration steps
• Remove all references to Content.associationType, Content.storage, and
Content.embeddedGroupPath.
• Remove all references to the AssociationType and Storage enums.
// Before
if (content.associationType === AssociationType.OWNED) { ... }
296

-- 296 of 334 --

// After
// associationType is no longer available; remove the condition.
AssociationType enum removed from RelationshipModel
Impact
TypeScript code importing or using AssociationType will fail to compile. Enum was removed as part
of relationship model cleanup.
Migration steps
• Remove all imports and usages of AssociationType.
// Before
import { AssociationType } from '@mgmtp/dataservices-access';
// After
// Remove the import entirely.
Storage enum removed from RelationshipModel
Impact
TypeScript code importing or using Storage will fail to compile. Enum was removed as part of
relationship model cleanup.
Migration steps
• Remove all imports and usages of Storage enum.
// Before
import { Storage } from '@mgmtp/dataservices-access';
// After
// Remove the import entirely.
Multiplicity.lowerLimit field removed
Impact
TypeScript code accessing multiplicity.lowerLimit will fail to compile. lowerLimit was always 0 and
not used. Removed as part of relationship model simplification.
Migration steps
• Remove all references to Multiplicity.lowerLimit. The lower limit is always 0 implicitly.
// Before
const lower = multiplicity.lowerLimit;
// After
297

-- 297 of 334 --

const lower = 0; // lowerLimit is always 0, removed from model
AggregationTuple type changed and LinkEntitySpec.docRef made mandatory
Impact
AggregationTuple type changed to ( string | number)[]. Update any code that depended on the
previous tuple structure. LinkEntitySpec.docRef is now mandatory. Ensure all LinkEntitySpec usages
provide a docRef value.
Migration steps
• Update any code that depended on the previous AggregationTuple structure.
• Ensure all LinkEntitySpec usages provide a docRef value.
Redux peer dependency upgraded to ^5 in dataservices-access
Impact
Projects that depend on Redux 4 will have a peer dependency conflict. For a complete list of
breaking changes in Redux 5, refer to the official Redux 5 release notes.
Migration steps
• Upgrade Redux in your project to ^5.
• Review the Redux 5 release notes for any additional migration steps required by your
application.
TypeScript 6.0.2 required and ES2025 output target
Impact
Projects using Data Services TypeScript packages must upgrade TypeScript to 6.0.2 or later. The
compiled packages now emit ES2025 syntax. If your project must support older browsers, ensure
your bundler is configured to transpile the output or provide the necessary polyfills.
Migration steps
• Upgrade TypeScript in your project: npm install typescript@^6.0.2 --save-dev
• If your bundler targets environments without ES2025 support, configure appropriate
transpilation or add polyfills.
TypeScript JsonRpc2Request interfaces removed — PageSpec, QueryPageSpec, FilterSpec, SortSpec
Impact
The following interfaces inside the JsonRpc2Request namespace have been removed:
• JsonRpc2Request.PageSpec
• JsonRpc2Request.QueryPageSpec
• JsonRpc2Request.FilterSpec
• JsonRpc2Request.SortSpec
298

-- 298 of 334 --

These were only used in the Solr-based search which is replaced by the Query API. Any code
referencing these interfaces will fail to compile.
Migration steps
• Remove any usages of these interfaces.
• Use the Query API request types from @com.mgmtp.a12.dataservices/dataservices-access instead.
Configuration
application-contentstore-http1-only.properties renamed to application-contentstore-http1_only.properties
Impact
Applications that activate the contentstore-http1-only Spring profile by name will not find the
corresponding properties file after upgrade. File renamed to fix the Spring profile naming.
Migration steps
• Update any Spring profile activations or references from contentstore-http1-only to
contentstore-http1_only.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_0043.
Default value removed for mgmtp.a12.uaa.authentication.client.rest.authentication-configuration.relative-
login.url
Impact
Applications that relied on the implicit default user/local/login will fail to authenticate after
upgrade if the property is not declared. The property has been removed from dataservices-client-
default.properties; consumers must now declare it explicitly.
Migration steps
• Add mgmtp.a12.uaa.authentication.client.rest.authentication-configuration.relative-
login.url=user/local/login to your application properties if it is not already present.
mgmtp.a12.dataservices.contentstore.client.content.base-url renamed to base-prefix
Impact
Applications that set mgmtp.a12.dataservices.contentstore.client.content.base-url will silently
ignore the value after upgrade. Key renamed to communicate that any string prefix is valid, not
only an HTTP or HTTPS URL.
Migration steps
• Rename mgmtp.a12.dataservices.contentstore.client.content.base-url to
mgmtp.a12.dataservices.contentstore.client.content.base-prefix in all property files and
environment variables.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_0045.
299

-- 299 of 334 --

mgmtp.a12.dataservices.query.exact-match.enumeration-value-match.enabled removed
Impact
The property has no effect after upgrade. If it is set to false (non-default), existing queries using
exact_match on enumeration fields with localized display text values will no longer return results.
Migration steps
• Remove mgmtp.a12.dataservices.query.exact-match.enumeration-value-match.enabled from all
property files.
• Replace any exact_match values for enumeration fields with the corresponding enumeration key
instead of the localized display text.
Hazelcast cache configuration properties removed from no_cache profile
Impact
Applications relying on the no_cache profile to override Hazelcast cache settings may need to update
their configuration if they override these properties elsewhere. Hazelcast-specific properties
removed as DataServices migrated from Hazelcast to Infinispan for caching.
Migration steps
• Remove spring.datasources.dataservices.jpa.properties.hibernate.cache.region.factory_class
and spring.datasources.dataservices.jpa.properties.hibernate.cache.hazelcast.instance_name
from application configuration.
• Use spring.cache.type=none or the dataservices-no_cache profile to disable caching.
mgmtp.a12.dataservices.filesystem.write.enabled removed
Impact
Applications that set mgmtp.a12.dataservices.filesystem.write.enabled will receive an unknown-
property warning (or error if fail-on-unknown-properties is enabled) after upgrade. Property was
unused since the introduction of content store.
Migration steps
• Remove mgmtp.a12.dataservices.filesystem.write.enabled from all application properties and
environment variable mappings.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_SNAPSHOT_0020.
mgmtp.a12.dataservices.search.paging.links.* properties removed
Impact
Applications that set mgmtp.a12.dataservices.search.paging.links.offset-limit or
mgmtp.a12.dataservices.search.paging.links.page-limit will receive an unknown-property warning
(or error if fail-on-unknown-properties is enabled) after upgrade. Properties removed together with
DataServicesCoreProperties.Search.
Migration steps
• Remove mgmtp.a12.dataservices.search.paging.links.offset-limit from all application
300

-- 300 of 334 --

properties.
• Remove mgmtp.a12.dataservices.search.paging.links.page-limit from all application properties.
Reduced Default Schedule for Database Cleanup Jobs
Impact
Projects that reference
com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties.CLEANUP_JOB_DEFAULT_SCHEDUL
E directly will fail to compile. Default schedules for cleanUpDirtyAttachmentsTrigger,
cleanUpStaleAttachments, cleanupRequestId, and rankRecalculation have changed from every 5
minutes to staggered daily schedules.
Migration steps
• Remove any direct references to DataServicesCoreProperties.CLEANUP_JOB_DEFAULT_SCHEDULE.
• Review the new default schedules for each cleanup job and override via configuration if the
defaults do not suit your deployment.
• If you set mgmtp.a12.dataservices.jobs.relationships.rankRecalculation.enabled=true without
an explicit schedule, the job will now execute every Sunday at 01:45.
Embedded Postgres initialization now sets LC_COLLATE explicitly
Impact
Previously, embedded Postgres initdb set only LC_CTYPE, and LC_COLLATE defaulted to the platform’s
locale. Starting with PostgreSQL 18, collations with different collate and ctype values are rejected
on Windows, causing a fatal startup error. Embedded Postgres initialization now sets both LC_CTYPE
and LC_COLLATE explicitly so that they always match.
This affects both Data Services (spring.datasources.dataservices.embedded-postgres.locale-collate)
and Content Store (spring.datasources.contentstore.embedded-postgres.locale-collate). Existing
embedded databases (persistent data directory already initialized) are not affected, because initdb
is not re-run for existing clusters. New embedded databases (first startup, or clean-data-
directory=true) now have both LC_CTYPE and LC_COLLATE set to en_US.UTF-8 by default. External (non-
embedded) PostgreSQL databases are not affected.
Migration steps
• If you previously customized locale-c-type to a value other than en_US.UTF-8, also set locale-
collate to the same value so they match (for example locale-c-type=German_Germany.1252
together with locale-collate=German_Germany.1252).
• If you are using the default locale-c-type (en_US.UTF-8), no action is required.
Database
localized_fields table dropped during upgrade
Impact
The localized_fields database table and its associated trigger (createFulltextTrigger) are dropped
automatically during the upgrade via a Liquibase changeset. No data migration is required. This
301

-- 301 of 334 --

table was part of an experimental localized fulltext search feature that was abandoned before
release and had no external consumers.
Migration steps
• No code changes are required. The table will be dropped automatically on first startup after
upgrade.
Build
dataservices-core-tooling module renamed to dataservices-modelgraph-fs-impl
Impact
Consumers that reference the fat-jar artifact or declare a direct Gradle or Maven dependency on
com.mgmtp.a12.dataservices:dataservices-core-tooling must update their artifact reference.
Migration steps
• Replace com.mgmtp.a12.dataservices:dataservices-core-tooling with
com.mgmtp.a12.dataservices:dataservices-modelgraph-fs-impl in all build files.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_0048.
Content Store artifact group renamed to com.mgmtp.a12.dataservices.contentstore
Impact
Builds that declare dependencies on Content Store artifacts under com.mgmtp.a12.dataservices will
fail to resolve them after upgrade. Group name changed for all Content Store artifacts.
Migration steps
• Update your build configuration to use the new group name
com.mgmtp.a12.dataservices.contentstore for all Content Store artifacts.
• Replace com.mgmtp.a12.dataservices:dataservices-content-store-core-spring-boot-
autoconfigure with com.mgmtp.a12.dataservices.contentstore:dataservices-content-store-core-
spring-boot-autoconfigure.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_0049.
dataservices-data-seed module renamed to dataservices-sme-workspace-support
Impact
Code depending on the com.mgmtp.a12.dataservices:dataservices-data-seed module must update
their Gradle/Maven dependency. REST endpoint URLs have changed. TypeScript namespaces have
been renamed.
Migration steps
• Replace dataservices-data-seed with dataservices-sme-workspace-support in all build files.
• Update REST endpoints: POST/GET/DELETE {ctx}/internal/seed-data → {ctx}/sme/workspace/import,
{ctx}/sme/workspace/export, {ctx}/sme/workspace/clearDatabase.
302

-- 302 of 334 --

• Update TypeScript namespaces: SeedDataImport → SmeWorkspaceImport, SeedDataExport →
SmeWorkspaceExport, SeedDataDelete → DatabaseClear.
• Update TypeScript import paths from SeedData/seedData to Workspace/workspace.
 Automated by com.mgmtp.a12.dataservices.rewrite.bc.bc_39_0_0_rc_2_0050.
Fat JAR artifacts are no longer published to Maven
Impact
Fat JAR artifacts (classifier fatjar) are no longer published to the Maven repository. Consumers that
download fat JAR artifacts (e.g. dataservices-server-app-VERSION-fatjar.jar) from the Maven
repository will no longer find them there.
Migration steps
• Build the fat JAR from source using ./gradlew bootJar.
Vanilla Docker images no longer published to community registry
Data Services no longer publishes production-ready Docker images to the public community Docker
registry. The following images are no longer available from the community registry:
• com.mgmtp.a12.dataservices/dataservices-server-app
• com.mgmtp.a12.dataservices/dataservices-server-init-app
• com.mgmtp.a12.dataservices.contentstore/dataservices-content-store-server-app
Projects must build their own Docker images based on the Spring Boot JAR artifact and the A12
project template. The Helm charts are still published and maintained for deployment, but they now
expect project-built images.
Impact
Projects that directly pulled and deployed the vanilla Docker images from the community Docker
registry must create their own Dockerfiles and CI/CD image builds.
Migration steps
• Create a project-specific Dockerfile based on the A12 project template.
• Build and publish your Docker image to your project’s container registry.
• Update Helm values to point to your project’s image repository and tag.
Behavior Changes
ignoreCase on non-string fields is consistently ignored across direct and relationship sorting
Impact
Queries that previously failed with "ignoreCase is only supported for string fields…" when
sorting a non-string field through a relationship will now succeed and produce the same ordering
as if ignoreCase were not set. This aligns the relationship sort behavior with direct field sorting.
303

-- 303 of 334 --

Migration steps
• No code changes are required. Queries that previously failed with an ignoreCase validation
error for relationship-based non-string sorting will now succeed.
SME workspace import now preserves exported link ordering
Impact
Imported relationship links now have the same ordering as they had in the source workspace.
Applications that relied on the previous behavior of re-creating links in undefined order may
observe different ordering after import. The target workspace must be empty before running an
import.
Migration steps
• No code changes are required. The target workspace must be empty before running an import.
JavaScript attachment MIME type changed to text/javascript
Impact
Applications that check for application/javascript when processing uploaded JavaScript
attachments must be updated to accept text/javascript. Due to the Apache Tika version upgrade,
the MIME type for JavaScript files has changed from application/javascript to text/javascript,
aligning with RFC 9239.
Migration steps
• Update any application logic that checks for application/javascript to accept text/javascript.
Changed error description keys in RPC error responses
Impact
Client applications that parse or compare the description.key field in JSON-RPC error responses will
receive different key values. Applications using keys for i18n lookup will need to provide
translations for the new keys. The title.key remains unchanged as rpc.operation.error.
Migration steps
• Update any client-side logic that matches on description.key == "rpc.operation.error" or
description.key == null to check for the specific new keys.
• Keys changed from rpc.operation.error: AccessDeniedException → error.security.notAuthorized;
generic Throwable/NullPointerException → error.input.invalid.
• Keys changed from null: QueryException → error.input.query.invalid; date/time parsing →
error.time.format; document not found → error.document.notFound; model repository not found
→ error.model.repository.notFound; model deserialization → error.model.deserialization;
model not found → error.model.notFound; model import → error.model.import.generic;
relationship link not found → error.relationship.link.notFound; resource resolution →
error.resource.resolution; configuration conflicts → error.configuration.invalid; other invalid
input → error.input.invalid.
• Add localization entries for the new keys if your application provides custom translations.
304

-- 304 of 334 --

exact_match on enumeration fields now always matches on the enumeration key
Impact
Any stored query or Filter Set that used exact_match on an enumeration field with a localized
display text as the value will no longer return results. The value must be the enumeration key.
Migration steps
• Replace any exact_match values for enumeration fields with the corresponding enumeration key
(e.g. replace "Travel" with "TRAVEL").
exact_match operator — value property changed from required to optional
Impact
Java client code calling exactMatchOperator.getValue() may now return null. Any code that calls
getValue() without a null check may throw a NullPointerException at runtime.
Migration steps
• Review and update call sites for exactMatchOperator.getValue() to check for null before using
the returned value.
QueryValidator now handles null targetDocumentModel for QueryLink and has operator
Impact
No code changes are required. This fix may change the set of queries that pass validation in
applications where targetDocumentModel was intentionally omitted for these operators.
Migration steps
• No code changes are required. Review any validation logic that assumed targetDocumentModel
must be set for QueryLink and has operator scenarios.
Seed import no longer preserves exported link ordering
Impact
Imported relationship links may have a different role ordering than they had in the source system.
Applications that rely on a specific initial ordering of relationship links after a seed import should
verify the ordering after import.
Migration steps
• No code changes are required. If the exact ordering matters, manually reorder the links after
import using the relationship reorder API.
DataServicesInitializationService no longer configures CustomTypes and Conditions
Impact
com.mgmtp.a12.dataservices.DataServicesInitializationService is no longer responsible for
configuring CustomTypes and Conditions. These are now resolved at runtime with every validation
and computation request. No code changes are required unless your application relied on
initialization-time configuration of CustomTypes or Conditions.
305

-- 305 of 334 --

Migration steps
• No code changes are required unless your application relied on initialization-time configuration
of CustomTypes or Conditions.
MODEL_CREATE permission check now precedes model content validation on create
Impact
A caller who lacks the MODEL_CREATE permission and also supplies invalid model content will now
receive a permission-denied error (HTTP 403) instead of a validation error (HTTP 400). Callers with
sufficient permissions are unaffected.
Migration steps
• No code changes are required. Review any error-handling or test logic that asserts a specific
error type (validation vs. authorization) when an unauthorized caller submits malformed
model content.
Field paths without a leading slash now throw an exception
Impact
Supplying a field path without a leading forward slash (for example
ContractRoot/ChangeLog/Changes/Status) in query operators, projection fields, or link fields now
throws a QueryInvalidInputException (HTTP 400). Field paths must begin with a leading forward
slash (for example /ContractRoot/ChangeLog/Changes/Status).
This applies to: ExactMatchOperator, SimpleSearchOperator, aggregation fields, projection fields on a
QueryRoot, fields and linkDocumentFields on a QueryLink, and field on direct and relationship sort
orders.
Migration steps
• Update all field paths that do not start with / to include the leading slash. This includes
constraint operator fields, projection field lists, and link document field lists.
String field sorting with NULLS_LAST/NULLS_FIRST treats explicit null and absent fields identically
Impact
When sorting on a string field with a NULLS_LAST or NULLS_FIRST null-handling strategy, a document
whose field is explicitly set to null was previously ordered differently from a document where the
field is absent. This was most visible for case-insensitive sorts (ignoreCase=true), where an explicit
JSON null was treated as a non-null value and sorted among real string values rather than with the
nulls.
Both cases are now mapped to SQL NULL, so documents with an explicit null value and documents
where the field is absent sort in the same position (together in the nulls group). Applications that
sort on string fields with NULLS_LAST or NULLS_FIRST may observe a change in result ordering for
documents that carry an explicit null value.
Migration steps
• No code changes are required. Documents with an explicit null value now sort together with
documents that have no value for that field, rather than appearing among the non-null values.
306

-- 306 of 334 --

PARTIAL_MODIFY_DOCUMENT input validation relaxed for group parts
Impact
The input validation of PARTIAL_MODIFY_DOCUMENT no longer rejects a documentPart that addresses a
group, nor a repetitions array ending in the wildcard index 0. Such requests previously failed
validation and now succeed: with a concrete repetitions array the group is replaced (if it exists) or
inserted (if it does not); when the repetitions array ends with the wildcard index 0, the supplied
group is appended as a new repetition of the addressed repeatable group (or becomes the first
entry when none exist yet). Field modification and null-value removal are unchanged.
Migration steps
• No code changes are required.
MISC
Changed error message for aggregation queries without a target document model
Impact
Client applications parsing validation error messages may need adjustment if they rely on the exact
error message text. The error message has changed from "Aggregations must have a target
document model specified" to "Target document model is missing".
Migration steps
• Update any client code that checks for the exact error message text for aggregation queries
without a target document model.
Changed error message for malformed HTTP request bodies
Impact
Client applications parsing this specific error message string may need adjustment. The error
message has changed from "The selected file is empty. Please select a different file." to "The
HTTP message could not be processed. Possibly the request body is empty or malformed.".
Migration steps
• Update any client code that checks for the exact error message text for malformed HTTP request
bodies.
Additional Migration Guides
Helm Chart Refactoring — Application-Centric Configuration
The Helm charts for Data Services and Content Store have been refactored to follow an
application-centric configuration approach. This change reduces duplication between Helm
values and Spring profile defaults, resulting in simpler chart maintenance and configuration.
Key Principles
• Environment-specific values remain in Helm (URLs with namespaces, database connections,
mount paths)
307

-- 307 of 334 --

• Application defaults are defined in Spring profiles (timeout values, buffer sizes, feature flags)
• Client overrides use extraEnvVars for direct Spring property access
Helm Chart Versioning Changes
 Audience: This section is for users and operators deploying Data Services, not for
developers.
Unified Version Strategy
Starting from Data Services 39.0.0, Helm chart version and application version are unified -
both use the same version number. This simplifies versioning and ensures consistency between
deployed applications and their Helm charts.
Version Comparison
Data Services Version Helm Chart Version Notes
≤ 38.x ≤ 7.2.x Separate versioning (deprecated)
≥ 39.0.0 ≥ 39.0.0 Unified versioning (current)
Previous Approach (Data Services ≤ 38.x)
Helm chart version was separate from application version:
# Chart.yaml in old Helm chart
appVersion: "38.0.0"
version: "7.0.0" # independent chart version
Data Services Helm Chart Changes
Content Store Configuration
Removed Variables
The following Content Store variables have been removed from values.yaml:
Removed Helm Variable Environment Variable Default Value
contentStore.contextPath MGMTP_A12_DATASERVICES_CONTENTSTORE_SER
VER_CONTEXTPATH
/cs
contentStore.contentStorag
e
MGMTP_A12_DATASERVICES_CONTENTSTORE_STO
RAGE_CONTENTSTORAGE
FS
contentStore.limitSize MGMTP_A12_DATASERVICES_CONTENTSTORE_LIM
ITSIZE
10 MiB
contentStore.ticketDuratio
n
MGMTP_A12_DATASERVICES_CONTENTSTORE_TIC
KETDURATION
5 min
308

-- 308 of 334 --

Removed Helm Variable Environment Variable Default Value
contentStore.ticketMultiDo
wnloadEnabled
MGMTP_A12_DATASERVICES_CONTENTSTORE_TIC
KETMULTIDOWNLOAD_ENABLED
false
Migration Example
# Before (Old values.yaml)
contentStore:
contextPath: /custom-cs
contentStorage: DB
limitSize: 50 MiB
ticketDuration: 10 min
ticketMultiDownloadEnabled: true
# After (New values.yaml)
extraEnvVars:
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_SERVER_CONTEXTPATH
value: "/custom-cs"
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_STORAGE_CONTENTSTORAGE
value: "DB"
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_LIMITSIZE
value: "50 MiB"
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_TICKETDURATION
value: "10 min"
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_TICKETMULTIDOWNLOAD_ENABLED
value: "true"
Kept Variables
These variables remain as they are environment-specific:
Helm Variable Purpose
contentStore.mode Mode: EMBEDDED or STANDALONE
contentStore.baseUrl Public URL for content download (contains
namespace/cluster)
contentStore.maintenanceUrl URL for content download during maintenance
contentStore.postgresql.enabled Enable PostgreSQL database
contentStore.postgresql.connectionURI PostgreSQL connection URI (environment-specific)
contentStore.postgresql.username PostgreSQL username
contentStore.postgresql.secret.* Secret configuration
contentStore.standalone.clientConfigu
rationRemoteUrl
URL of standalone Content Store service
309

-- 309 of 334 --

Actuator Configuration
Removed Variables
Removed Helm Variable Environment Variable Default Value
actuator.enabled N/A true (always enabled)
actuator.basePath MANAGEMENT_ENDPOINTS_WEB_BASEPATH /actuator
actuator.probeEndpointsEna
bled
N/A true (always enabled)
actuator.prometheusEndpoin
tAccess
MANAGEMENT_ENDPOINT_PROMETHEUS_ACCESS Auto-configured based on
metrics.enabled
actuator.exposeEndpoints MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLU
DE
Auto-configured based on
metrics.enabled
Actuator Base Path via extraEnvVars
The actuator.basePath has been removed. The actuator base path is now configured directly via
extraEnvVars using the Spring Boot environment variable MANAGEMENT_ENDPOINTS_WEB_BASEPATH. The
default value is /actuator when not set.

The base path set in extraEnvVars is automatically read by the Helm chart at render
time and applied consistently to:
• Kubernetes liveness/readiness probes
• Management ingress paths
• Prometheus ServiceMonitor scrape paths
Migration Example
# Before (Old values.yaml)
actuator:
enabled: true
basePath: /custom-actuator
probeEndpointsEnabled: true
prometheusEndpointAccess: read_only
exposeEndpoints: health,info,prometheus
# After (New values.yaml) - custom base path
extraEnvVars:
- name: MANAGEMENT_ENDPOINTS_WEB_BASEPATH
value: "/custom-actuator"
# Enable metrics to auto-configure prometheus endpoint
metrics:
enabled: true
# Other actuator properties via extraEnvVars
310

-- 310 of 334 --

extraEnvVars:
- name: MANAGEMENT_ENDPOINT_INFO_ACCESS
value: "read_only"
Automatic Prometheus Configuration
When metrics.enabled=true, the Prometheus actuator endpoint is automatically configured:
• MANAGEMENT_ENDPOINT_PROMETHEUS_ACCESS is set to read_only
• MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE includes prometheus
Configurable Properties
These properties can be overridden via extraEnvVars:
Property Environment Variable Default Purpose
management.endpoints.acces
s.default
MANAGEMENT_ENDPOINTS_ACCES
S_DEFAULT
none Default access level for all
endpoints
management.endpoint.health
.access
MANAGEMENT_ENDPOINT_HEALTH
_ACCESS
read_only Health endpoint access
management.endpoint.health
.show-details
MANAGEMENT_ENDPOINT_HEALTH
_SHOW_DETAILS
never Show health details
management.endpoint.info.a
ccess
MANAGEMENT_ENDPOINT_INFO_A
CCESS
read_only Info endpoint access
management.health.defaults
.enabled
MANAGEMENT_HEALTH_DEFAULTS
_ENABLED
false Enable default health
indicators
OAuth2/OIDC Configuration
Removed Variables
Removed Helm Variable Environment Variable Default Value
uaa.authentication.oidc.re
almName
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_REALMNA
ME
A12Realm
uaa.authentication.oidc.cl
ientId
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_CLIENTI
D
a12-spa-client
uaa.authentication.oidc.jw
kSetUri
SPRING_SECURITY_OAUTH2_RESOURCESERVER_J
WT_JWKSETURI
-
uaa.authentication.oidc.jw
tPublicKeyLocation
SPRING_SECURITY_OAUTH2_RESOURCESERVER_J
WT_PUBLICKEYLOCATION
-
uaa.authentication.oidc.jw
sAlgorithms
SPRING_SECURITY_OAUTH2_RESOURCESERVER_J
WT_JWSALGORITHMS
RS256
uaa.authentication.oidc.lo
ginRelativeUrl
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_LOGINRE
LATIVE_URL
-
311

-- 311 of 334 --

Removed Helm Variable Environment Variable Default Value
uaa.authentication.oidc.lo
ginRedirectRelativeUrl
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_LOGINRE
DIRECTRELATIVE_URL
-
uaa.authentication.oidc.lo
goutRedirectRelativeUrl
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_LOGOUTR
EDIRECTRELATIVE_URL
-
uaa.authentication.oidc.si
lentRedirectRelativeUrl
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_SILENTR
EDIRECTRELATIVE_URL
-
uaa.authentication.oidc.to
kenExchangeRelativeUrl
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_TOKENEX
CHANGERELATIVE_URL
-
uaa.authentication.oidc.us
ernameXpath
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_SSOCONF
IGURATION_USERNAMEXPATH
-
uaa.authentication.oidc.pa
sswordXpath
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_SSOCONF
IGURATION_PASSWORDXPATH
-
uaa.authentication.oidc.lo
ginButtonXpath
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELF
CONFIGURATION_OIDC_PUBLICCLIENT_SSOCONF
IGURATION_LOGINBUTTONXPATH
-
Migration Example
# Before (Old values.yaml)
uaa:
authentication:
oidc:
realmName: MyRealm
clientId: my-client
jwsAlgorithms: RS512
loginRelativeUrl: "/login"
# After (New values.yaml)
extraEnvVars:
- name:
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELFCONFIGURATION_OIDC_PUBLICCLIENT_REALMNAME
value: "MyRealm"
- name:
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELFCONFIGURATION_OIDC_PUBLICCLIENT_CLIENTID
value: "my-client"
- name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWSALGORITHMS
value: "RS512"
- name:
MGMTP_A12_UAA_AUTHENTICATION_CLIENTSELFCONFIGURATION_OIDC_PUBLICCLIENT_LOGINRELATIVE_U
RL
value: "/login"
312

-- 312 of 334 --

Kept Variables
Helm Variable Purpose
uaa.authentication.oidc.idpBaseUrl IDP base URL (contains {{ .Release.Namespace }})
uaa.authentication.oidc.issuerUri OpenID Connect discovery endpoint (contains {{
.Release.Namespace }})
Removed Variables
Removed Helm Variable Replacement Notes
uaa.authentication.oidc.en
abled
Remove — no longer needed OIDC config is now always
rendered when
uaa.authentication.types
contains OAUTH2
UAA General Configuration
Removed Variables
Removed Helm Variable Environment Variable Default Value
uaa.authentication.unsecur
edUrls
MGMTP_A12_UAA_AUTHENTICATION_UNSECURED_
URLS
-
uaa.authentication.context
Path
MGMTP_A12_UAA_AUTHENTICATION_CONTEXTPAT
H
/api
uaa.restClient.authenticat
ionType
MGMTP_A12_UAA_AUTHENTICATION_CLIENT_RES
T_AUTHENTICATIONTYPE
DELEGATED
uaa.restClient.delegatedMo
deConfiguration.excludedCo
ntexts
MGMTP_A12_UAA_AUTHENTICATION_CLIENT_RES
T_DELEGATEDMODECONFIGURATION_EXCLUDEDCO
NTEXTS
-

The Helm chart automatically sets both
MGMTP_A12_UAA_AUTHENTICATION_UNSECURED_URLS and
MGMTP_A12_UAA_AUTHENTICATION_CLIENT_REST_DELEGATEDMODECONFIGURATION_EXCLUDEDCON
TEXTS to /actuator/** in the config-map-actuator ConfigMap. This ensures:
• Kubernetes liveness/readiness probes can access health endpoints without
authentication
• The delegated authentication filter (REST client) also bypasses actuator
endpoints
If you add custom unsecured URLs via extraEnvVars, make sure to include
/actuator/** in both properties:
extraEnvVars:
- name: MGMTP_A12_UAA_AUTHENTICATION_UNSECURED_URLS
value: "/actuator/**,/public/**"
- name:
313

-- 313 of 334 --

MGMTP_A12_UAA_AUTHENTICATION_CLIENT_REST_DELEGATEDMODECONFIGURATION_EXC
LUDEDCONTEXTS
value: "/actuator/**,/public/**"
Migration Example
# Before (Old values.yaml)
uaa:
authentication:
unsecuredUrls: "/public/**"
contextPath: "/api/v2"
restClient:
authenticationType: CLIENT_CREDENTIALS
delegatedModeConfiguration:
excludedContexts: "/actuator/**,/public/**"
# After (New values.yaml)
extraEnvVars:
- name: MGMTP_A12_UAA_AUTHENTICATION_UNSECURED_URLS
value: "/public/**"
- name: MGMTP_A12_UAA_AUTHENTICATION_CONTEXTPATH
value: "/api/v2"
- name: MGMTP_A12_UAA_AUTHENTICATION_CLIENT_REST_AUTHENTICATIONTYPE
value: "CLIENT_CREDENTIALS"
- name:
MGMTP_A12_UAA_AUTHENTICATION_CLIENT_REST_DELEGATEDMODECONFIGURATION_EXCLUDEDCONTEXTS
value: "/actuator/**,/public/**"
Kept Variables
Helm Variable Purpose
uaa.authentication.apiKey.authorityRe
sourcesData
Populate with certificate entries to enable API key
authentication
uaa.authentication.oidc.issuerUri OIDC issuer URI (environment-specific, contains
namespace)
uaa.authentication.clientSelfConfigur
ation.applicationBaseUrl
Contains {{ .Release.Namespace }}
uaa.authentication.clientSelfConfigur
ation.uaaBaseUrl
Contains {{ .Release.Namespace }}
uaa.restClient.secret Reference to REST client credentials secret — presence
enables the REST client
Removed Variables
314

-- 314 of 334 --

Removed Helm Variable Replacement Notes
uaa.enabled Remove — no longer needed UAA is always active
uaa.restClient.enabled Set uaa.restClient.secret REST client is enabled by
setting a non-empty
uaa.restClient.secret
uaa.authentication.types Remove — no longer needed OIDC config is always
rendered; API key config is
enabled by populating
uaa.authentication.apiKey.
authorityResourcesData
uaa.authentication.apiKey.
enabled
Populate
uaa.authentication.apiKey.authorityReso
urcesData
API key config (ConfigMap,
env prop, volume) is
rendered when
authorityResourcesData is
non-empty
Kept Variables
Helm Variable Purpose
uaa.jwt.enabled Enable/disable JWT secret injection from Kubernetes
secret. When true, injects
MGMTP_A12_UAA_AUTHENTICATION_JWT_SECRET from the secret
specified by uaa.jwt.secret.name and uaa.jwt.secret.key.
Default: false
uaa.jwt.secret.name Name of the Kubernetes secret containing the JWT secret
(default: {{ .Values.global.secrets.name }}-jwt)
uaa.jwt.secret.key Key in the Kubernetes secret containing the JWT secret
value (default: jwt-secret)
Spring Profiles Configuration
Removed Variables
Removed Helm Variable Environment Variable Default Value
springProfiles SPRING_PROFILES_ACTIVE Auto-detected based on
configuration
init.springProfiles SPRING_PROFILES_ACTIVE dataservices-
initscripts,dataservices-
rpc,dataservices-uaa
Migration Example
# Before (Old values.yaml)
springProfiles: dataservices-rpc,dataservices-uaa
315

-- 315 of 334 --

init:
springProfiles: dataservices-initscripts,dataservices-rpc,dataservices-uaa
# After (New values.yaml)
# Main app: use extraEnvVars for custom profiles
extraEnvVars:
- name: SPRING_PROFILES_ACTIVE
value: "dataservices-rpc,dataservices-uaa"
# Init job: use init.extraEnvVars for custom profiles
init:
extraEnvVars:
- name: SPRING_PROFILES_ACTIVE
value: "dataservices-initscripts,dataservices-rpc,dataservices-uaa"
Note
The application and init job will auto-detect required profiles based on configuration: -
dataservices-external_postgres - when database.enabled=true - dataservices-cluster - when
init.enabled=true - dataservices-embedded_contentstore or dataservices-standalone_contentstore -
based on contentStore.mode
Only set SPRING_PROFILES_ACTIVE via extraEnvVars (or init.extraEnvVars for the init job) if you need
profiles beyond the auto-detected ones.
Data Services REST Client Configuration
Removed Variable
Removed Helm Variable Environment Variable Default Value
restClient.enabled N/A Removed — REST client is
configured via
extraEnvVars only
restClient.baseUrl MGMTP_A12_DATASERVICES_CLIENT_CONFIGURA
TION_BASEURL
"" (must be set explicitly)
Migration Example
# Before (Old values.yaml)
restClient:
enabled: true
baseUrl: "http://my-host/api"
# After (New values.yaml)
extraEnvVars:
- name: MGMTP_A12_DATASERVICES_CLIENT_CONFIGURATION_BASEURL
value: "http://my-host/api"
316

-- 316 of 334 --

Content Store Helm Chart Changes
Content Store Configuration
Removed Variables
Removed Helm Variable Environment Variable Default Value
springProfiles SPRING_PROFILES_ACTIVE contentstore-uaa (auto-
detected)
contextPath MGMTP_A12_DATASERVICES_CONTENTSTORE_SER
VER_CONTEXTPATH
/cs
contentStorage MGMTP_A12_DATASERVICES_CONTENTSTORE_STO
RAGE_CONTENTSTORAGE
FS
limitSize MGMTP_A12_DATASERVICES_CONTENTSTORE_LIM
ITSIZE
10 MiB
ticketDuration MGMTP_A12_DATASERVICES_CONTENTSTORE_TIC
KETDURATION
5 min
ticketMultiDownloadEnabled MGMTP_A12_DATASERVICES_CONTENTSTORE_TIC
KETMULTIDOWNLOAD_ENABLED
false

Ingress path: The ingress path for public download is now hardcoded to
/cs/download in the Helm chart. This matches the default Spring application
context path (/cs). To change the application context path, use extraEnvVars to
override MGMTP_A12_DATASERVICES_CONTENTSTORE_SERVER_CONTEXTPATH.
Spring profiles: The application auto-detects required profiles. Only use
extraEnvVars to set SPRING_PROFILES_ACTIVE if you need additional custom profiles.
Migration Example
# Before (Old values.yaml)
springProfiles: contentstore-uaa,custom-profile
contextPath: /cs
contentStorage: DB
limitSize: 50 MiB
ticketDuration: 10 min
ticketMultiDownloadEnabled: true
# After (New values.yaml)
# springProfiles is removed - profiles are auto-detected
# contextPath is removed - ingress path is now hardcoded to /cs/download
# To customize, use extraEnvVars:
extraEnvVars:
# Only add custom profiles if needed beyond auto-detected ones
- name: SPRING_PROFILES_ACTIVE
value: "contentstore-uaa,custom-profile"
# Only if you need to change from default /cs
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_SERVER_CONTEXTPATH
317

-- 317 of 334 --

value: "/custom-cs"
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_STORAGE_CONTENTSTORAGE
value: "DB"
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_LIMITSIZE
value: "50 MiB"
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_TICKETDURATION
value: "10 min"
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_TICKETMULTIDOWNLOAD_ENABLED
value: "true"

The ingress path for public download remains /cs/download (hardcoded in Helm
chart). If you change the application’s context path via
MGMTP_A12_DATASERVICES_CONTENTSTORE_SERVER_CONTEXTPATH, you must also update
the ingress path manually in your values override or via
ingress.publicDownload.path.
Kept Variables
Helm Variable Purpose
ingress.publicDownload.path Ingress path for public download (default: /cs/download,
can be customized)
baseUrl Public URL for attachment download (environment-
specific)
persistence.mountPath Mount path for persistent storage
database.* Database connection settings
UAA Configuration
Removed Variables
Removed Helm Variable Environment Variable Default Value
uaa.authentication.unsecur
edUrls
MGMTP_A12_UAA_AUTHENTICATION_UNSECURED_
URLS
-
uaa.authentication.context
Path
MGMTP_A12_UAA_AUTHENTICATION_CONTEXTPAT
H
/cs/api
uaa.authentication.oauth2.
realmName
Part of issuerUri A12Realm
uaa.authentication.oauth2.
jwkSetUri
SPRING_SECURITY_OAUTH2_RESOURCESERVER_J
WT_JWKSETURI
-
uaa.authentication.oauth2.
jwtPublicKeyLocation
SPRING_SECURITY_OAUTH2_RESOURCESERVER_J
WT_PUBLICKEYLOCATION
-
uaa.authentication.oauth2.
jwsAlgorithms
SPRING_SECURITY_OAUTH2_RESOURCESERVER_J
WT_JWSALGORITHMS
RS256
318

-- 318 of 334 --

Migration Example
# Before (Old values.yaml)
uaa:
authentication:
unsecuredUrls: "/public/**"
contextPath: "/cs/api/v2"
oauth2:
realmName: MyRealm
jwkSetUri: "https://idp.example.com/jwks"
jwsAlgorithms: RS512
# After (New values.yaml)
uaa:
authentication:
oauth2:
issuerUri: http://keycloak-{{ .Release.Namespace }}.{{
.Values.global.cluster.domainName }}/realms/MyRealm
extraEnvVars:
- name: MGMTP_A12_UAA_AUTHENTICATION_UNSECURED_URLS
value: "/public/**"
- name: MGMTP_A12_UAA_AUTHENTICATION_CONTEXTPATH
value: "/cs/api/v2"
- name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWKSETURI
value: "https://idp.example.com/jwks"
- name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWSALGORITHMS
value: "RS512"
Kept Variables
Helm Variable Purpose
uaa.authentication.oauth2.issuerUri OIDC issuer URI (environment-specific, contains
namespace)
Removed Variables
Removed Helm Variable Replacement Notes
uaa.enabled Remove — no longer needed UAA is always active
uaa.authentication.types Remove — no longer needed OIDC config is always
rendered using
uaa.authentication.oauth2.
issuerUri
Actuator Configuration
319

-- 319 of 334 --

Removed Variables
Removed Helm Variable Environment Variable Default Value
actuator.enabled N/A true (always enabled)
actuator.basePath MANAGEMENT_ENDPOINTS_WEB_BASEPATH /actuator
actuator.probeEndpointsEna
bled
N/A true (always enabled)
actuator.prometheusEndpoin
tAccess
MANAGEMENT_ENDPOINT_PROMETHEUS_ACCESS Auto-configured
actuator.exposeEndpoints MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLU
DE
Auto-configured
Migration Example
# Before (Old values.yaml)
actuator:
enabled: true
basePath: /actuator
probeEndpointsEnabled: true
prometheusEndpointAccess: read_only
exposeEndpoints: health,info,prometheus
# After (New values.yaml) - custom base path
extraEnvVars:
- name: MANAGEMENT_ENDPOINTS_WEB_BASEPATH
value: "/actuator" # optional, /actuator is the default
# Enable metrics to auto-configure prometheus endpoint
metrics:
enabled: true
# Other actuator properties via extraEnvVars
extraEnvVars:
- name: MANAGEMENT_ENDPOINT_INFO_ACCESS
value: "read_only"
Using extraEnvVars with Helm Templating
The extraEnvVars field supports Helm templating, allowing you to use dynamic values:
extraEnvVars:
# Using Release namespace
- name: MGMTP_A12_DATASERVICES_CONTENTSTORE_BASEURL
value: "http://client-{{ .Release.Namespace }}.{{
.Values.global.cluster.domainName }}"
# Referencing other values
- name: MY_CUSTOM_PROPERTY
320

-- 320 of 334 --

value: "{{ .Values.myCustomValue }}"
# Using tpl function for complex expressions
- name: COMPLEX_VALUE
value: '{{ tpl .Values.complexTemplate . }}'
Schema Validation Changes
The values.schema.json files have been updated to remove the deprecated properties. If you use
schema validation, ensure your CI/CD pipelines and tooling are updated.
Reference Documentation
For complete documentation on configurable properties, refer to:
Data Services
• application-dataservices-embedded_contentstore.properties.adoc
• application-dataservices-standalone_contentstore.properties.adoc
• application-dataservices-actuators.properties.adoc
Content Store
• application-contentstore-uaa.properties.adoc
Troubleshooting
Issue: Probes Failing After Upgrade
Symptom: Liveness/readiness probes fail after upgrading the Helm chart.
Cause: The actuator base path in the application does not match the probe paths configured in the
Helm chart. This can happen if MANAGEMENT_ENDPOINTS_WEB_BASEPATH is set inconsistently.
Solution: Set the base path via extraEnvVars so the chart and the application use the same value:
# Correct: use extraEnvVars — the chart reads this value for probes and ingress paths
extraEnvVars:
- name: MANAGEMENT_ENDPOINTS_WEB_BASEPATH
value: "/custom-path"
Issue: Configuration Not Applied
Symptom: Configuration changes in extraEnvVars are not reflected in the application.
Cause: Check for typos in environment variable names or incorrect property name format.
Solution: Verify the property name matches the Spring Boot property naming convention:
321

-- 321 of 334 --

• Use uppercase with underscores: SPRING_DATASOURCES_DATASERVICES_URL
• Nested properties use underscores: MGMTP_A12_UAA_AUTHENTICATION_TYPES
Issue: Migration Confusion
Symptom: Unsure which variables to keep vs. remove.
Solution: Use this decision tree:
1. Does the variable contain {{ .Release.Namespace }} or {{ .Values.global.* }}? → Keep it
2. Is it a connection URI, URL, or endpoint? → Keep it (likely environment-specific)
3. Does it control Helm resource creation (e.g., enabled flags)? → Keep it
4. Does it have a default in the Spring profile? → Use extraEnvVars
5. Is it a timeout, size, or duration with a default? → Use extraEnvVars
Caching Provider: Hazelcast Replaced by Infinispan
The caching provider has been replaced with Infinispan. Hazelcast is no longer a dependency of
Data Services.
Removed Configuration Properties
The following properties are no longer supported and must be removed from application
configuration:
spring.cache.type=hazelcast
spring.hazelcast.config=classpath:hazelcast.xml
spring.hazelcast.instance.name=A12S
spring.datasources.dataservices.jpa.properties.hibernate.cache.region.factory_class=..
.
spring.datasources.dataservices.jpa.properties.hibernate.cache.hazelcast.instance_name
=A12S
hz.network.port.port=5701
hz.clusterName=dev
hz.instanceName=A12S
Hibernate Second Level Cache is disabled entirely. The region.factory_class and
hazelcast.instance_name Hibernate properties have no replacement.
New Configuration Properties
Add the following properties to activate the Infinispan cache provider:
spring.cache.type=infinispan
infinispan.embedded.configXml=infinispan.xml
322

-- 322 of 334 --

Dependency Changes
Before
implementation 'com.hazelcast:hazelcast:5.5.0'
implementation 'com.hazelcast:hazelcast-hibernate53:5.2.0'
After
implementation 'org.infinispan:infinispan-spring-boot4-starter-embedded:16.1.2'
Migration Steps for Extending Applications
1. Remove hazelcast.xml (and hazelcast-caches.xml if present) from your application resources.
2. Replace the Hazelcast dependency with org.infinispan:infinispan-spring-boot4-starter-
embedded:16.1.2 in your build.gradle.
3. Copy the sample infinispan.xml from examples-extending-
server/src/main/resources/infinispan.xml to your src/main/resources directory.
4. Add spring.cache.type=infinispan and infinispan.embedded.configXml=infinispan.xml to your
application properties.
5. Remove any Hibernate @Cache entity annotations from your domain classes (Hibernate Second
Level Cache is disabled).
Kubernetes Deployment Changes
JGroups uses port 7800 (TCP) for cluster peer discovery. Add port 7800 to the Deployment container
specification and create a Kubernetes headless Service for JGroups. See the Caching documentation
for the complete headless Service definition and Deployment configuration.
A12S-6643: Relationship-Based Sorting Migration
Overview
Data Services now supports sorting query results by fields on related documents through to-1
relationships. This feature allows sorting by properties of linked documents, such as sorting
Contracts by BusinessPartner name.
Breaking Changes
Order Record Constructor Change
Severity: BREAKING CHANGE
Impact: Code using the 4-parameter canonical constructor of Order record
The Order record canonical constructor has been extended from 4 to 5 parameters to accommodate
the new relationship sorting capability.
323

-- 323 of 334 --

Before (No Longer Works)
// Direct invocation of 4-parameter constructor - BREAKS
Order order = new Order(
Direction.ASC,
"/Contract/Name",
false,
NullHandling.NULLS_LAST
);
After (Migration Options)
Option 1: Use convenience constructors (recommended)
// Simple field sorting with defaults
Order order = new Order("/Contract/Name");
// With direction
Order order = new Order("/Contract/Name", Direction.ASC);
// With direction and case sensitivity
Order order = new Order("/Contract/Name", Direction.ASC, true);
// With direction and null handling
Order order = new Order("/Contract/Name", Direction.ASC, NullHandling.NULLS_LAST);
Option 2: Use new 5-parameter canonical constructor
// Add null as 5th parameter for direct field sorting
Order order = new Order(
Direction.ASC,
"/Contract/Name",
false,
NullHandling.NULLS_LAST,
null // relationshipOrder - new 5th parameter
);
Option 3: For relationship-based sorting
// New relationship sorting capability
RelationshipOrder relationshipOrder = new RelationshipOrder(
"ContractBusinessPartner", // relationship model
"Partner", // target role
"/BusinessPartner/Name", // field on target document
null // no further nesting
);
324

-- 324 of 334 --

Order order = new Order(
Direction.ASC,
null, // no direct field
false,
NullHandling.NULLS_LAST,
relationshipOrder // relationship-based sorting
);
// Or use the convenience constructor for relationship orders
Order order = new Order(
relationshipOrder,
Direction.ASC,
NullHandling.NULLS_LAST
);
Migration Steps
1. Identify usages of the Order constructor in your codebase
2. Replace 4-parameter constructor calls with one of the following:
◦ Convenience constructors (recommended for most cases)
◦ 5-parameter canonical constructor with null for relationshipOrder
3. Test your queries to ensure correct sorting behavior
4. Consider using the new relationship sorting feature where applicable
New Features
Relationship-Based Sorting
You can now sort query results by fields on related documents. The feature supports:
• Single-level sorting: Sort by fields on directly related documents
• Nested sorting: Sort by fields on documents multiple hops away
• Mixed sorting: Combine direct field and relationship field sorting in the same query
• Multiple relationship orders: Use multiple relationship-based sorts (subject to configuration
limits)
JSON API
The JSON API remains backward compatible. The relationshipField property is optional.
Direct Field Sorting (unchanged)
{
"query": {
"targetDocumentModel": "Contract",
"sort": [
325

-- 325 of 334 --

{
"field": "/Contract/Name",
"direction": "ASC",
"ignoreCase": false,
"nullHandling": "NULLS_LAST"
}
]
}
}
Relationship Field Sorting (new)
{
"query": {
"targetDocumentModel": "Contract",
"sort": [
{
"direction": "ASC",
"ignoreCase": false,
"nullHandling": "NULLS_LAST",
"relationshipField": {
"relationshipModel": "ContractBusinessPartner",
"targetRole": "Partner",
"field": "/BusinessPartner/Name"
}
}
]
}
}
Nested Relationship Sorting (new)
{
"query": {
"targetDocumentModel": "Contract",
"sort": [
{
"direction": "ASC",
"ignoreCase": false,
"nullHandling": "NULLS_LAST",
"relationshipField": {
"relationshipModel": "ContractBusinessPartner",
"targetRole": "Partner",
"sortBy": {
"relationshipModel": "PartnerAddresses",
"targetRole": "Address",
"field": "/Address/City"
}
326

-- 326 of 334 --

}
}
]
}
}
Property Name Mapping
Note the property name difference between Java and JSON:
Java Property JSON Property Description
relationshipOrder relationshipField Relationship-based sorting specification
This mapping is handled automatically by Jackson serialization.
Configuration
Two new configuration properties control relationship sorting behavior:
Property Default Description
mgmtp.a12.dataservices.query.r
elationshipOrder.maxNestingDep
th
5 Maximum relationship traversal depth
mgmtp.a12.dataservices.query.r
elationshipOrder.maxCount
5 Maximum relationship orders per query
These limits protect against excessive JOINs and potential circular references.
Validation
Relationship-based sorting includes comprehensive validation:
• Mutual exclusivity: field XOR relationshipField in Order
• Mutual exclusivity: field XOR sortBy in RelationshipOrder
• Null handling required: Explicit nullHandling required for relationship orders
• Cardinality check: Only to-1 relationships are supported
• Permission checks: User must have access to relationship and target models
• Depth limits: Nesting depth must not exceed maxNestingDepth
• Count limits: Number of relationship orders must not exceed maxCount
• Field validation: Fields must exist on target document model
• Repeatable fields: Fields in repeatable groups cannot be used for sorting
Validation errors include descriptive messages to help identify and fix issues.
327

-- 327 of 334 --

Performance Considerations
Relationship-based sorting introduces SQL LEFT JOINs:
• Single-level: 4 JOINs per relationship (source role, link, target role, target document)
• Nested relationships: 4 JOINs per level
• Example: Sorting by Contract → Partner → Address.city generates 8 JOINs
Recommendations:
1. Use sparingly: Only use relationship sorting when necessary
2. Consider alternatives: Denormalize frequently sorted fields if performance is critical
3. Limit depth: Avoid deep nesting (3+ levels) in performance-critical queries
4. Use indexes: Ensure indexed fields are used for terminal sort fields
5. Monitor performance: Test queries with realistic data volumes
6. Combine with pagination: Always use pagination to limit result set size
Security
• Permission checks are performed for all relationship models and target document models
• Users must have access to traverse relationships
• Validation errors do not expose unauthorized model structures
Examples
Example 1: Sort Contracts by Partner Name
RelationshipOrder relOrder = new RelationshipOrder(
"ContractBusinessPartner",
"Partner",
"/BusinessPartner/Name",
null
);
Order order = new Order(relOrder, Direction.ASC, NullHandling.NULLS_LAST);
QueryRoot query = QueryRoot.builder()
.targetDocumentModel("Contract")
.sort(List.of(order))
.paging(new Paging(0, 100))
.projectionName("document")
.build();
Example 2: Sort Contracts by Partner Address City
// Nested: Contract → Partner → Address → city
328

-- 328 of 334 --

RelationshipOrder addressLevel = new RelationshipOrder(
"PartnerAddresses",
"Address",
"/Address/City",
null
);
RelationshipOrder partnerLevel = new RelationshipOrder(
"ContractBusinessPartner",
"Partner",
null,
addressLevel
);
Order order = new Order(partnerLevel, Direction.ASC, NullHandling.NULLS_FIRST);
Example 3: Mixed Sorting (Direct + Relationship)
// Primary sort: Contract name (direct field)
Order directOrder = new Order("/Contract/Name", Direction.ASC);
// Secondary sort: Partner name (relationship field)
RelationshipOrder relOrder = new RelationshipOrder(
"ContractBusinessPartner",
"Partner",
"/BusinessPartner/Name",
null
);
Order relBasedOrder = new Order(relOrder, Direction.ASC, NullHandling.NULLS_LAST);
QueryRoot query = QueryRoot.builder()
.targetDocumentModel("Contract")
.sort(List.of(directOrder, relBasedOrder))
.paging(new Paging(0, 100))
.projectionName("document")
.build();
Further Information
• See Query API Documentation for complete feature documentation
• Configuration properties: Configuration Options
• Error codes: Validation Errors
Spring Dependencies Upgrade
Breaking changes
329

-- 329 of 334 --

Dependencies
Upgrading Spring dependencies may constitute a breaking change according to the Breaking
Change Management policy (Dependencies – Breaking). Review the exact upgraded versions in
gradle/libs.versions.toml and validate downstream impacts.
Impact
• Build may fail due to transitive dependency alignment and plugin requirements
• API behavior can change due to upstream fixes in Spring Framework/Boot
• Actuator/metrics exposure and defaults can differ across minor releases
Migration Steps
# Before
springBootVersion = "3.x"
# After (example – adjust to target)
springBootVersion = "4.1.0"
// Verify BOM usage remains consistent
implementation platform(libs.springBootBom)
// Starters are versioned via BOM
implementation libs.springBootStarterWeb
implementation libs.springBootStarterActuator
• Update springBootVersion in the Version Catalog and ensure Boot BOM consumption remains
intact
• Run ./gradlew revapiCheck codeCheck spotlessApply build to detect API breaks and formatting
issues
• Inspect warnings from spring-boot-properties-migrator (if enabled) and adjust application
properties accordingly
• Verify application starts via ./gradlew dataservices-server-app:bootRun and check health
endpoints
Hibernate Naming Strategy Relocation
Spring Boot 4 relocates HibernateImplicitNamingStrategy from
org.springframework.boot.orm.jpa.hibernate to org.springframework.boot.hibernate.
# Before
spring.datasources.contentstore.jpa.properties.hibernate.implicit_naming_strategy=org.
springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
# After
330

-- 330 of 334 --

spring.datasources.contentstore.jpa.properties.hibernate.implicit_naming_strategy=org.
springframework.boot.hibernate.SpringImplicitNamingStrategy
Applications overriding JPA naming strategies MUST update the class name to the new package.
Jackson 3 Upgrade (Package and API changes)
Jackson has been upgraded to 3.x. Packages move from com.fasterxml.jackson.* to tools.jackson.*,
and several APIs change (e.g., SerializerProvider → SerializationContext).
// Before
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
// After
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
Key changes to consider:
• JsonParser string accessors: getText() → getString(), nextTextValue() → nextStringValue()
• Exception types: JsonParseException → StreamReadException, JacksonException
• Deserializer base classes: JsonDeserializer<T> → ValueDeserializer<T> (in some cases)
Client code and custom serializers/deserializers MUST be adapted accordingly. Validate all domain
marshalling utilities and RPC payload handling.
Content Store Base URL Mandatory
Content Store server now enforces mgmtp.a12.dataservices.contentstore.base-url presence at
startup. Missing or blank value causes application startup to fail with UnexpectedException.
# Required
mgmtp.a12.dataservices.contentstore.base-url=http://localhost:${server.port:8080}
If you previously relied on default/implicit behavior, set the property explicitly per environment.
Content Store Client Error Handling
The Content Store client error handler was refactored from extending Spring’s
DefaultResponseErrorHandler to implementing ResponseErrorHandler directly. This can change how
HTTP errors are detected and mapped to exceptions.
// Before
public class ContentStoreErrorHandler extends DefaultResponseErrorHandler {
331

-- 331 of 334 --

// inherits: hasError(HttpStatus) and default exception mapping
}
// After
@Slf4j
@RequiredArgsConstructor
public class ContentStoreErrorHandler implements ResponseErrorHandler {
@Override
public boolean hasError(ClientHttpResponse response) { /* custom logic */ }
@Override
public void handleError(ClientHttpResponse response) { /* custom mapping */ }
}
Impact:
• Exception types and messages may differ from Spring defaults (HttpClientErrorException
/HttpServerErrorException).
• Error detection may vary if hasError uses custom status classification.
• Charset/body decoding is no longer implied by DefaultResponseErrorHandler; ensure consistent
parsing in the custom handler.
Migration Steps:
• Update consumers that catch specific Spring Http*ErrorException types to handle the new
mapping or preserve compatibility in the handler.
• Align hasError logic with intended status handling (typically 4xx/5xx), document any deviations.
• Add unit tests covering 4xx/5xx responses, non-error statuses, and error body parsing/logging.
Data Services Client Error Handling
The Data Services client error handler was refactored from extending Spring’s
DefaultResponseErrorHandler to implementing ResponseErrorHandler directly. This can change how
HTTP errors are detected and mapped to exceptions.
// Before
public class DataServicesErrorHandler extends DefaultResponseErrorHandler {
// inherits: hasError(HttpStatus) and default exception mapping
}
// After
public class DataServicesErrorHandler implements ResponseErrorHandler {
@Override public boolean hasError(ClientHttpResponse response) { /* custom logic
*/ }
@Override public void handleError(ClientHttpResponse response) { /* custom mapping
332

-- 332 of 334 --

*/ }
}
Impact:
• Exception types and messages may differ from Spring defaults (HttpClientErrorException
/HttpServerErrorException).
• Error detection may vary if hasError uses custom status classification.
• Charset/body decoding is no longer implied by DefaultResponseErrorHandler; ensure consistent
parsing in the custom handler.
Migration Steps:
• Update consumers that catch specific Spring Http*ErrorException types to handle the new
mapping or preserve compatibility in the handler.
• Align hasError logic with intended status handling (typically 4xx/5xx), document any deviations.
• Add unit tests covering 4xx/5xx responses, non-error statuses, and error body parsing/logging.
Data Source and Transactional context changes
• All transactions in Data Services use dsTransactionManager.
• Extension modules with @EnableJpaRepositories must explicitly set transactionManagerRef =
"dsTransactionManager" — @EnableJpaRepositories resolves this attribute by bean name, not by
@Primary. Omitting it produces the following error when the repository is first used
transactionally:
No bean named 'transactionManager' available: No matching TransactionManager bean
found
for qualifier 'transactionManager' - neither qualifier match nor bean name match!
Note that this error may not appear on normal server startup if the repository is only used by a
service gated with @ConditionalOnProperty that is disabled by default — the failure only surfaces
when that feature is enabled. See Custom JPA Entities and Repositories for the required
configuration pattern.
Configuration changes
No new configuration keys introduced by this ticket. Review spring.* and management.* properties
for upstream changes; adjust profiles if defaults differ.
Deprecations
No deprecations introduced by this ticket. Address upstream deprecation warnings emitted during
build or startup.
333

-- 333 of 334 --

Behavior changes
Minor behavior changes may result from upstream bug fixes and default adjustments in Spring
Boot/Framework. Validate:
• Error handling and exception mapping in controllers
• Actuator endpoint exposure and path mappings
• HTTP client behavior if using spring-boot-starter-restclient
Jackson mapping exceptions are now handled by a dedicated mapper in server layer. Error
payloads for JSON mapping failures may differ (exception type and message). Review client error
parsing if it relied on exact texts.
Health endpoints and actuator IDs are classified now. For Data Services it’s
dataservicesInitializationFinished and for Content Store contentstoreInitializationFinished.
Information
• Classification: Dependencies – potential breaking change when upgrading to a new major or
behavior‑altering minor release
• Testing: Execute module tests and regression tests; verify startup logs and actuator health
• Documentation: No UML diagram changes required unless public API signatures in Data
Services are modified by follow‑up work
334

-- 334 of 334 --

