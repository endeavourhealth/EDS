#!/bin/sh -e

HOST=${1:-localhost}
SCHEMA_DIR=$(readlink -f $(dirname $0)/../src/database/cql)

load_file() {
    echo "loading $1..."
    cqlsh -f "$1"
}

load_file "$SCHEMA_DIR/config schema.cql"
load_file "$SCHEMA_DIR/config data.cql"
load_file "$SCHEMA_DIR/admin/admin-schema.cql"
load_file "$SCHEMA_DIR/audit/audit schema.cql"
load_file "$SCHEMA_DIR/ehr/ehr-schema.cql"
load_file "$SCHEMA_DIR/logging/logging schema.cql"
