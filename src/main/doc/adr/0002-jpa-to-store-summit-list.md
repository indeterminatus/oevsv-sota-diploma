# 2. JPA to store summit list

Date: 2022-10-07

## Status

Accepted

## Context

The list of summits must be fetched (and kept) to filter out logged activations whose summit was not valid at the time
of attempted activation. In the log entries, there would be 0 points awarded, but also activations with less than 4
QSOs get no points (but count for the diploma nonetheless).

To filter only those, we need to know when each summit was valid and thus can be accepted for certain activations.

The list of summits is currently a CSV file with ~23 MiB in size and ~170k entries. It rarely changes (on a global
scale, roughly every month, for Austria, probably once a month).

To fetch it only as often as necessary, while providing fast and memory-efficient means to query the data, the summits
need to be persisted somehow.

## Decision

JPA was chosen, since it works nicely with Quarkus.

## Consequences

Easier to do:
 * No custom management for persistence
 * Out-of-the-box support for Quarkus

Risks:
 * Needs a DBMS installed
