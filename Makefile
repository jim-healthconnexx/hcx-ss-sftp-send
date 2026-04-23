# HDC-35: Deploy hcx-ss-sftp-send to RSP03 via the mypi Docker context.
# Prerequisites:
#   - SSH key-based auth to rsp03 must be configured (ssh://jhstansell@rsp03)
#   - local-infra Docker network must exist on RSP03 (run `make setup` once if needed)
#   - .env.local (or HCX_ENV_FILE) must exist locally before running `make up`
#
# Override the target context: make CONTEXT=default up
CONTEXT ?= mypi

.PHONY: build up down logs ps setup deploy rebuild

## Compile the JAR locally, then build the Docker image on RSP03.
## The Dockerfile copies target/*.jar — run this before `make up`.
build:
	./mvnw -DskipTests package
	docker --context $(CONTEXT) compose build

## Start the app stack on RSP03 (detached).
up:
	docker --context $(CONTEXT) compose up -d

## Stop the app stack on RSP03.
down:
	docker --context $(CONTEXT) compose down

## Tail container logs from RSP03.
logs:
	docker --context $(CONTEXT) compose logs -f

## Show running containers on RSP03.
ps:
	docker --context $(CONTEXT) compose ps

## Create the local-infra network on RSP03 (idempotent — safe to run multiple times).
setup:
	docker --context $(CONTEXT) network create local-infra 2>/dev/null || true

## Full pipeline: compile JAR, build image on RSP03, start stack.
deploy: build up

## Force-rebuild the image without Docker layer cache (useful for troubleshooting stale layers).
rebuild:
	./mvnw -DskipTests package
	docker --context $(CONTEXT) compose build --no-cache
	docker --context $(CONTEXT) compose up -d

