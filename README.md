# link-checker
In order to run, simply add a run configuration with ```akka.Main``` as your main class and ```me.rotemfo.linkchecker.Main``` as the parameter.

If you are running Intellij IDEA, edit the .idea/workspace.xml and add the following:
```` 
<component name="RunManager">
    <configuration name="LinkChecker" type="Application" factoryName="Application">
      <option name="MAIN_CLASS_NAME" value="akka.Main" />
      <module name="link-checker" />
      <option name="PROGRAM_PARAMETERS" value="me.rotemfo.linkchecker.Main" />
      <method v="2">
        <option name="Make" enabled="true" />
      </method>
    </configuration>
  </component>
````
