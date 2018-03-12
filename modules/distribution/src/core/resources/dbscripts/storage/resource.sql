CREATE TABLE IF NOT EXISTS resource (
                uuid VARCHAR(250),
                tenantId VARCHAR(250),
                fileName VARCHAR(250),
                contentLength INT,
                contentType VARCHAR(150),
                content BYTEA
)
;
