# 8. Changed font from Copperplate Gothic Bold to Tiffany Gothic CC

Date: 2023-04-14

## Status

Accepted

## Context

Some Names with special characters would not properly render on the award.
These characters were missing in the font `Copperplate Gothic Bold`.

The easiest fix was to replace the font with one that contains the relevant
characters.

The choices established were:

* Copperplate CC
* Tiffany Gothic CC

Both have licenses we can use, and seem good choices.

## Decision

Using `Tiffany Gothic CC`, because it is visually closer to the font it should replace.

## Consequences

Future awards do not look exactly the same as previous ones. We should be able to 
deal with more names.
