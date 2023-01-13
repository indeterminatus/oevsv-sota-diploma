# 5. Use Redis for rate-limiting

Date: 2022-11-17

## Status

Accepted

## Context

Rate-limiting the relevant API calls to mitigate flooding/DoS attacks should be implemented, to keep the user's
experience as clean and slick as possible (e.g., do not require captchas or similar techniques).

If rate-limiting can be provided by another layer, our approach might not be needed and could be discarded.

## Decision

Using a cheap Redis instance to keep track of requests (with automated expiry) is simple enough to provide the most
basic protection against bots, scraping, or DoS attacks.

## Consequences

The service gets basic protection against bots, scraping, or DoS attacks. There is no protection against DDoS attacks.

Easier to do:
 * no captcha required
 * estimating the boundary conditions for CPU and memory

More difficult to do:
 * added complexity of rate-limiting needs to be taken into account for future developments
