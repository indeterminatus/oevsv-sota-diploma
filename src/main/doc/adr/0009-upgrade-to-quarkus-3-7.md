# 9. Upgrade to Quarkus 3.7

Date: 2024-04-07

## Status

Accepted

## Context

Quarkus released a new minor version, 3.7, which provides some critical performance and stability improvements.
Mainly for maintenance and future development, it is critical that the software runs atop the latest platform.

## Decision

The decision to upgrade was not light-hearted, because some incompatibilities were introduced:

* GZIP support changed
* RESTeasy client no longer supported

## Consequences

Easier to do in the future:

* Support for RESTeasy Reactive, which seems to be the way forward
* Tooling support is best for newest (and officially maintained) version of Quarkus

Risks:

* Some library upgrades are affected as well, potentially breaking compatibility with native image.
