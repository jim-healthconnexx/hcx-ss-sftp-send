# Copilot Instructions

## Stack
- Java 21, Maven, Spring Boot 2.7
- PostgreSQL via jOOQ (no JPA/Hibernate)
- Docker Compose

## Architecture
- REST controllers → services → jOOQ writers
- One transaction per patient (`@Transactional` on writer)
- FHIR parsing and OMOP building are plain Java objects (not Spring beans)
- Spring beans: controllers, services, writers, `JooqIdGenerator`, `OmopJooqConfig`

## Conventions
- Constructor injection only (`@RequiredArgsConstructor`); no field injection
- Use `@Slf4j`; `log.debug()` for row-level detail, `log.error()` + rethrow on failures
- jOOQ only for DB access: `table(name("..."))`, `field(name("..."))`; no schema prefixes, no raw SQL
- Always use `idGenerator.getId("table_name", "id_column")` for OMOP PKs
- Prefix log messages and comments with the Jira ticket (e.g. `// HDQ-42:`)
- Mark replaced code `@Deprecated`; leave it in place for reference

## Do Not Use
- JPA, Hibernate, or raw JDBC connection management
- Schema-qualified table names in jOOQ calls

## Terminal Output Handling

Due to IDE limitations, terminal output may not be visible to the agent.

When executing terminal commands:

1. Always redirect stdout and stderr to a file in the project root.
2. Use deterministic filenames.
3. Read the file to inspect results instead of relying on terminal output.
4. Remove files after processing to avoid clutter.

## JIRA Workflow Rules (mcp-atlassian)

When working on any task associated with a JIRA ticket, you MUST follow these rules strictly:

### Planning Phase
- **ALWAYS** begin by documenting a detailed implementation plan as a comment on the associated JIRA ticket before touching any code
- The plan comment must include:
    - Summary of the problem/feature
    - Files that will be created or modified
    - Approach and reasoning
    - Any risks or dependencies identified
    - Any suggestions to add to the implementation plan for future reference
- Planning Phase may proceed without approval.

### Approval Gate
- **NEVER** make any code changes until the plan comment on the JIRA ticket has received explicit approval
- Approval is defined as a comment from an authorized team member containing words like "approved", "LGTM", or "go ahead"
- If no approval exists, stop and remind the user that the plan must be approved before proceeding

### Implementation Phase
- **ALWAYS** follow the approved plan exactly; any deviations must be documented as a new comment and approved before implementation
- If you encounter any blockers or new information during implementation, document it as a comment on the JIRA ticket and seek approval for any changes to the plan
- once implementation is complete, add a comment summarizing the work done and any remaining tasks or follow-ups needed

### Ticket Transitions
- **NEVER** transition a JIRA ticket status under any circumstances
- Do not call any MCP transition or status-change operations on JIRA tickets
- Leave all ticket transitions to be performed manually by the team

### General JIRA Etiquette
- Only add comments — never edit or delete existing comments
- Always reference the ticket key (e.g., PROJ-123) in any commit messages or PR descriptions
- Do not assign or reassign tickets