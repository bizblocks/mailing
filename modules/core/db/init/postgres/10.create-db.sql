-- begin MAILING_MAILING
create table MAILING_MAILING (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    MAILING_TARGET_SCRIPT_ID uuid,
    CONSOLIDATION_GROOVY_ID uuid,
    CONSOLIDATION_JSON text,
    NOTIFICATION_BUILD_SCRIPT_ID uuid,
    OBJECT_FILTER_SCRIPT_ID uuid,
    FINAL_CHECK_SCRIPT_ID uuid,
    FINAL_DECORATION_SCRIPT_ID uuid,
    MAILING_PERFORMERS varchar(1000),
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end MAILING_MAILING
-- begin MAILING_GROOVY_SCRIPT
create table MAILING_GROOVY_SCRIPT (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    SCRIPT text,
    SCRIP_VERSION integer,
    SCRIPT_VERSION_COMMENT varchar(255),
    RETURN_TYPE integer,
    ENTITY_TYPE varchar(255),
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end MAILING_GROOVY_SCRIPT
-- begin MAILING_NOTIFICATION
create table MAILING_NOTIFICATION (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    TARGET_ID uuid,
    TEMPLATE_JSON text,
    SEND_DATE date,
    STAGE integer,
    MAILING_ID uuid,
    --
    primary key (ID)
)^
-- end MAILING_NOTIFICATION
-- begin MAILING_NOTIFICATION_STAGE_LOG
create table MAILING_NOTIFICATION_STAGE_LOG (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    DATE_ date,
    STAGE integer,
    NOTIFICATION_ID uuid,
    --
    primary key (ID)
)^
-- end MAILING_NOTIFICATION_STAGE_LOG
-- begin MAILING_MAILING_IDENTIFIER
create table MAILING_MAILING_IDENTIFIER (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    OBJECT_ID varchar(255),
    IDENTIFIER_NAME varchar(255) not null,
    IDENTIFIER_VALUE varchar(255) not null,
    --
    primary key (ID)
)^
-- end MAILING_MAILING_IDENTIFIER
