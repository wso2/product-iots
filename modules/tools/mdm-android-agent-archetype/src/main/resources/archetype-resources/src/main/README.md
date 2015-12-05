WSO2 EMM Agent
=================

Configure and build the Android client application
----------------------
Follow the instructions below to configure and build the Android client application:

1.  Get a Git clone of the project.
2.  Download <a href="https://developer.android.com/sdk/installing/installing-adt.html"> Android ADT plugin and configure </a> it in your Eclipse.
3.  Open the project in your Eclipse IDE.
4.  Import the project as an Android project using "File-->Import-->Existing Android Code Into Workspace"
5.  Two projects will show, a library and the agent. Clean the Library first and build it.
6.  Open the file properties of the Agent project.
7.  Under "Android" scroll down (past the Build targets).
8.  The library project will show with a red "X" next to it. Remove it.
9.  Add the library project you just built in step 3
10. Ensure the Library is also on your "Java Build Path" under Libraries.
11. Clean and build.
