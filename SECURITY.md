# Security policy

This document gathers security-related policies and guidelines for the codebase available in this repository.

This libraries help developers create [Solid](https://solidproject.org/) applications. The libraries are composed of different modules with different functionalities.

* Some modules help with reading and writing data in Solid servers. Data should always be considered sensitive and be processed with care and regards to access restrictions and personal information.
* Some modules help with Authentication. Authentication is a sensitive domain, and as such we designed these libraries with a particular attention to security. In particular, we decided to apply the following rules:
  * Comply with the [OAuth security guidelines](https://datatracker.ietf.org/doc/id/draft-ietf-oauth-security-topics-15.html). This involves, among other things:
    * No support for the implicit grant and the resource owner password grant;
    * The use of a PKCE token;
    * Binding tokens to a DPoP key to make them sender-constrained whenever possible.

# Reporting a vulnerability

If you discover a vulnerability in our code, or experience a bug related to security, please report it following the instructions provided on [Inrupt’s security page](https://inrupt.com/security/).
