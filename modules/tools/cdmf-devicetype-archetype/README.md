# multi-module-maven-archetype example


To create new project

     mvn archetype:generate -DarchetypeCatalog=local
     
Then select the cdmf.devicetype:cdmf-devicetype-archetype as new archetype. Then you need to provide groupId, artifactId, version and packaging as shown bellow.

       Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 3
       Define value for property 'groupId': : org.coffeeking
       Define value for property 'artifactId': : connectedcup
       Define value for property 'version':  1.0-SNAPSHOT: : 1.0.0-SNAPSHOT
       Define value for property 'package':  org.coffeeking: : 
       Confirm properties configuration:
       groupId: org.coffeeking
       artifactId: connectedcup
       version: 1.0.0-SNAPSHOT
       package: org.coffeeking
       Y: : Y
       


     

