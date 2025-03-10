/*
 * MIT License
 *
 * Copyright (c) 2025 EDN
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

import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.exception.AbortOperationException
import org.duckdb.DuckDBResultSet

import java.sql.Connection
import java.sql.DriverManager

@CompileStatic
@Slf4j
class PluginSQL {

    private final PluginConfiguration configuration

    private Sql sql

    PluginSQL(PluginConfiguration configuration) {
        this.configuration = configuration
        Class.forName("org.duckdb.DuckDBDriver")
    }

    protected String buildURL(){
        String suffix = ""
        if( configuration.file ){
            suffix = configuration.file
        }
        return "jdbc:duckdb:$suffix"
    }

    protected boolean usePostgre(){
        configuration.postgresConfiguration && configuration.postgresConfiguration.enabled
    }

    protected void attachPostgre(PluginConfiguration.PostgresConfiguration config){
        sql.execute "ATTACH 'dbname=$config.database user=$config.user password=$config.password host=$config.host' AS db (TYPE postgres);".toString()
        sql.execute "use db;"
    }

    protected String getSentenceFromResource(String path){
        this.getClass().getResourceAsStream(path).text
    }

    void validateConnection(){
        try{
            sql = Sql.newInstance(buildURL(),'org.duckdb.DuckDBDriver')
            if( usePostgre() ){
                attachPostgre(configuration.postgresConfiguration)
            }
        }catch(Exception e){
            log.error "Error validating cache connection",e
            throw new AbortOperationException("Invalid connection for nf-pqcache")
        }
    }

    void createTablesIfRequired(){
        try{
            sql.execute getSentenceFromResource("/create-index.sql")
            sql.execute getSentenceFromResource("/create-cache.sql")
        }catch(Exception e){
            log.error "Error creating tables",e
            throw new AbortOperationException("Invalid connection for nf-pqcache")
        }
    }

    byte[] readCache(String session, long id){
        def row = sql.firstRow(getSentenceFromResource("/select-cache.sql"), [
                id:id,
                session_id:session
        ])
        DuckDBResultSet.DuckDBBlobResult entry = row?.entry as DuckDBResultSet.DuckDBBlobResult
        entry?.binaryStream?.readAllBytes()
    }

    void writeCache(String session, long id, byte[]entry){
        deleteCache(session, id)
        sql.executeUpdate(getSentenceFromResource("/insert-cache.sql"), [
                id:id,
                session_id:session,
                entry:entry
        ])
    }

    void deleteCache(String session, long id){
        sql.executeUpdate(getSentenceFromResource("/delete-cache.sql"), [
                id:id,
                session_id:session
        ])
    }

    void writeIndex(long key, String session, String name, boolean cached) {
        deleteIndex(session, name)
        sql.executeUpdate(getSentenceFromResource("/insert-index.sql"), [
                id:key, cached:cached, session_id:session, name:name
        ])
    }

    void deleteIndex(String session, String name){
        sql.executeUpdate(getSentenceFromResource("/truncate-index.sql"),[
                session_id:session, name:name
        ])
    }

    List<Map>allIndex(String session, String name){
        def rows = sql.rows(getSentenceFromResource("/select-index.sql"),[
                session_id:session, name:name
        ])
        rows.inject([],{ list, row->
            list << [key:row.id, cached:row.cached]
        }) as List<Map>
    }

    void truncateTables(){
        try{
            sql.execute getSentenceFromResource("/truncate-index.sql")
            sql.execute getSentenceFromResource("/truncate-cache.sql")
        }catch(Exception e){
            log.error "Error truncating tables",e
            throw new AbortOperationException("Invalid connection for nf-pqcache")
        }
    }
}
