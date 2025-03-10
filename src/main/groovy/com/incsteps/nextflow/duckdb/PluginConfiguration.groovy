/*
 * MIT License
 *
 * Copyright (c) 2025 Incremental Steps Software Solutions
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.incsteps.nextflow.duckdb

import groovy.transform.PackageScope

@PackageScope
class PluginConfiguration {

    final private String file
    final private PostgresConfiguration postgresConfiguration

    PluginConfiguration(Map map){
        def config = map ?: Collections.emptyMap()
        this.file = config.file?.toString()
        if( config.postgre && config.postgre instanceof Map){
            def mappg = config.postgre as Map
            postgresConfiguration = new PostgresConfiguration(
                    (mappg.enabled?.toString() ?: "false").toBoolean(),
                    mappg.host?.toString(),
                    mappg.user?.toString(),
                    mappg.password?.toString(),
                    mappg.database?.toString()
            )
        }else{
            postgresConfiguration = null
        }
    }

    String getFile() {
        return file
    }

    PostgresConfiguration getPostgresConfiguration() {
        return postgresConfiguration
    }

    class PostgresConfiguration{
        final private boolean enabled
        final private String host
        final private String user
        final private String password
        final private String database

        PostgresConfiguration(boolean enabled, String host, String user, String password, String dbname) {
            this.enabled = enabled
            this.host = host
            this.user = user
            this.password = password
            this.database = dbname
        }

        boolean isEnabled(){
            enabled
        }

        String getHost() {
            return host
        }

        String getUser() {
            return user
        }

        String getPassword() {
            return password
        }

        String getDatabase() {
            return database
        }
    }
}
