#!/bin/zsh
# To configure Docker, see https://vaadin.com/docs/latest/flow/testing/end-to-end/test-grid
# or https://github.com/SeleniumHQ/docker-selenium/blob/trunk/docker-compose-v3.yml

mvn verify -Dcom.vaadin.testbench.Parameters.hubHostname=localhost "$@"
