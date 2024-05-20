# 12. Extend timeout for sending mails

Date: 2024-05-19

## Status

Accepted

## Context

Some mails with PDFs for review were sent twice for inspection.

After ensuring that no data inconsistencies occur, the issue itself is annoying at most, since that incurs added
work for the award manager.

Sending mails on average takes 8.4 seconds (as of the time of this writing). In the occasions the mails were sent twice,
the first attempt took approximately 12 seconds to complete.

By that time, an internal timeout hit and marked the attempt as failed.

## Decision

The existing timeout of 10 seconds is too tight for the current performance in production. It will be extended to
20 seconds.

Since this happens in the background, the incurred risk is acceptable.

## Consequences

Less mail should time out internally, and thus the chance shrinks to deliver a request for review twice.
The internal loop may take a bit longer now, especially when the mail server never responds in time.

We have no good historic evolution of performance metrics, since there is no telemetry active in production;
it may be worthwhile to evaluate if sending mails deteriorates over time.

There might be another issue lurking.
