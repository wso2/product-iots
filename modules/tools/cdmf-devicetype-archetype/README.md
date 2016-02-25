# cdmf-devicetype-archetype

To install this maven archetype 
     go to this folder `cdmf-devicetype-archetype` 
       
     mvn clean install

To create new project
     go to this folder `/wso2iots-1.0.0-SNAPSHOT/samples`
     
     mvn archetype:generate -DarchetypeCatalog=local
     
Then select the `cdmf.devicetype:cdmf-devicetype-archetype` as new archetype. Then you need to provide groupId, artifactId,
version, packaging, name of your device type and name for sensor as shown bellow.

       Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 3
       Define value for property 'groupId': : org.homeautomation              
       Define value for property 'artifactId': : safeLocker
       Define value for property 'version':  1.0-SNAPSHOT: : 1.0.0-SNAPSHOT
       Define value for property 'package':  org.homeautomation: : 
       Define value for property 'deviceType': : safeLocker
       Define value for property 'nameOfTheSensor': : lock
       Confirm properties configuration:
       groupId: org.homeautomation
       artifactId: safeLocker
       version: 1.0.0-SNAPSHOT
       package: org.homeautomation
       deviceType: safeLocker
       nameOfTheSensor: lock
       
       
To install sample app into IOTS
   open `device-deployer.xml` which is located in wso2iots-1.0.0-SNAPSHOT directory

Under modules tag add name of sample which you created as module as below
           
           <module>samples/safeLocker</module>

Under featureArtifacts tag add feature artifact definition as below
         
          <featureArtifactDef>
              org.homeautomation:org.homeautomation.safeLocker.feature:1.0.0-SNAPSHOT
          </featureArtifactDef>    
          
Under features tag add feature group definition as below
          
          <feature>
               <id>org.homeautomation.safeLocker.feature.group</id>
               <version>1.0.0-SNAPSHOT</version>
          </feature>
              
Finally to deploy sample device type into IoT Server       
   
           mvn clean install -f device-deployer.xml
           
Note: This command should be executed place where `device-deployer.xml` is located            

