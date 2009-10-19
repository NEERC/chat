NEERC Chat

Copyright (C) 2009 NEERC team


Directory Structure
~~~~~~~~~~~~~~~~~~~

  src       - java source code files
  resources - non-source-code files included into delivey .jar files
              such as GUI images, HTMLs, etc.
  etc       - auxiliary files (run-scripts, configurations) included
              into delivery units besides .jar files
  test      - java unit-tests
  target    - temporary directory created by build.xml that contains
              compiled .class files and delivery units


Deployment
~~~~~~~~~~

Both server and client chat applications are deployed as single .jar files
with external configuration XMLs and run-scripts.

The following software is required in order to create a delivery unit:

 * J2SE SDK 1.5 or later
 * Apache Ant

