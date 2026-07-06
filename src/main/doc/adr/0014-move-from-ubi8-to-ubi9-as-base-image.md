# 14. Move from ubi8 to ubi9 as base image

Date: 2026-07-06

## Status

Accepted

## Context

An updated GraalVM builder image links against a glibc version, which is not available in ubi8. The update happened
upstream.

We have multiple options to solve this:
- Use a different base image that includes the required glibc version
- Use a different GraalVM image that links against a different glibc version
- (Not recommended: pin the glibc version or do not use glibc at all)

## Decision

We use a different base image that includes the required glibc version. The currently compatible version is ubi9.

## Consequences

Staying up to date with the latest GraalVM features should keep us agile in changes later on.

A new base image should make us future-proof. As always, especially PDF generation and mail sending need to be tested
manually.
