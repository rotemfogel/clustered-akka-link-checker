# clustered-akka-link-checker
In order to run, simply add a run configuration with ```akka.Main``` as your main class and ```me.rotemfo.linkchecker.Main``` as the parameter.

If you are running Intellij IDEA, edit the .idea/workspace.xml and add the following:
```` 
<component name="RunManager">
    <configuration name="CluserMain" type="Application" factoryName="Application">
      <option name="MAIN_CLASS_NAME" value="akka.Main" />
      <module name="link-checker" />
      <option name="VM_PARAMETERS" value="-Dakka.cluster.min-nr-of-members=2" />
      <option name="PROGRAM_PARAMETERS" value="me.rotemfo.cluster.ClusterMain" />
      <method v="2">
        <option name="Make" enabled="true" />
      </method>
    </configuration>
    <configuration name="CluserWorker" type="Application" factoryName="Application">
      <option name="MAIN_CLASS_NAME" value="akka.Main" />
      <module name="link-checker" />
      <option name="VM_PARAMETERS" value="-Dakka.remote.netty.tcp.port=0 -Dakka.cluster.auto-down=on" />
      <option name="PROGRAM_PARAMETERS" value="me.rotemfo.cluster.CluserWorker" />
      <method v="2">
        <option name="Make" enabled="true" />
      </method>
    </configuration>
</component>
````
