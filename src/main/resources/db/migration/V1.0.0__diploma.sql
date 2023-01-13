create sequence hibernate_sequence start 1 increment 1;

create table SummitList
(
    summitCode varchar(255) not null,
    summitName varchar(255),
    validFrom  date,
    validTo    date,
    primary key (summitCode)
);

create table SummitListUpdateLog
(
    id          int8 not null,
    date        timestamp,
    updateCount int4 not null,
    primary key (id)
);
create index idx_summitlistentry on SummitList (summitCode);
create index idx_summitlistentry_validfrom on SummitList (validFrom);
create index idx_summitlistentry_validto on SummitList (validTo);
