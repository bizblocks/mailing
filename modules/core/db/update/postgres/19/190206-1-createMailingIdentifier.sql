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
);
