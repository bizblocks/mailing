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
    ENTITY_TYPE_FOR_PERSONAL_SETTINGS varchar(255),
    ORIGIN_MAILING_ID uuid,
    ENTITY_ID_FOR_PERSONAL_SETTINGS uuid,
    USE_DEFAULT_MAILING boolean,
    ACTIVATED boolean,
    STRING_ID varchar(255),
    CONSOLIDATION_GROOVY_ID uuid,
    CONSOLIDATION_JSON text,
    NOTIFICATION_BUILD_SCRIPT_ID uuid,
    OBJECT_FILTER_SCRIPT_ID uuid,
    FINAL_CHECK_SCRIPT_ID uuid,
    ADAPTER_FOR_MAILING_TARGET_SCREEN_ID uuid,
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
    TARGET_ENTITY_UUID uuid,
    NOTIFICATION_CHANNELS text,
    TEMPLATE_JSON text,
    SEND_DATE date,
    STAGE integer,
    MAILING_ID uuid,
    TARGET_ENTITY_TYPE varchar(255),
    TARGET_ENTITY_NAME varchar(255),
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

-- begin MAILING_WAY_TO_GET_MAILING_IDENTIFIER
create table MAILING_WAY_TO_GET_MAILING_IDENTIFIER (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    ENTITY_TYPE varchar(255),
    IS_GENERAL boolean,
    ENTITY_ID varchar(255),
    WAY_TO_GET_IDENTIFER_FROM_ENTITY_FIELDS varchar(255),
    NOTIFICATION_CHANEL varchar(255),
    MAILING_TO_REQUEST_IDENTIFER_ID uuid,
    FIELD_USED_AS_EXTERNAL_ID varchar(255),
    MAILING_IDENTIFIER text,
    --
    primary key (ID)
)^
-- end MAILING_WAY_TO_GET_MAILING_IDENTIFIER
-- begin MAILING_MAILING_GROOVY_SCRIPT_LINK
create table MAILING_MAILING_GROOVY_SCRIPT_LINK (
    MAILING_ID uuid,
    GROOVY_SCRIPT_ID uuid,
    primary key (MAILING_ID, GROOVY_SCRIPT_ID)
)^
-- end MAILING_MAILING_GROOVY_SCRIPT_LINK
