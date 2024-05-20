# 11. Use own ID sequence for OE20SOTA

Date: 2024-05-17

## Status

Accepted

## Context

When a Special Award OE20SOTA is generated, it should start with the ID **OE20-0001**.

To allow using almost all the existing mechanisms, the existing table `diplomalog` should be used for the special
awards as well. This has the side effect that existing ID will still count up, but will not be used.

The next "normal" award will thus appear to skip an element.

## Decision

The side effect is acceptable for award manager Martin OE5REO. The proposal was accepted.

## Consequences

There is a new column `oe20` in `diplomalog`, with a unique constraint on all non-null values.
Some "DB tricks" are necessary to ensure data consistency. These include a trigger and a stored procedure, but will be
automatically set up with a Flyway migration.

Adding other special awards later on should be possible with this approach; however, it should be noted that every such
award would require another column in `diplomalog`. This scales only up to a certain point.
