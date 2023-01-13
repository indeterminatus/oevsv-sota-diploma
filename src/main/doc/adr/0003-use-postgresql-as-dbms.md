# 3. Use PostgreSQL as DBMS

Date: 2022-10-07

## Status

Accepted

## Context

Persisting (and regularly updating) ~170k summits and their period of validity. Why this is needed, please refer to
ADR-0002. First attempts to use H2 failed ignominiously, as the DB would become corrupted after a while; it did not
cope well with more than 100k rows.

## Decision

Using PostgreSQL as DBMS.

## Consequences

PostgreSQL is a stable and production-ready DBMS. It is expected to handle the load nicely.
