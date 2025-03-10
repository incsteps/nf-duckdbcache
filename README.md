# Nextflow DuckDB cache

Nextflow's DuckDB Cache Plugin enables the storage of task execution metadata in a DuckDB database, ensuring data persistence and scalability. This plugin is particularly useful for workflows that require robust, centralized caching mechanisms across distributed environments.

Add the plugin to your `nextflow.config` , configure it to connect with a database and ... this is all.
The plugin will create required tables and will store tasks context when you run your pipeline

Among a way to *Centralized Cache Storage*, using this plugin, you're able to see log executions in real time

## Available backends

- memory: for *dev*, use an in-memory database so no cache is used across executions
- file: similar to Nextflow default implementation but using a Duckdb database
- postgre: in local or remote you can use a PostgreSQL instance to store your cache (shareable between member of a team for example)

## License

This plugin is licensed under the MIT License.

## Contributing

Contributions are welcome! Please submit issues or pull requests to the project's GitHub repository.

## Contact

For commercial support, contact the Incremental Steps Software Solutions team at hello@incsteps.com or refer to the plugin documentation at
https://incsteps.github.io/nf-duckdbcache/index.html
