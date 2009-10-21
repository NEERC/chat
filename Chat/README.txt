NEERC Chat

Copyright (C) 2009 NEERC team


Directory Structure
~~~~~~~~~~~~~~~~~~~

  src       - source code files & resources
  etc       - auxiliary files (run-scripts, configurations) included
              into delivery units besides .jar files
  target    - temporary directory created by Maven that contains
              compiled .class files and delivery units


Deployment
~~~~~~~~~~

Both server and client chat applications are deployed as single .jar files
with external configuration XMLs and run-scripts.

The following software is required in order to create a delivery unit:

 * J2SE SDK 1.6 or later
 * Apache Maven

