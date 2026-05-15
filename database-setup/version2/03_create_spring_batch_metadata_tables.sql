-- Source: spring-batch-core 5.2.5 / org/springframework/batch/core/schema-postgresql.sql

CREATE SCHEMA IF NOT EXISTS batch;

CREATE TABLE IF NOT EXISTS batch.batch_job_instance (
    job_instance_id bigint NOT NULL PRIMARY KEY,
    version bigint,
    job_name varchar(100) NOT NULL,
    job_key varchar(32) NOT NULL,
    CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
);

CREATE TABLE IF NOT EXISTS batch.batch_job_execution (
    job_execution_id bigint NOT NULL PRIMARY KEY,
    version bigint,
    job_instance_id bigint NOT NULL,
    create_time timestamp NOT NULL,
    start_time timestamp DEFAULT NULL,
    end_time timestamp DEFAULT NULL,
    status varchar(10),
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id)
        REFERENCES batch.batch_job_instance (job_instance_id)
);

CREATE TABLE IF NOT EXISTS batch.batch_job_execution_params (
    job_execution_id bigint NOT NULL,
    parameter_name varchar(100) NOT NULL,
    parameter_type varchar(100) NOT NULL,
    parameter_value varchar(2500),
    identifying char(1) NOT NULL,
    CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch.batch_job_execution (job_execution_id)
);

CREATE TABLE IF NOT EXISTS batch.batch_step_execution (
    step_execution_id bigint NOT NULL PRIMARY KEY,
    version bigint NOT NULL,
    step_name varchar(100) NOT NULL,
    job_execution_id bigint NOT NULL,
    create_time timestamp NOT NULL,
    start_time timestamp DEFAULT NULL,
    end_time timestamp DEFAULT NULL,
    status varchar(10),
    commit_count bigint,
    read_count bigint,
    filter_count bigint,
    write_count bigint,
    read_skip_count bigint,
    write_skip_count bigint,
    process_skip_count bigint,
    rollback_count bigint,
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch.batch_job_execution (job_execution_id)
);

CREATE TABLE IF NOT EXISTS batch.batch_step_execution_context (
    step_execution_id bigint NOT NULL PRIMARY KEY,
    short_context varchar(2500) NOT NULL,
    serialized_context text,
    CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id)
        REFERENCES batch.batch_step_execution (step_execution_id)
);

CREATE TABLE IF NOT EXISTS batch.batch_job_execution_context (
    job_execution_id bigint NOT NULL PRIMARY KEY,
    short_context varchar(2500) NOT NULL,
    serialized_context text,
    CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch.batch_job_execution (job_execution_id)
);

CREATE SEQUENCE IF NOT EXISTS batch.batch_step_execution_seq MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS batch.batch_job_execution_seq MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS batch.batch_job_seq MAXVALUE 9223372036854775807 NO CYCLE;


-- rollback
DROP SEQUENCE IF EXISTS batch.batch_job_seq;
DROP SEQUENCE IF EXISTS batch.batch_job_execution_seq;
DROP SEQUENCE IF EXISTS batch.batch_step_execution_seq;
DROP TABLE IF EXISTS batch.batch_job_execution_context;
DROP TABLE IF EXISTS batch.batch_step_execution_context;
DROP TABLE IF EXISTS batch.batch_step_execution;
DROP TABLE IF EXISTS batch.batch_job_execution_params;
DROP TABLE IF EXISTS batch.batch_job_execution;
DROP TABLE IF EXISTS batch.batch_job_instance;
