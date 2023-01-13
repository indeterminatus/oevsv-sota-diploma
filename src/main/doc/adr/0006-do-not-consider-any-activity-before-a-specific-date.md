# 6. Do not consider any activity before a specific date

Date: 2022-12-07

## Status

Accepted

## Context

Awards shall only consider logged entries after a specific date. What the date is, is configurable but fixed; the
agreed-upon value is 2023-01-01.

## Decision

The decision was made by Sylvia OE5YYN. The technical decision to expose a setting for this date was made by David
OE5IDT.

## Consequences

Easier to do:
 * Change the date value later on, for future installations

Risks involved:
 * Mis-configuration might lead to no awards granted, ever, without any hint why
 * Changing this setting in a running system may yield unfair results
