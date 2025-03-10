package com.incsteps.nextflow.duckdb

import groovy.sql.Sql
import nextflow.Channel
import nextflow.plugin.Plugins
import nextflow.plugin.TestPluginDescriptorFinder
import nextflow.plugin.TestPluginManager
import nextflow.plugin.extension.PluginExtensionProvider
import org.pf4j.PluginDescriptorFinder
import spock.lang.Shared
import test.Dsl2Spec
import test.MockScriptRunner

import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.Manifest

class PluginTest extends Dsl2Spec{

    @Shared String pluginsMode

    def setupSpec(){
    }

    def cleanupSpec(){
    }

    def setup() {
        // reset previous instances
        PluginExtensionProvider.reset()
        // this need to be set *before* the plugin manager class is created
        pluginsMode = System.getProperty('pf4j.mode')
        System.setProperty('pf4j.mode', 'dev')
        // the plugin root should
        def root = Path.of('.').toAbsolutePath().normalize()
        def manager = new TestPluginManager(root){
            @Override
            protected PluginDescriptorFinder createPluginDescriptorFinder() {
                return new TestPluginDescriptorFinder(){
                    @Override
                    protected Manifest readManifestFromDirectory(Path pluginPath) {
                        def manifestPath= getManifestPath(pluginPath)
                        final input = Files.newInputStream(manifestPath)
                        return new Manifest(input)
                    }
                    protected Path getManifestPath(Path pluginPath) {
                        return pluginPath.resolve('build/tmp/jar/MANIFEST.MF')
                    }
                }
            }
        }
        Plugins.init(root, 'dev', manager)
    }

    def cleanup() {
        Plugins.stop()
        PluginExtensionProvider.reset()
        pluginsMode ? System.setProperty('pf4j.mode',pluginsMode) : System.clearProperty('pf4j.mode')
    }

    def 'should starts' () {
        given: 'a clean directory'
        def path = Files.createTempDirectory("nf-duckdbcache")
        def file = path.toAbsolutePath().toString()+"/test.db"

        when:
        def SCRIPT = '''
            channel.of('hi!') 
            '''
        and:
        def result = new MockScriptRunner([
                duckdbcache:[
                        file: file
                ]
        ]).setScript(SCRIPT).execute()
        then:
        result.val == 'hi!'
        result.val == Channel.STOP

        and:
        new File(file).size()
        new File(file).delete()
        new File(path.toAbsolutePath().toString()).deleteDir()
    }

}
