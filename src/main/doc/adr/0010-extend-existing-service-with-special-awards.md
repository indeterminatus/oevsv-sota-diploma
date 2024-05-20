# 10. Extend existing service with special awards

Date: 2024-05-14

## Status

Accepted

## Context

The special occasion of the award OE20SOTA was used to extend the existing service with new award
calculation capabilities.

## Decision

It had not been explicitly stated in the requirements, but the reuse of existing infrastructure makes
perfect sense and was accepted.

## Consequences

Easier to do:
* add other special awards later on

Performance **slightly deteriorates** with the added complexity of another check to perform.
Overall, the effect is still negligible, but it is something to keep in mind when adding
award after award.

The size of the deployed image increases by approximately **30 MiB**, most of which is attributed
to high-res background graphics.
