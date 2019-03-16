# jSite

jSite is a tool that uploads websites from a local directory into Freenet.

## Compiling

Gradle’s build process is handled by [Gradle](https://gradle.org/). Just use the Gradle wrapper that comes with jSite:

    # ./gradlew clean build fatJar

This will resolve jSite’s dependencies, compile jSite, run all the tests (of which there are only a few, unfortunately), and put the file `jSite-`*&lt;version&gt;*`-jar-with-dependencies.jar` into the `build/libs` directory. This is the file that you can simply run, either by double-clicking it in a file manager of your choice, or by using the command line:

    # java -jar build/libs/jSite-0.14-jar-with-dependencies.jar

## Using

jSite is built on a “wizard dialog” concept: it consists of several different dialogs that can be navigated to by using “previous” and “next” buttons. This allows jSite to check that all necessary input for a following dialog has been given in the current dialog.

### The Node Manager

This dialog lets you configure the connection to your node. jSite’s default setup should match the default setup of Freenet, i.e. a default node is added which can be reached on port 9481 on localhost.

In the logical order of the dialogs this is the first dialog; however, jSite starts with showing the second dialog: the project dialog.

### The Project Dialog

In this dialog you can manage your projects, i.e. the websites you have inserted or are planning to insert into Freenet.

Adding a project will create a new key pair; a connection to the Freenet node is required for this. If you want to use an already existing keypair you can do so by using the “Manage Keys” function. Here you can also copy keys from existing Web of Trust identities.

Pressing the “next” button will take you to a more detailed view of the project you selected.

### The Project Files

The Project Files dialog lets you adjust settings for the files in your project. The most important functionality this dialog offers is the selection of an “index,” or default, file, i.e., the file that is served when the URI of your freesite is navigated to without a file in the URL path.

Pressing “next” here will take you to where the action finally happens.

### The Project Insert

This dialog shows the progress of the insert. There’s not really much happening here. You have the possibility to cancel the insert using the “cancel” button.

As soon as the Freenet node has generated the final URI of the freesite, the “copy URI to clipboard” button will be activated, letting you paste the final URI wherever you want. For security reasons (e.g., the “mobile attacker source tracing” attack) you should not publish the URI until the insert has finished completely.

## Preferences

In this dialog you can configure a couple of settings of your local jSite instance.

If you have specific requirements for where jSite should store temporary files (i.e., a directory on an encrypted volume), you can configure this directory here.

jSite can read its configuration file from a number of different locations. In case you are using jSite for long-living projects it is recommended to keep the configuration in your user’s home directory. However, this might not be an option for when you want to run jSite from an external drive (e.g., a thumb drive) and don’t want to leave permanent traces on the computer you are using. In this case the option “next to the JAR file” might be just for you which will cause jSite to store the configuration file in the directory the JAR file is stored in.

There is also a third option that tells jSite to store the configuration in a custom location. For this option to be active you have to specify the location when starting jSite with a command-line parameter (`--config-file=`*&lt;config file>*).

The third section in the dialog configures jSite’s insert behaviour.
