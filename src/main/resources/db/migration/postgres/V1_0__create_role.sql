CREATE ROLE risk_profiler LOGIN PASSWORD '${database_password}';
GRANT USAGE ON SCHEMA risk_profiler TO risk_profiler;
