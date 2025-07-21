#!/bin/bash -ex

npx prettier . --check && npm run lint
