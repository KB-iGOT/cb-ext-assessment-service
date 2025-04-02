package com.igot.cb.common.helper.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;

import java.util.List;

/**
 * Interface for cassandra connection manager , implementation would be Standalone and Embedde
 * cassandra connection manager .
 */
public interface CassandraConnectionManager {
  CqlSession getSession(String keyspaceName);

  List<String> getTableList(String keyspacename);
}